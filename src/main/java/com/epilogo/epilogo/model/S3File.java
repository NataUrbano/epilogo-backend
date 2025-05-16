package com.epilogo.epilogo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "s3_files")
public class S3File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "entity_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "s3_url", nullable = false, length = 500)
    private String s3Url;

    @Column(name = "s3_key", nullable = false, length = 500)
    private String s3Key;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "upload_date")
    @CreationTimestamp
    private LocalDateTime uploadDate;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    // Nuevo campo para controlar la expiración de la URL
    @Column(name = "url_expiration_time")
    private LocalDateTime urlExpirationTime;

    public enum EntityType {
        USER, BOOK, AUTHOR, CATEGORY
    }

    @Override
    public String toString() {
        return "S3File{" +
                "fileId=" + fileId +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", s3Key='" + s3Key + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileSize=" + fileSize +
                ", uploadDate=" + uploadDate +
                ", isPublic=" + isPublic +
                ", urlExpirationTime=" + urlExpirationTime +
                '}';
    }
}