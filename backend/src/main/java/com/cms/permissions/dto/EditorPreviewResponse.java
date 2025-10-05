package com.cms.permissions.dto;

import java.util.List;
import java.util.Map;

/**
 * 编辑器预览响应DTO
 */
public class EditorPreviewResponse {
    
    private String htmlContent;
    private String plainText;
    private List<String> headings;
    private List<Map<String, Object>> codeBlocks;
    private List<Map<String, Object>> images;
    private List<Map<String, Object>> links;
    private Map<String, Object> metadata;
    private Integer wordCount;
    private Integer characterCount;
    private Integer readingTime; // 预估阅读时间（分钟）
    
    public EditorPreviewResponse() {}
    
    public EditorPreviewResponse(String htmlContent) {
        this.htmlContent = htmlContent;
    }
    
    // Getters and Setters
    public String getHtmlContent() {
        return htmlContent;
    }
    
    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }
    
    public String getPlainText() {
        return plainText;
    }
    
    public void setPlainText(String plainText) {
        this.plainText = plainText;
    }
    
    public List<String> getHeadings() {
        return headings;
    }
    
    public void setHeadings(List<String> headings) {
        this.headings = headings;
    }
    
    public List<Map<String, Object>> getCodeBlocks() {
        return codeBlocks;
    }
    
    public void setCodeBlocks(List<Map<String, Object>> codeBlocks) {
        this.codeBlocks = codeBlocks;
    }
    
    public List<Map<String, Object>> getImages() {
        return images;
    }
    
    public void setImages(List<Map<String, Object>> images) {
        this.images = images;
    }
    
    public List<Map<String, Object>> getLinks() {
        return links;
    }
    
    public void setLinks(List<Map<String, Object>> links) {
        this.links = links;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public Integer getWordCount() {
        return wordCount;
    }
    
    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }
    
    public Integer getCharacterCount() {
        return characterCount;
    }
    
    public void setCharacterCount(Integer characterCount) {
        this.characterCount = characterCount;
    }
    
    public Integer getReadingTime() {
        return readingTime;
    }
    
    public void setReadingTime(Integer readingTime) {
        this.readingTime = readingTime;
    }
}