package com.cms.permissions.controller;

import com.cms.permissions.config.CacheConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cache/config")
public class CacheConfigController {

    @Autowired
    private CacheConfigManager cacheConfigManager;

    @GetMapping
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<Map<String, Object>> getCacheConfig() {
        return ResponseEntity.ok(cacheConfigManager.getCacheConfigInfo());
    }

    @GetMapping("/ttl")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<Map<String, Duration>> getAllCacheTtl() {
        return ResponseEntity.ok(cacheConfigManager.getAllCacheTtl());
    }

    @GetMapping("/ttl/{cacheName}")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<Map<String, Object>> getCacheTtl(@PathVariable String cacheName) {
        if (!cacheConfigManager.isValidCacheName(cacheName)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid cache name: " + cacheName);
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("cacheName", cacheName);
        response.put("ttl", cacheConfigManager.getCacheTtl(cacheName));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/ttl/{cacheName}")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<Map<String, Object>> setCacheTtl(
            @PathVariable String cacheName,
            @RequestParam int minutes) {
        
        if (!cacheConfigManager.isValidCacheName(cacheName)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid cache name: " + cacheName);
            return ResponseEntity.badRequest().body(error);
        }

        if (minutes <= 0 || minutes > 1440) { // 最大24小时
            Map<String, Object> error = new HashMap<>();
            error.put("error", "TTL must be between 1 and 1440 minutes");
            return ResponseEntity.badRequest().body(error);
        }

        Duration newTtl = Duration.ofMinutes(minutes);
        cacheConfigManager.setCacheTtl(cacheName, newTtl);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "TTL updated successfully");
        response.put("cacheName", cacheName);
        response.put("newTtl", newTtl);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommendations")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<Map<String, String>> getRecommendations() {
        return ResponseEntity.ok(cacheConfigManager.getRecommendedConfig());
    }

    @PostMapping("/reset")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<Map<String, Object>> resetToDefaults() {
        // 重置为默认配置
        cacheConfigManager.setCacheTtl("userPermissions", Duration.ofMinutes(10));
        cacheConfigManager.setCacheTtl("userDocumentAssignments", Duration.ofMinutes(5));
        cacheConfigManager.setCacheTtl("documentPublicStatus", Duration.ofMinutes(15));

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Cache configuration reset to defaults");
        response.put("config", cacheConfigManager.getAllCacheTtl());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate/{cacheName}")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<Map<String, Object>> validateCacheName(@PathVariable String cacheName) {
        Map<String, Object> response = new HashMap<>();
        response.put("cacheName", cacheName);
        response.put("isValid", cacheConfigManager.isValidCacheName(cacheName));
        
        if (!cacheConfigManager.isValidCacheName(cacheName)) {
            response.put("validCacheNames", cacheConfigManager.getAllCacheTtl().keySet());
        }
        
        return ResponseEntity.ok(response);
    }
}