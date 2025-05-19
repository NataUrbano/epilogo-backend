package com.epilogo.epilogo.service;

import com.epilogo.epilogo.model.S3File;
import com.epilogo.epilogo.repository.S3FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Tag(name = "S3 URL Refresh Service", description = "Servicio para actualización automática de URLs pre-firmadas de S3")
public class S3UrlRefreshService {

    private final S3FileRepository s3FileRepository;
    private final S3Service s3Service;

    @Scheduled(fixedRate = 43200000)
    @Transactional
    @Operation(summary = "Refrescar URLs que expiran", description = "Tarea programada que actualiza las URLs pre-firmadas que están por expirar en las próximas 24 horas")
    @Hidden
    public void refreshExpiringUrls() {
        log.info("Starting scheduled refresh of expiring S3 presigned URLs");

        try {
            LocalDateTime expirationThreshold = LocalDateTime.now().plusHours(24);
            List<S3File> expiringFiles = s3FileRepository.findByUrlExpirationTimeBefore(expirationThreshold);

            log.info("Found {} S3 files with URLs about to expire", expiringFiles.size());

            int refreshedCount = 0;
            int errorCount = 0;

            for (S3File file : expiringFiles) {
                try {
                    s3Service.refreshPresignedUrl(file.getFileId());
                    refreshedCount++;

                    if (refreshedCount % 50 == 0) {
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    log.error("Error refreshing presigned URL for file ID {}: {}", file.getFileId(), e.getMessage());
                    errorCount++;
                }
            }

            log.info("Completed scheduled refresh of S3 presigned URLs. Refreshed: {}, Errors: {}",
                    refreshedCount, errorCount);
        } catch (Exception e) {
            log.error("Error during scheduled URL refresh: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedRate = 86400000, initialDelay = 3600000)
    @Transactional
    @Operation(summary = "Verificar archivos sin tiempo de expiración", description = "Tarea programada que verifica y actualiza archivos que no tienen tiempo de expiración definido")
    @Hidden
    public void checkMissingExpirationTimes() {
        log.info("Checking for S3 files without expiration times");

        try {
            List<S3File> filesWithoutExpiration = s3FileRepository.findByUrlExpirationTimeIsNull();
            log.info("Found {} S3 files without expiration time", filesWithoutExpiration.size());

            int updatedCount = 0;

            for (S3File file : filesWithoutExpiration) {
                try {
                    if (file.isPublic() && isPublicFile(getFileExtension(file.getFileName()))) {
                        file.setS3Url(s3Service.generatePublicUrl(file.getS3Key()));
                        s3FileRepository.save(file);
                    } else {
                        s3Service.refreshPresignedUrl(file.getFileId());
                    }

                    updatedCount++;
                } catch (Exception e) {
                    log.error("Error updating S3 file ID {}: {}", file.getFileId(), e.getMessage());
                }
            }

            log.info("Updated {} S3 files with missing expiration times", updatedCount);
        } catch (Exception e) {
            log.error("Error checking files without expiration times: {}", e.getMessage(), e);
        }
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

    private boolean isPublicFile(String extension) {
        return extension.matches("\\.(jpg|jpeg|png|gif|svg|webp|ico)$");
    }
}