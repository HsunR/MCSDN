package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CsdnSyncConfigRequest {
    @NotBlank(message = "CSDN userId is required")
    private String csdnUserId;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private Boolean enabled;

    public String getCsdnUserId() { return csdnUserId; }
    public void setCsdnUserId(String csdnUserId) { this.csdnUserId = csdnUserId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
