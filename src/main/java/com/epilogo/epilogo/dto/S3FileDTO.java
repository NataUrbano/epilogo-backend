package com.epilogo.epilogo.dto;

import com.epilogo.epilogo.model.S3File.EntityType;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(name = "S3FileResponse", description = "Respuesta con la información de un archivo almacenado en S3")
    public static class S3FileResponse {
        @Schema(description = "Identificador único del archivo", example = "456")
        private Long fileId;

        @Schema(description = "Tipo de entidad asociada al archivo", example = "USER")
        private EntityType entityType;

        @Schema(description = "Identificador de la entidad asociada", example = "123")
        private Long entityId;

        @Schema(description = "URL pública o privada del archivo en S3", example = "https://s3.amazonaws.com/bucket/file.jpg")
        private String s3Url;

        @Schema(description = "Nombre original del archivo", example = "foto_perfil.jpg")
        private String fileName;

        @Schema(description = "Tipo MIME del archivo", example = "image/jpeg")
        private String fileType;

        @Schema(description = "Tamaño del archivo en bytes", example = "204800")
        private Long fileSize;

        @Schema(description = "Fecha y hora en que se subió el archivo", example = "2024-05-18T16:50:00", format = "date-time")
        private LocalDateTime uploadDate;

        @Schema(description = "Indica si el archivo es público", example = "true")
        private Boolean isPublic;

        @Schema(description = "URL de la miniatura del archivo, si aplica", example = "https://s3.amazonaws.com/bucket/thumb_file.jpg")
        private String thumbnailUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "S3FileUploadRequest", description = "Solicitud para subir un archivo a S3")
    public static class S3FileUploadRequest {
        @Schema(description = "Tipo de entidad a la que se asocia el archivo", example = "USER", required = true)
        private EntityType entityType;

        @Schema(description = "ID de la entidad asociada", example = "123", required = true)
        private Long entityId;

        @Schema(description = "Indica si el archivo será público", example = "false")
        private Boolean isPublic;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "S3FileUpdateRequest", description = "Solicitud para actualizar propiedades de un archivo en S3")
    public static class S3FileUpdateRequest {
        @Schema(description = "Indica si el archivo es público", example = "true")
        private Boolean isPublic;
    }
}
