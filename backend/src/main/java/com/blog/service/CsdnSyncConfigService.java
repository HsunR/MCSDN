package com.blog.service;

import com.blog.dto.CsdnSyncConfigRequest;
import com.blog.entity.CsdnSyncConfig;

import java.time.LocalDateTime;

public interface CsdnSyncConfigService {
    CsdnSyncConfig getConfig();
    CsdnSyncConfig saveConfig(CsdnSyncConfigRequest request);
    void updateLastSyncAt(LocalDateTime lastSyncAt);
}
