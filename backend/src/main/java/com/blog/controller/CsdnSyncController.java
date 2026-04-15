package com.blog.controller;

import com.blog.dto.CsdnSyncConfigRequest;
import com.blog.dto.SyncResultResponse;
import com.blog.entity.CsdnSyncConfig;
import com.blog.service.CsdnSyncConfigService;
import com.blog.service.CsdnSyncService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/csdn-sync")
public class CsdnSyncController {

    private static final Logger log = LoggerFactory.getLogger(CsdnSyncController.class);

    @Autowired
    private CsdnSyncConfigService csdnSyncConfigService;

    @Autowired
    private CsdnSyncService csdnSyncService;

    /**
     * Get current CSDN sync configuration.
     * GET /api/admin/csdn-sync/config
     */
    @GetMapping("/config")
    public ResponseEntity<CsdnSyncConfig> getConfig() {
        CsdnSyncConfig config = csdnSyncConfigService.getConfig();
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(config);
    }

    /**
     * Save CSDN sync configuration.
     * POST /api/admin/csdn-sync/config
     * Body: CsdnSyncConfigRequest with csdnUserId, categoryId, enabled
     */
    @PostMapping("/config")
    public ResponseEntity<CsdnSyncConfig> saveConfig(@Valid @RequestBody CsdnSyncConfigRequest request) {
        log.info("Saving CSDN sync config: userId={}, categoryId={}, enabled={}",
                 request.getCsdnUserId(), request.getCategoryId(), request.getEnabled());
        CsdnSyncConfig saved = csdnSyncConfigService.saveConfig(request);
        return ResponseEntity.ok(saved);
    }

    /**
     * Trigger CSDN article synchronization.
     * POST /api/admin/csdn-sync/sync
     * Returns: SyncResultResponse with created/updated/skipped/error counts
     */
    @PostMapping("/sync")
    public ResponseEntity<SyncResultResponse> syncArticles() {
        log.info("CSDN sync triggered via API");
        SyncResultResponse result = csdnSyncService.syncArticles();
        return ResponseEntity.ok(result);
    }
}
