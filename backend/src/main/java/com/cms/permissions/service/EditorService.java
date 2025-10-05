package com.cms.permissions.service;

import com.cms.permissions.dto.EditorContentRequest;
import com.cms.permissions.dto.EditorContentResponse;
import com.cms.permissions.dto.EditorPreviewResponse;
import com.cms.permissions.entity.Document;
import com.cms.permissions.entity.User;
import com.cms.permissions.exception.ResourceNotFoundException;
import com.cms.permissions.repository.DocumentRepository;
import com.cms.permissions.repository.UserRepository;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class EditorService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MinIOService minIOService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final Parser markdownParser = Parser.builder()
            .extensions(Arrays.asList(TablesExtension.create(), HeadingAnchorExtension.create()))
            .build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder()
            .extensions(Arrays.asList(TablesExtension.create(), HeadingAnchorExtension.create()))
            .build();

    private static final String DRAFT_KEY_PREFIX = "editor:draft:";
    private static final int DRAFT_EXPIRE_HOURS = 24;

    /**
     * 保存文档内容
     */
    @CacheEvict(value = "documents", key = "#request.documentId")
    public EditorContentResponse saveContent(EditorContentRequest request, Long userId) {
        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + request.getDocumentId()));

        // 更新文档内容
        if (request.getTitle() != null) {
            document.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            document.setContent(request.getContent());
        }
        document.setUpdatedAt(LocalDateTime.now());

        // 如果不是草稿，更新文档状态
        if (!Boolean.TRUE.equals(request.getIsDraft())) {
            document.setStatus(Document.DocumentStatus.DRAFT);
        }

        Document savedDocument = documentRepository.save(document);

        // 清除草稿缓存
        clearDraft(request.getDocumentId(), userId);

        return buildEditorContentResponse(savedDocument, userId);
    }

    /**
     * 获取文档内容
     */
    @Cacheable(value = "documents", key = "#documentId")
    public EditorContentResponse getContent(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        return buildEditorContentResponse(document, null);
    }

    /**
     * 预览Markdown内容
     */
    public EditorPreviewResponse previewMarkdown(String markdownContent) {
        if (markdownContent == null || markdownContent.trim().isEmpty()) {
            return new EditorPreviewResponse("");
        }

        try {
            // 解析Markdown
            Node document = markdownParser.parse(markdownContent);
            String htmlContent = htmlRenderer.render(document);

            EditorPreviewResponse response = new EditorPreviewResponse(htmlContent);

            // 提取纯文本
            String plainText = markdownContent.replaceAll("\\*\\*(.+?)\\*\\*", "$1")
                    .replaceAll("\\*(.+?)\\*", "$1")
                    .replaceAll("\\[(.+?)\\]\\(.+?\\)", "$1")
                    .replaceAll("#+\\s*", "")
                    .replaceAll("```[\\s\\S]*?```", "")
                    .replaceAll("`(.+?)`", "$1");
            response.setPlainText(plainText);

            // 提取标题
            List<String> headings = extractHeadings(markdownContent);
            response.setHeadings(headings);

            // 提取代码块
            List<Map<String, Object>> codeBlocks = extractCodeBlocks(markdownContent);
            response.setCodeBlocks(codeBlocks);

            // 提取图片
            List<Map<String, Object>> images = extractImages(markdownContent);
            response.setImages(images);

            // 提取链接
            List<Map<String, Object>> links = extractLinks(markdownContent);
            response.setLinks(links);

            // 计算统计信息
            response.setWordCount(countWords(plainText));
            response.setCharacterCount(plainText.length());
            response.setReadingTime(calculateReadingTime(plainText));

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to preview markdown content", e);
        }
    }

    /**
     * 上传媒体文件
     */
    public Map<String, String> uploadMedia(MultipartFile file, Long documentId, Long userId) {
        try {
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !isValidMediaType(contentType)) {
                throw new IllegalArgumentException("Unsupported file type: " + contentType);
            }

            // 验证文件大小 (20MB)
            if (file.getSize() > 20 * 1024 * 1024) {
                throw new IllegalArgumentException("File size exceeds 20MB limit");
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String fileName = "editor/" + UUID.randomUUID().toString() + extension;

            // 上传到MinIO
            String fileUrl = minIOService.uploadFile(file, fileName);

            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("originalFileName", originalFilename);
            response.put("fileUrl", fileUrl);
            response.put("fileType", contentType);
            response.put("fileSize", String.valueOf(file.getSize()));

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload media file", e);
        }
    }

    /**
     * 自动保存草稿
     */
    public Map<String, Object> autoSave(EditorContentRequest request, Long userId) {
        String draftKey = DRAFT_KEY_PREFIX + request.getDocumentId() + ":" + userId;
        
        Map<String, Object> draftData = new HashMap<>();
        draftData.put("documentId", request.getDocumentId());
        draftData.put("title", request.getTitle());
        draftData.put("content", request.getContent());
        draftData.put("changeLog", request.getChangeLog());
        draftData.put("savedAt", LocalDateTime.now().toString());

        // 保存到Redis，设置过期时间
        redisTemplate.opsForValue().set(draftKey, draftData, DRAFT_EXPIRE_HOURS, TimeUnit.HOURS);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("savedAt", LocalDateTime.now());
        response.put("message", "Draft saved successfully");

        return response;
    }

    /**
     * 获取草稿内容
     */
    public EditorContentResponse getDraft(Long documentId, Long userId) {
        String draftKey = DRAFT_KEY_PREFIX + documentId + ":" + userId;
        
        @SuppressWarnings("unchecked")
        Map<String, Object> draftData = (Map<String, Object>) redisTemplate.opsForValue().get(draftKey);
        
        if (draftData == null) {
            // 如果没有草稿，返回文档的当前内容
            return getContent(documentId);
        }

        EditorContentResponse response = new EditorContentResponse();
        response.setDocumentId(documentId);
        response.setTitle((String) draftData.get("title"));
        response.setContent((String) draftData.get("content"));
        response.setChangeLog((String) draftData.get("changeLog"));
        response.setIsDraft(true);
        response.setHasUnsavedChanges(true);
        
        String savedAtStr = (String) draftData.get("savedAt");
        if (savedAtStr != null) {
            response.setLastSaved(LocalDateTime.parse(savedAtStr));
        }

        return response;
    }

    /**
     * 清除草稿
     */
    public void clearDraft(Long documentId, Long userId) {
        String draftKey = DRAFT_KEY_PREFIX + documentId + ":" + userId;
        redisTemplate.delete(draftKey);
    }

    // 私有辅助方法

    private EditorContentResponse buildEditorContentResponse(Document document, Long userId) {
        EditorContentResponse response = new EditorContentResponse();
        response.setDocumentId(document.getId());
        response.setTitle(document.getTitle());
        response.setContent(document.getContent());
        response.setStatus(document.getStatus().toString());
        response.setLastModified(document.getUpdatedAt());
        response.setVersion(document.getVersion());
        response.setCreatedBy(document.getCreatedBy());
        response.setIsDraft(false);
        response.setHasUnsavedChanges(false);

        // 获取创建者姓名
        if (document.getCreatedBy() != null) {
            userRepository.findById(document.getCreatedBy())
                    .ifPresent(user -> response.setCreatedByName(user.getUsername()));
        }

        return response;
    }

    private boolean isValidMediaType(String contentType) {
        return contentType.startsWith("image/") || 
               contentType.startsWith("video/") || 
               contentType.startsWith("audio/") ||
               contentType.equals("application/pdf");
    }

    private List<String> extractHeadings(String markdown) {
        List<String> headings = new ArrayList<>();
        Pattern pattern = Pattern.compile("^(#{1,6})\\s+(.+)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(markdown);
        
        while (matcher.find()) {
            headings.add(matcher.group(2).trim());
        }
        
        return headings;
    }

    private List<Map<String, Object>> extractCodeBlocks(String markdown) {
        List<Map<String, Object>> codeBlocks = new ArrayList<>();
        Pattern pattern = Pattern.compile("```(\\w+)?\\n([\\s\\S]*?)```", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(markdown);
        
        while (matcher.find()) {
            Map<String, Object> codeBlock = new HashMap<>();
            codeBlock.put("language", matcher.group(1) != null ? matcher.group(1) : "text");
            codeBlock.put("code", matcher.group(2).trim());
            codeBlocks.add(codeBlock);
        }
        
        return codeBlocks;
    }

    private List<Map<String, Object>> extractImages(String markdown) {
        List<Map<String, Object>> images = new ArrayList<>();
        Pattern pattern = Pattern.compile("!\\[([^\\]]*)\\]\\(([^\\)]+)\\)");
        Matcher matcher = pattern.matcher(markdown);
        
        while (matcher.find()) {
            Map<String, Object> image = new HashMap<>();
            image.put("alt", matcher.group(1));
            image.put("src", matcher.group(2));
            images.add(image);
        }
        
        return images;
    }

    private List<Map<String, Object>> extractLinks(String markdown) {
        List<Map<String, Object>> links = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)");
        Matcher matcher = pattern.matcher(markdown);
        
        while (matcher.find()) {
            Map<String, Object> link = new HashMap<>();
            link.put("text", matcher.group(1));
            link.put("url", matcher.group(2));
            links.add(link);
        }
        
        return links;
    }

    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    private int calculateReadingTime(String text) {
        int wordCount = countWords(text);
        // 假设平均阅读速度为每分钟200字
        return Math.max(1, (int) Math.ceil(wordCount / 200.0));
    }
}