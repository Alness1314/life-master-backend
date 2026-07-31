package com.alness.lifemaster.files;

import java.time.LocalDateTime;
import java.util.UUID;

public record StoredFileResponse(UUID id, String originalName, String contentType, Long sizeBytes,
        String sha256, FilePurpose purpose, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
