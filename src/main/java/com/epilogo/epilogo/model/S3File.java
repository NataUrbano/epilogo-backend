package com.epilogo.epilogo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "s3_files")
@Schema(description = "Entidad que representa un archivo almacenado en S3")
public class S3File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    @Schema(description = "Identificador único del archivo", example = "1")
    private Long fileId;

    @Column(name = "entity_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Tipo de entidad a la que está asociado el archivo", example = "BOOK", required = true)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    @Schema(description = "ID de la entidad a la que está asociado el archivo", example = "123", required = true)
    private Long entityId;

    @Column(name = "s3_url", nullable = false, length = 500)
    @Schema(description = "URL de acceso al archivo en S3", example = "https://epilogo-bucket.s3.amazonaws.com/books/sample.pdf", required = true)
    private String s3Url;

    @Column(name = "s3_key", nullable = false, length = 500)
    @Schema(description = "Clave del archivo en S3", example = "books/123/sample.pdf", required = true)
    private String s3Key;

    @Column(name = "file_name", length = 255)
    @Schema(description = "Nombre original del archivo", example = "mi-libro.pdf")
    private String fileName;

    @Column(name = "file_type", length = 100)
    @Schema(description = "Tipo MIME del archivo", example = "application/pdf")
    private String fileType;

    @Column(name = "file_size")
    @Schema(description = "Tamaño del archivo en bytes", example = "1048576")
    private Long fileSize;

    @Column(name = "upload_date")
    @CreationTimestamp
    @Schema(description = "Fecha y hora de carga del archivo", example = "2023-05-20T15:30:45")
    private LocalDateTime uploadDate;

    @Column(name = "is_public", nullable = false)
    @Schema(description = "Indica si el archivo es de acceso público", example = "true")
    private boolean isPublic;

    @Column(name = "thumbnail_url", length = 500)
    @Schema(description = "URL de la miniatura del archivo (si aplica)", example = "https://epilogo-bucket.s3.amazonaws.com/thumbnails/123.jpg")
    private String thumbnailUrl;

    @Column(name = "url_expiration_time")
    @Schema(description = "Fecha y hora de expiración de la URL pre-firmada", example = "2023-05-23T15:30:45")
    private LocalDateTime urlExpirationTime;

    @Schema(description = "Tipos de entidades que pueden tener archivos asociados")
    public enum EntityType {
        @Schema(description = "Archivo asociado a un usuario")
        USER,
        @Schema(description = "Archivo asociado a un libro")
        BOOK,
        @Schema(description = "Archivo asociado a un autor")
        AUTHOR,
        @Schema(description = "Archivo asociado a una categoría")
        CATEGORY
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