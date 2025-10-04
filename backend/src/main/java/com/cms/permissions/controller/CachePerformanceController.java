package com.cms.permissions.controller;

import com.cms.permissions.service.CachePerformanceAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cache/performance")
public class CachePerformanceController {

    @Autowired
    private CachePerformanceAnalyzer performanceAnalyzer;

    @GetMapping("/report")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<CachePerformanceAnalyzer.PerformanceReport> getPerformanceReport() {
        CachePerformanceAnalyzer.PerformanceReport report = performanceAnalyzer.getPerformanceReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<Map<String, Object>> getPerformanceSummary() {
        CachePerformanceAnalyzer.PerformanceReport report = performanceAnalyzer.getPerformanceReport();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("hitRate", String.format("%.2f%%", report.getHitRate()));
        summary.put("totalRequests", report.getTotalRequests());
        summary.put("avgResponseTime", report.getAvgResponseTime() + "ms");
        summary.put("totalEvictions", report.getTotalEvictions());
        summary.put("status", getPerformanceStatus(report.getHitRate(), report.getAvgResponseTime()));
        
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/trends")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<Map<String, Object>> getPerformanceTrends() {
        CachePerformanceAnalyzer.PerformanceReport report = performanceAnalyzer.getPerformanceReport();
        
        Map<String, Object> trends = new HashMap<>();
        trends.put("hourlyTrends", report.getPerformanceTrend());
        trends.put("cacheTypeEfficiency", report.getCacheEfficiency());
        
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/recommendations")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<Map<String, Object>> getRecommendations() {
        CachePerformanceAnalyzer.PerformanceReport report = performanceAnalyzer.getPerformanceReport();
        
        Map<String, Object> response = new HashMap<>();
        response.put("recommendations", report.getRecommendations());
        response.put("basedOnMetrics", Map.of(
                "hitRate", report.getHitRate(),
                "avgResponseTime", report.getAvgResponseTime(),
                "totalRequests", report.getTotalRequests()
        ));
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<Map<String, String>> resetStatistics() {
        performanceAnalyzer.resetStatistics();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Performance statistics reset successfully");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/efficiency/{cacheType}")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<Map<String, Object>> getCacheTypeEfficiency(@PathVariable String cacheType) {
        CachePerformanceAnalyzer.PerformanceReport report = performanceAnalyzer.getPerformanceReport();
        
        if (!report.getCacheEfficiency().containsKey(cacheType)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid cache type: " + cacheType);
            error.put("availableTypes", report.getCacheEfficiency().keySet());
            return ResponseEntity.badRequest().body(error);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("cacheType", cacheType);
        response.put("efficiency", report.getCacheEfficiency().get(cacheType));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<Map<String, Object>> getPerformanceAlerts() {
        CachePerformanceAnalyzer.PerformanceReport report = performanceAnalyzer.getPerformanceReport();
        
        Map<String, Object> alerts = new HashMap<>();
        
        // 检查性能警告
        if (report.getHitRate() < 50) {
            alerts.put("lowHitRate", Map.of(
                    "severity", "HIGH",
                    "message", "Cache hit rate is critically low: " + String.format("%.2f%%", report.getHitRate()),
                    "action", "Consider increasing TTL or reviewing cache strategy"
            ));
        } else if (report.getHitRate() < 70) {
            alerts.put("moderateHitRate", Map.of(
                    "severity", "MEDIUM",
                    "message", "Cache hit rate could be improved: " + String.format("%.2f%%", report.getHitRate()),
                    "action", "Review cache configuration and usage patterns"
            ));
        }
        
        if (report.getAvgResponseTime() > 200) {
            alerts.put("highResponseTime", Map.of(
                    "severity", "HIGH",
                    "message", "Average response time is high: " + report.getAvgResponseTime() + "ms",
                    "action", "Check Redis connection and network latency"
            ));
        } else if (report.getAvgResponseTime() > 100) {
            alerts.put("moderateResponseTime", Map.of(
                    "severity", "MEDIUM",
                    "message", "Average response time is elevated: " + report.getAvgResponseTime() + "ms",
                    "action", "Monitor Redis performance and consider optimization"
            ));
        }
        
        if (report.getTotalEvictions() > 5000) {
            alerts.put("highEvictions", Map.of(
                    "severity", "MEDIUM",
                    "message", "High number of cache evictions: " + report.getTotalEvictions(),
                    "action", "Review cache invalidation strategy"
            ));
        }
        
        if (alerts.isEmpty()) {
            alerts.put("status", "All performance metrics are within normal ranges");
        }
        
        return ResponseEntity.ok(alerts);
    }

    private String getPerformanceStatus(double hitRate, long avgResponseTime) {
        if (hitRate >= 80 && avgResponseTime <= 50) {
            return "EXCELLENT";
        } else if (hitRate >= 70 && avgResponseTime <= 100) {
            return "GOOD";
        } else if (hitRate >= 50 && avgResponseTime <= 200) {
            return "FAIR";
        } else {
            return "POOR";
        }
    }
}