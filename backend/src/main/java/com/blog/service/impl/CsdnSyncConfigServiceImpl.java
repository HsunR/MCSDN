package com.blog.service.impl;

import com.blog.dto.CsdnSyncConfigRequest;
import com.blog.entity.CsdnSyncConfig;
import com.blog.mapper.CsdnSyncConfigMapper;
import com.blog.service.CsdnSyncConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CsdnSyncConfigServiceImpl implements CsdnSyncConfigService {

    @Autowired
    private CsdnSyncConfigMapper csdnSyncConfigMapper;

    @Override
    public CsdnSyncConfig getConfig() {
        return csdnSyncConfigMapper.findLatest();
    }

    @Override
    @Transactional
    public CsdnSyncConfig saveConfig(CsdnSyncConfigRequest request) {
        CsdnSyncConfig existing = csdnSyncConfigMapper.findLatest();
        if (existing != null) {
            existing.setCsdnUserId(request.getCsdnUserId());
            existing.setCategoryId(request.getCategoryId());
            existing.setEnabled(request.getEnabled() != null ? request.getEnabled() : false);
            csdnSyncConfigMapper.update(existing);
            return existing;
        } else {
            CsdnSyncConfig config = new CsdnSyncConfig();
            config.setCsdnUserId(request.getCsdnUserId());
            config.setCategoryId(request.getCategoryId());
            config.setEnabled(request.getEnabled() != null ? request.getEnabled() : false);
            csdnSyncConfigMapper.insert(config);
            return config;
        }
    }

    @Override
    @Transactional
    public void updateLastSyncAt(LocalDateTime lastSyncAt) {
        csdnSyncConfigMapper.updateLastSyncAt(lastSyncAt);
    }
}
