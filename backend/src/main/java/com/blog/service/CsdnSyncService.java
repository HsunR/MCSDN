package com.blog.service;

import com.blog.dto.SyncResultResponse;

/**
 * Synchronizes articles from CSDN blog.
 * Orchestrates fetch -> parse -> dedup -> create/update.
 */
public interface CsdnSyncService {
    /**
     * Synchronizes all articles from configured CSDN blog.
     * Creates new articles, updates changed articles, skips unchanged.
     * Runs synchronously and returns result counts per D-05.
     * @return Sync result with created/updated/skipped counts
     */
    SyncResultResponse syncArticles();
}
