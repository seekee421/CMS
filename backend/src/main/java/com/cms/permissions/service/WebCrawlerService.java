package com.cms.permissions.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class WebCrawlerService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebCrawlerService.class);
    
    // 连接超时时间（毫秒）
    private static final int TIMEOUT = 30000;
    
    // User-Agent
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    
    /**
     * 抓取网页内容
     */
    public CrawlResult crawlPage(String url) {
        try {
            logger.info("开始抓取网页: {}", url);
            
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .followRedirects(true)
                    .get();
            
            CrawlResult result = new CrawlResult();
            result.setUrl(url);
            result.setTitle(extractTitle(doc));
            result.setContent(extractContent(doc));
            result.setMetadata(extractMetadata(doc));
            result.setLinks(extractLinks(doc, url));
            result.setSuccess(true);
            
            logger.info("成功抓取网页: {}, 标题: {}", url, result.getTitle());
            return result;
            
        } catch (IOException e) {
            logger.error("抓取网页失败: {}, 错误: {}", url, e.getMessage());
            CrawlResult result = new CrawlResult();
            result.setUrl(url);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            return result;
        }
    }
    
    /**
     * 批量抓取网页
     */
    public List<CrawlResult> crawlPages(List<String> urls) {
        List<CrawlResult> results = new ArrayList<>();
        
        for (String url : urls) {
            try {
                CrawlResult result = crawlPage(url);
                results.add(result);
                
                // 添加延迟，避免过于频繁的请求
                Thread.sleep(1000);
                
            } catch (InterruptedException e) {
                logger.warn("爬虫被中断: {}", e.getMessage());
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        return results;
    }
    
    /**
     * 提取页面标题
     */
    private String extractTitle(Document doc) {
        String title = doc.title();
        if (title == null || title.trim().isEmpty()) {
            Element h1 = doc.selectFirst("h1");
            if (h1 != null) {
                title = h1.text();
            }
        }
        return title != null ? title.trim() : "无标题";
    }
    
    /**
     * 提取页面主要内容
     */
    private String extractContent(Document doc) {
        // 移除不需要的元素
        doc.select("script, style, nav, header, footer, aside, .sidebar, .menu, .navigation").remove();
        
        // 尝试找到主要内容区域
        Element mainContent = findMainContent(doc);
        
        if (mainContent != null) {
            return cleanText(mainContent.html());
        }
        
        // 如果没有找到主要内容区域，使用body
        Element body = doc.body();
        return body != null ? cleanText(body.html()) : "";
    }
    
    /**
     * 查找主要内容区域
     */
    private Element findMainContent(Document doc) {
        // 常见的内容区域选择器
        String[] contentSelectors = {
            "main", ".main", "#main",
            ".content", "#content", ".main-content",
            ".article", ".post", ".entry",
            ".documentation", ".doc-content",
            ".container .row .col"
        };
        
        for (String selector : contentSelectors) {
            Element element = doc.selectFirst(selector);
            if (element != null && element.text().length() > 100) {
                return element;
            }
        }
        
        // 如果没有找到，尝试找到最大的文本块
        Elements divs = doc.select("div");
        Element largestDiv = null;
        int maxTextLength = 0;
        
        for (Element div : divs) {
            int textLength = div.ownText().length();
            if (textLength > maxTextLength) {
                maxTextLength = textLength;
                largestDiv = div;
            }
        }
        
        return largestDiv;
    }
    
    /**
     * 清理文本内容
     */
    private String cleanText(String html) {
        if (html == null) return "";
        
        // 移除多余的空白字符
        html = html.replaceAll("\\s+", " ");
        html = html.replaceAll("\\n\\s*\\n", "\n");
        
        return html.trim();
    }
    
    /**
     * 提取页面元数据
     */
    private Map<String, String> extractMetadata(Document doc) {
        Map<String, String> metadata = new HashMap<>();
        
        // 提取meta标签
        Elements metaTags = doc.select("meta");
        for (Element meta : metaTags) {
            String name = meta.attr("name");
            String property = meta.attr("property");
            String content = meta.attr("content");
            
            if (!name.isEmpty() && !content.isEmpty()) {
                metadata.put(name, content);
            } else if (!property.isEmpty() && !content.isEmpty()) {
                metadata.put(property, content);
            }
        }
        
        // 提取其他有用信息
        metadata.put("charset", doc.charset().name());
        metadata.put("language", doc.attr("lang"));
        
        return metadata;
    }
    
    /**
     * 提取页面链接
     */
    private List<String> extractLinks(Document doc, String baseUrl) {
        List<String> links = new ArrayList<>();
        Elements linkElements = doc.select("a[href]");
        
        for (Element link : linkElements) {
            String href = link.attr("href");
            try {
                URL url = new URL(new URL(baseUrl), href);
                String absoluteUrl = url.toString();
                
                // 只收集同域名的链接
                if (isSameDomain(baseUrl, absoluteUrl)) {
                    links.add(absoluteUrl);
                }
            } catch (Exception e) {
                // 忽略无效的URL
            }
        }
        
        return links;
    }
    
    /**
     * 检查是否为同一域名
     */
    private boolean isSameDomain(String baseUrl, String targetUrl) {
        try {
            URL base = new URL(baseUrl);
            URL target = new URL(targetUrl);
            return base.getHost().equals(target.getHost());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 爬虫结果类
     */
    public static class CrawlResult {
        private String url;
        private String title;
        private String content;
        private Map<String, String> metadata;
        private List<String> links;
        private boolean success;
        private String errorMessage;
        
        // Getter和Setter方法
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public Map<String, String> getMetadata() { return metadata; }
        public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
        
        public List<String> getLinks() { return links; }
        public void setLinks(List<String> links) { this.links = links; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}