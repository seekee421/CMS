package com.cms.permissions.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ContentParserService {
    
    private static final Logger logger = LoggerFactory.getLogger(ContentParserService.class);
    
    private final FlexmarkHtmlConverter converter;
    private final ObjectMapper objectMapper;
    
    public ContentParserService() {
        this.converter = FlexmarkHtmlConverter.builder().build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 解析网页内容
     */
    public ParsedContent parseContent(WebCrawlerService.CrawlResult crawlResult) {
        try {
            logger.info("开始解析内容: {}", crawlResult.getUrl());
            
            ParsedContent parsed = new ParsedContent();
            parsed.setSourceUrl(crawlResult.getUrl());
            parsed.setTitle(crawlResult.getTitle());
            
            // 解析HTML内容
            String cleanedHtml = cleanHtml(crawlResult.getContent());
            String markdown = convertHtmlToMarkdown(cleanedHtml);
            parsed.setContent(markdown);
            
            // 提取和解析元数据
            parsed.setCategory(extractCategory(crawlResult));
            parsed.setTags(extractTags(crawlResult));
            parsed.setVersion(extractVersion(crawlResult));
            parsed.setOriginalId(generateOriginalId(crawlResult.getUrl()));
            
            // 计算内容统计
            parsed.setContentLength(markdown.length());
            parsed.setWordCount(countWords(markdown));
            
            logger.info("内容解析完成: {}, 字数: {}", crawlResult.getUrl(), parsed.getWordCount());
            return parsed;
            
        } catch (Exception e) {
            logger.error("内容解析失败: {}, 错误: {}", crawlResult.getUrl(), e.getMessage());
            throw new RuntimeException("内容解析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 清理HTML内容
     */
    private String cleanHtml(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }
        
        Document doc = Jsoup.parse(html);
        
        // 移除不需要的元素
        doc.select("script, style, nav, header, footer, aside").remove();
        doc.select(".sidebar, .menu, .navigation, .breadcrumb").remove();
        doc.select(".advertisement, .ads, .social-share").remove();
        
        // 清理属性
        Elements allElements = doc.select("*");
        for (Element element : allElements) {
            // 保留重要属性，移除样式和事件属性
            element.removeAttr("style");
            element.removeAttr("class");
            element.removeAttr("id");
            element.removeAttr("onclick");
            element.removeAttr("onload");
        }
        
        // 保留链接的href属性
        Elements links = doc.select("a");
        for (Element link : links) {
            String href = link.attr("href");
            link.clearAttributes();
            if (!href.isEmpty()) {
                link.attr("href", href);
            }
        }
        
        // 保留图片的src和alt属性
        Elements images = doc.select("img");
        for (Element img : images) {
            String src = img.attr("src");
            String alt = img.attr("alt");
            img.clearAttributes();
            if (!src.isEmpty()) {
                img.attr("src", src);
            }
            if (!alt.isEmpty()) {
                img.attr("alt", alt);
            }
        }
        
        return doc.body().html();
    }
    
    /**
     * 将HTML转换为Markdown
     */
    private String convertHtmlToMarkdown(String html) {
        try {
            String markdown = converter.convert(html);
            
            // 后处理：清理多余的空行
            markdown = markdown.replaceAll("\\n{3,}", "\n\n");
            markdown = markdown.trim();
            
            return markdown;
        } catch (Exception e) {
            logger.warn("HTML转Markdown失败，返回原始HTML: {}", e.getMessage());
            return html;
        }
    }
    
    /**
     * 提取文档分类
     */
    private String extractCategory(WebCrawlerService.CrawlResult crawlResult) {
        String url = crawlResult.getUrl();
        Map<String, String> metadata = crawlResult.getMetadata();
        
        // 从URL路径提取分类
        if (url.contains("/quick-start/")) {
            return "快速入门";
        } else if (url.contains("/manual/") || url.contains("/docs/")) {
            return "产品手册";
        } else if (url.contains("/api/")) {
            return "API文档";
        } else if (url.contains("/tutorial/")) {
            return "教程指南";
        } else if (url.contains("/jdbc/")) {
            return "JDBC开发";
        } else if (url.contains("/programming/")) {
            return "编程指南";
        }
        
        // 从元数据提取分类
        if (metadata != null) {
            String category = metadata.get("category");
            if (category != null && !category.trim().isEmpty()) {
                return category.trim();
            }
        }
        
        // 默认分类
        return "技术文档";
    }
    
    /**
     * 提取标签
     */
    private String extractTags(WebCrawlerService.CrawlResult crawlResult) {
        Set<String> tags = new HashSet<>();
        String url = crawlResult.getUrl();
        String title = crawlResult.getTitle();
        Map<String, String> metadata = crawlResult.getMetadata();
        
        // 从URL提取标签
        if (url.contains("dameng")) tags.add("达梦数据库");
        if (url.contains("jdbc")) tags.add("JDBC");
        if (url.contains("java")) tags.add("Java");
        if (url.contains("api")) tags.add("API");
        if (url.contains("dm8")) tags.add("DM8");
        
        // 从标题提取标签
        if (title != null) {
            if (title.contains("快速")) tags.add("快速入门");
            if (title.contains("安装")) tags.add("安装");
            if (title.contains("配置")) tags.add("配置");
            if (title.contains("开发")) tags.add("开发");
            if (title.contains("连接")) tags.add("数据库连接");
        }
        
        // 从元数据提取标签
        if (metadata != null) {
            String keywords = metadata.get("keywords");
            if (keywords != null && !keywords.trim().isEmpty()) {
                String[] keywordArray = keywords.split("[,，;；]");
                for (String keyword : keywordArray) {
                    String trimmed = keyword.trim();
                    if (!trimmed.isEmpty()) {
                        tags.add(trimmed);
                    }
                }
            }
        }
        
        // 转换为JSON字符串
        try {
            return objectMapper.writeValueAsString(new ArrayList<>(tags));
        } catch (JsonProcessingException e) {
            logger.warn("标签序列化失败: {}", e.getMessage());
            return "[]";
        }
    }
    
    /**
     * 提取版本信息
     */
    private String extractVersion(WebCrawlerService.CrawlResult crawlResult) {
        String url = crawlResult.getUrl();
        String title = crawlResult.getTitle();
        
        // 从URL提取版本
        Pattern versionPattern = Pattern.compile("v?(\\d+\\.\\d+(?:\\.\\d+)?)");
        Matcher matcher = versionPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // 从标题提取版本
        if (title != null) {
            matcher = versionPattern.matcher(title);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        
        // 检查是否包含DM8
        if (url.contains("dm8") || (title != null && title.toLowerCase().contains("dm8"))) {
            return "8.0";
        }
        
        return "1.0";
    }
    
    /**
     * 生成原始ID
     */
    private String generateOriginalId(String url) {
        // 使用URL的hash作为原始ID
        return "eco_" + Math.abs(url.hashCode());
    }
    
    /**
     * 统计单词数量
     */
    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        
        // 移除Markdown标记
        String cleanText = text.replaceAll("[#*`\\[\\]()_~]", " ");
        
        // 分割单词（支持中英文）
        String[] words = cleanText.split("\\s+");
        int count = 0;
        
        for (String word : words) {
            if (!word.trim().isEmpty()) {
                // 中文字符按字符数计算，英文按单词计算
                if (word.matches(".*[\\u4e00-\\u9fa5].*")) {
                    count += word.replaceAll("[^\\u4e00-\\u9fa5]", "").length();
                } else {
                    count++;
                }
            }
        }
        
        return count;
    }
    
    /**
     * 解析后的内容类
     */
    public static class ParsedContent {
        private String sourceUrl;
        private String title;
        private String content;
        private String category;
        private String tags;
        private String version;
        private String originalId;
        private int contentLength;
        private int wordCount;
        
        // Getter和Setter方法
        public String getSourceUrl() { return sourceUrl; }
        public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public String getOriginalId() { return originalId; }
        public void setOriginalId(String originalId) { this.originalId = originalId; }
        
        public int getContentLength() { return contentLength; }
        public void setContentLength(int contentLength) { this.contentLength = contentLength; }
        
        public int getWordCount() { return wordCount; }
        public void setWordCount(int wordCount) { this.wordCount = wordCount; }
    }
}