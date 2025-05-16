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
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3FileRepository s3FileRepository;

    // Configuración de duración para las URLs pre-firmadas
    private static final Duration DEFAULT_URL_DURATION = Duration.ofDays(3); // 3 días por defecto
    private static final Duration REFRESH_THRESHOLD = Duration.ofHours(12);  // Refrescar si quedan menos de 12 horas

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

    /**
     * Uploads a file to S3 and saves the reference in the database
     */
    public S3File uploadFile(MultipartFile file, EntityType entityType, Long entityId) {
        try {
            // Generate a unique file name
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueName = UUID.randomUUID().toString() + extension;

            // Determine the appropriate S3 path based on entity type
            String s3Key = getS3KeyForEntityType(entityType, uniqueName);

            // Upload to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            // Decide si usar URL pública o pre-firmada
            String fileUrl;
            LocalDateTime expirationTime = null;

            if (entityType != EntityType.USER && isPublicFile(extension)) {
                // Para imágenes y archivos públicos, usar URL pública
                fileUrl = generatePublicUrl(s3Key);
            } else {
                // Para archivos privados o usuarios, usar URL pre-firmada
                fileUrl = generatePresignedUrl(s3Key, DEFAULT_URL_DURATION);
                expirationTime = LocalDateTime.now().plus(DEFAULT_URL_DURATION);
            }

            // Save file reference to database
            S3File s3File = S3File.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .s3Key(s3Key)
                    .s3Url(fileUrl)
                    .urlExpirationTime(expirationTime)
                    .fileName(originalFilename)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .isPublic(entityType != EntityType.USER) // Only user files are private by default
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

    /**
     * Generates a presigned URL for accessing a file in S3 with specified duration
     */
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

    /**
     * Generates a presigned URL with default duration
     */
    public String generatePresignedUrl(String s3Key) {
        return generatePresignedUrl(s3Key, DEFAULT_URL_DURATION);
    }

    /**
     * Generates a public URL for accessing the file in S3
     */
    public String generatePublicUrl(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, region, s3Key);
    }

    /**
     * Refreshes presigned URLs for S3 files
     */
    public S3File refreshPresignedUrl(Long fileId) {
        Optional<S3File> optionalS3File = s3FileRepository.findById(fileId);
        if (optionalS3File.isPresent()) {
            S3File s3File = optionalS3File.get();

            // Si es un archivo público y tiene una extensión de imagen, usar URL pública
            if (s3File.isPublic() && isPublicFile(getFileExtension(s3File.getFileName()))) {
                s3File.setS3Url(generatePublicUrl(s3File.getS3Key()));
                s3File.setUrlExpirationTime(null); // Las URLs públicas no expiran
            } else {
                // Generar nueva URL pre-firmada con la duración predeterminada
                s3File.setS3Url(generatePresignedUrl(s3File.getS3Key(), DEFAULT_URL_DURATION));
                s3File.setUrlExpirationTime(LocalDateTime.now().plus(DEFAULT_URL_DURATION));
            }

            return s3FileRepository.save(s3File);
        } else {
            throw new S3FileException("File not found with ID: " + fileId);
        }
    }

    /**
     * Gets a file by ID, refreshing its URL if needed
     */
    public S3File getFileById(Long fileId) {
        Optional<S3File> optionalS3File = s3FileRepository.findById(fileId);
        if (optionalS3File.isPresent()) {
            S3File s3File = optionalS3File.get();

            // Si es un archivo público y no tiene tiempo de expiración, devolver directamente
            if (s3File.isPublic() && s3File.getUrlExpirationTime() == null) {
                return s3File;
            }

            // Si la URL está por expirar (o no tiene tiempo de expiración definido), refrescarla
            if (s3File.getUrlExpirationTime() == null ||
                    s3File.getUrlExpirationTime().isBefore(LocalDateTime.now().plus(REFRESH_THRESHOLD))) {
                log.debug("URL for file ID {} is expired or will expire soon, refreshing", fileId);
                return refreshPresignedUrl(fileId);
            }

            // La URL sigue siendo válida, devolver el archivo directamente
            return s3File;
        }
        throw new S3FileException("File not found with ID: " + fileId);
    }

    /**
     * Deletes a file from S3 and removes the reference from the database
     */
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

    /**
     * Gets all files associated with an entity
     */
    public List<S3File> getFilesForEntity(EntityType entityType, Long entityId) {
        List<S3File> files = s3FileRepository.findByEntityTypeAndEntityId(entityType, entityId);

        // Verificar y refrescar URLs expiradas
        for (int i = 0; i < files.size(); i++) {
            S3File file = files.get(i);

            // Si no es una URL pública y está por expirar, refrescarla
            if (file.getUrlExpirationTime() != null &&
                    file.getUrlExpirationTime().isBefore(LocalDateTime.now().plus(REFRESH_THRESHOLD))) {
                files.set(i, refreshPresignedUrl(file.getFileId()));
            }
        }

        return files;
    }

    /**
     * Helper method to get the file extension from a filename
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // No extension
        }
        return filename.substring(lastIndexOf).toLowerCase();
    }

    /**
     * Helper method to determine if a file should be publicly accessible based on extension
     */
    boolean isPublicFile(String extension) {
        // Considerar imágenes y otros archivos públicos comunes
        return extension.matches("\\.(jpg|jpeg|png|gif|svg|webp|ico)$");
    }

    /**
     * Helper method to determine the appropriate S3 key based on entity type
     */
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