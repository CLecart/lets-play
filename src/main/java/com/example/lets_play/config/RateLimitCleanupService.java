package com.example.lets_play.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RateLimitCleanupService {

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    // Nettoie les anciennes entrées toutes les 5 minutes
    @Scheduled(fixedRate = 300000) // 5 minutes en millisecondes
    public void cleanupOldRateLimitEntries() {
        rateLimitingFilter.cleanupOldEntries();
    }
}