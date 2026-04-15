package com.blog.dto;

import java.util.List;

public class SyncResultResponse {
    private int created;
    private int updated;
    private int skipped;
    private List<String> errors;

    public SyncResultResponse() {}

    public SyncResultResponse(int created, int updated, int skipped, List<String> errors) {
        this.created = created;
        this.updated = updated;
        this.skipped = skipped;
        this.errors = errors;
    }

    public int getCreated() { return created; }
    public int getUpdated() { return updated; }
    public int getSkipped() { return skipped; }
    public List<String> getErrors() { return errors; }
}
