package com.epilogo.epilogo.service;

import com.epilogo.epilogo.exception.S3FileException;
import com.epilogo.epilogo.model.S3File;
import com.epilogo.epilogo.model.S3File.EntityType;
import com.epilogo.epilogo.repository.S3FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Tag(name = "S3 Service", description = "Servicio para gestión de archivos en Amazon S3")
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3FileRepository s3FileRepository;

    private static final Duration DEFAULT_URL_DURATION = Duration.ofDays(3);
    private static final Duration REFRESH_THRESHOLD = Duration.ofHours(12);

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.books-path}")
    private String booksPath;

    @Value("${aws.s3.authors-path}")
    private String authorsPath;

    @Value("${aws.s3.categories-path}")
    private String categoriesPath;

    @Value("${aws.s3.users-path}")
    private String usersPath;

    @Value("${aws.region}")
    private String region;

    @Operation(summary = "Subir archivo", description = "Sube un archivo a S3 y guarda la referencia en la base de datos")
    public S3File uploadFile(MultipartFile file, EntityType entityType, Long entityId) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueName = UUID.randomUUID().toString() + extension;

            String s3Key = getS3KeyForEntityType(entityType, uniqueName);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            String fileUrl;
            LocalDateTime expirationTime = null;

            if (entityType != EntityType.USER && isPublicFile(extension)) {
                fileUrl = generatePublicUrl(s3Key);
            } else {
                fileUrl = generatePresignedUrl(s3Key, DEFAULT_URL_DURATION);
                expirationTime = LocalDateTime.now().plus(DEFAULT_URL_DURATION);
            }

            S3File s3File = S3File.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .s3Key(s3Key)
                    .s3Url(fileUrl)
                    .urlExpirationTime(expirationTime)
                    .fileName(originalFilename)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .isPublic(entityType != EntityType.USER)
                    .build();

            return s3FileRepository.save(s3File);
        } catch (IOException e) {
            log.error("Error uploading file to S3: {}", e.getMessage());
            throw new S3FileException("Failed to upload file: " + e.getMessage());
        } catch (S3Exception e) {
            log.error("AWS S3 error uploading file: {}", e.getMessage());
            throw new S3FileException("AWS S3 error: " + e.getMessage());
        }
    }

    @Operation(summary = "Generar URL pre-firmada", description = "Genera una URL pre-firmada para acceder a un archivo en S3 con duración específica")
    public String generatePresignedUrl(String s3Key, Duration duration) {
        try {
            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Key)
                            .build())
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
            URL url = presignedGetObjectRequest.url();
            return url.toString();
        } catch (Exception e) {
            log.error("Error generating presigned URL: {}", e.getMessage());
            throw new S3FileException("Failed to generate presigned URL: " + e.getMessage());
        }
    }

    @Operation(summary = "Generar URL pre-firmada con duración predeterminada", description = "Genera una URL pre-firmada para acceder a un archivo en S3")
    public String generatePresignedUrl(String s3Key) {
        return generatePresignedUrl(s3Key, DEFAULT_URL_DURATION);
    }

    @Operation(summary = "Generar URL pública", description = "Genera una URL pública para acceder a un archivo en S3")
    public String generatePublicUrl(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, region, s3Key);
    }

    @Operation(summary = "Refrescar URL pre-firmada", description = "Refresca la URL pre-firmada de un archivo S3")
    public S3File refreshPresignedUrl(Long fileId) {
        Optional<S3File> optionalS3File = s3FileRepository.findById(fileId);
        if (optionalS3File.isPresent()) {
            S3File s3File = optionalS3File.get();

            if (s3File.isPublic() && isPublicFile(getFileExtension(s3File.getFileName()))) {
                s3File.setS3Url(generatePublicUrl(s3File.getS3Key()));
                s3File.setUrlExpirationTime(null);
            } else {
                s3File.setS3Url(generatePresignedUrl(s3File.getS3Key(), DEFAULT_URL_DURATION));
                s3File.setUrlExpirationTime(LocalDateTime.now().plus(DEFAULT_URL_DURATION));
            }

            return s3FileRepository.save(s3File);
        } else {
            throw new S3FileException("File not found with ID: " + fileId);
        }
    }

    @Operation(summary = "Obtener archivo por ID", description = "Obtiene un archivo por su ID, refrescando su URL si es necesario")
    public S3File getFileById(Long fileId) {
        Optional<S3File> optionalS3File = s3FileRepository.findById(fileId);
        if (optionalS3File.isPresent()) {
            S3File s3File = optionalS3File.get();

            if (s3File.isPublic() && s3File.getUrlExpirationTime() == null) {
                return s3File;
            }

            if (s3File.getUrlExpirationTime() == null ||
                    s3File.getUrlExpirationTime().isBefore(LocalDateTime.now().plus(REFRESH_THRESHOLD))) {
                log.debug("URL for file ID {} is expired or will expire soon, refreshing", fileId);
                return refreshPresignedUrl(fileId);
            }

            return s3File;
        }
        throw new S3FileException("File not found with ID: " + fileId);
    }

    @Operation(summary = "Eliminar archivo", description = "Elimina un archivo de S3 y elimina la referencia de la base de datos")
    public void deleteFile(Long fileId) {
        Optional<S3File> optionalS3File = s3FileRepository.findById(fileId);
        if (optionalS3File.isPresent()) {
            S3File s3File = optionalS3File.get();
            try {
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3File.getS3Key())
                        .build();

                s3Client.deleteObject(deleteObjectRequest);
                s3FileRepository.delete(s3File);
            } catch (S3Exception e) {
                log.error("AWS S3 error deleting file: {}", e.getMessage());
                throw new S3FileException("AWS S3 error: " + e.getMessage());
            }
        } else {
            throw new S3FileException("File not found with ID: " + fileId);
        }
    }

    @Operation(summary = "Obtener archivos para entidad", description = "Obtiene todos los archivos asociados a una entidad")
    public List<S3File> getFilesForEntity(EntityType entityType, Long entityId) {
        List<S3File> files = s3FileRepository.findByEntityTypeAndEntityId(entityType, entityId);

        for (int i = 0; i < files.size(); i++) {
            S3File file = files.get(i);

            if (file.getUrlExpirationTime() != null &&
                    file.getUrlExpirationTime().isBefore(LocalDateTime.now().plus(REFRESH_THRESHOLD))) {
                files.set(i, refreshPresignedUrl(file.getFileId()));
            }
        }

        return files;
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return filename.substring(lastIndexOf).toLowerCase();
    }

    boolean isPublicFile(String extension) {
        return extension.matches("\\.(jpg|jpeg|png|gif|svg|webp|ico)$");
    }

    private String getS3KeyForEntityType(EntityType entityType, String uniqueFileName) {
        switch (entityType) {
            case BOOK:
                return booksPath + uniqueFileName;
            case AUTHOR:
                return authorsPath + uniqueFileName;
            case CATEGORY:
                return categoriesPath + uniqueFileName;
            case USER:
                return usersPath + uniqueFileName;
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
    }
}