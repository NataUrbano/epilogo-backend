package com.epilogo.epilogo.service;

import com.epilogo.epilogo.model.S3File;
import com.epilogo.epilogo.repository.S3FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3UrlRefreshService {

    private final S3FileRepository s3FileRepository;
    private final S3Service s3Service;

    /**
     * Ejecuta cada 12 horas la actualización de URLs que están por expirar
     * El valor está en milisegundos: 12 horas = 12 * 60 * 60 * 1000 = 43200000
     */
    @Scheduled(fixedRate = 43200000)
    @Transactional
    public void refreshExpiringUrls() {
        log.info("Starting scheduled refresh of expiring S3 presigned URLs");

        try {
            // Buscar URLs que expiran en las próximas 24 horas
            LocalDateTime expirationThreshold = LocalDateTime.now().plusHours(24);
            List<S3File> expiringFiles = s3FileRepository.findByUrlExpirationTimeBefore(expirationThreshold);

            log.info("Found {} S3 files with URLs about to expire", expiringFiles.size());

            int refreshedCount = 0;
            int errorCount = 0;

            for (S3File file : expiringFiles) {
                try {

                    s3Service.refreshPresignedUrl(file.getFileId());
                    refreshedCount++;

                    // Un pequeño retraso para no sobrecargar el servicio de S3
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

    /**
     * También revisa archivos sin tiempo de expiración definido
     * Ejecutar una vez al día (86400000 ms)
     */
    @Scheduled(fixedRate = 86400000, initialDelay = 3600000) // Empezar tras 1 hora del inicio
    @Transactional
    public void checkMissingExpirationTimes() {
        log.info("Checking for S3 files without expiration times");

        try {
            List<S3File> filesWithoutExpiration = s3FileRepository.findByUrlExpirationTimeIsNull();
            log.info("Found {} S3 files without expiration time", filesWithoutExpiration.size());

            int updatedCount = 0;

            for (S3File file : filesWithoutExpiration) {
                try {
                    // Si es un archivo público con contenido de imagen, dejarlo sin tiempo de expiración
                    if (file.isPublic() && isPublicFile(getFileExtension(file.getFileName()))) {
                        // Asegurarse de que use URL pública en lugar de pre-firmada
                        file.setS3Url(s3Service.generatePublicUrl(file.getS3Key()));
                        s3FileRepository.save(file);
                    } else {
                        // Para otros archivos, establecer la URL pre-firmada y tiempo de expiración
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
     * Helper method to check if file is public based on extension
     */
    private boolean isPublicFile(String extension) {
        return extension.matches("\\.(jpg|jpeg|png|gif|svg|webp|ico)$");
    }
}