package com.epilogo.epilogo.dto;

import com.epilogo.epilogo.model.S3File.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class S3FileDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class S3FileResponse {
        private Long fileId;
        private EntityType entityType;
        private Long entityId;
        private String s3Url;
        private String fileName;
        private String fileType;
        private Long fileSize;
        private LocalDateTime uploadDate;
        private Boolean isPublic;
        private String thumbnailUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class S3FileUploadRequest {
        private EntityType entityType;
        private Long entityId;
        private Boolean isPublic;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class S3FileUpdateRequest {
        private Boolean isPublic;
    }
}