package com.cms.permissions.controller;

import com.cms.permissions.entity.DocumentIndex;
import com.cms.permissions.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/fulltext")
    @PreAuthorize("hasAuthority('SEARCH:QUERY')")
    public ResponseEntity<Page<DocumentIndex>> fullText(@RequestParam String q,
                                                        @RequestParam(required = false) Boolean publicOnly,
                                                        @RequestParam(required = false) String language,
                                                        @RequestParam(required = false) String contentType,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(defaultValue = "false") boolean booleanMode) {
        Page<DocumentIndex> result = searchService.fullTextSearch(q, publicOnly, language, contentType, page, size, booleanMode);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/title")
    @PreAuthorize("hasAuthority('SEARCH:QUERY')")
    public ResponseEntity<Page<DocumentIndex>> byTitle(@RequestParam String q,
                                                       @RequestParam(required = false) Boolean publicOnly,
                                                       @RequestParam(required = false) String language,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(searchService.searchByTitle(q, publicOnly, language, page, size));
    }

    @GetMapping("/content")
    @PreAuthorize("hasAuthority('SEARCH:QUERY')")
    public ResponseEntity<Page<DocumentIndex>> byContent(@RequestParam String q,
                                                         @RequestParam(required = false) Boolean publicOnly,
                                                         @RequestParam(required = false) String language,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(searchService.searchByContent(q, publicOnly, language, page, size));
    }

    @GetMapping("/suggest")
    @PreAuthorize("hasAuthority('SEARCH:SUGGEST')")
    public ResponseEntity<List<String>> suggest(@RequestParam String q,
                                                @RequestParam(required = false) Boolean publicOnly) {
        return ResponseEntity.ok(searchService.getSearchSuggestions(q, publicOnly));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('SEARCH:READ:STATS')")
    public ResponseEntity<Map<String, Object>> stats() {
        // 简单返回数量统计，详细统计可在 StatisticsService 中实现
        return ResponseEntity.ok(Map.of("totalIndexes", 0));
    }
}