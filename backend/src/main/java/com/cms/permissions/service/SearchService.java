package com.cms.permissions.service;

import com.cms.permissions.entity.DocumentIndex;
import com.cms.permissions.repository.DocumentIndexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    @Autowired
    private DocumentIndexRepository documentIndexRepository;

    /**
     * 全文搜索（MySQL FULLTEXT），支持可选的布尔模式
     */
    @PreAuthorize("hasAuthority('SEARCH:QUERY')")
    public Page<DocumentIndex> fullTextSearch(String query,
                                              Boolean isPublicOnly,
                                              String language,
                                              String contentType,
                                              int page,
                                              int size,
                                              boolean booleanMode) {
        Pageable pageable = PageRequest.of(page, size);
        if (booleanMode) {
            return documentIndexRepository.searchBooleanMode(query, asBool(isPublicOnly), language, pageable);
        }
        return documentIndexRepository.searchFullText(query, asBool(isPublicOnly), language, contentType, pageable);
    }

    /**
     * 标题搜索（LIKE）
     */
    @PreAuthorize("hasAuthority('SEARCH:QUERY')")
    public Page<DocumentIndex> searchByTitle(String query, Boolean isPublicOnly, String language, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return documentIndexRepository.searchByTitle(query, asBool(isPublicOnly), language, pageable);
    }

    /**
     * 内容搜索（LIKE）
     */
    @PreAuthorize("hasAuthority('SEARCH:QUERY')")
    public Page<DocumentIndex> searchByContent(String query, Boolean isPublicOnly, String language, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return documentIndexRepository.searchByContent(query, asBool(isPublicOnly), language, pageable);
    }

    /**
     * 搜索建议
     */
    @PreAuthorize("hasAuthority('SEARCH:SUGGEST')")
    public List<String> getSearchSuggestions(String query, Boolean isPublicOnly) {
        return documentIndexRepository.findSearchSuggestions(query, asBool(isPublicOnly));
    }

    // 预留：Elasticsearch 接口
    @PreAuthorize("hasAuthority('SEARCH:QUERY')")
    public Page<DocumentIndex> elasticsearchSearch(String query, int page, int size) {
        throw new UnsupportedOperationException("Elasticsearch integration is not implemented yet");
    }

    private boolean asBool(Boolean b) {
        return b != null && b;
    }
}