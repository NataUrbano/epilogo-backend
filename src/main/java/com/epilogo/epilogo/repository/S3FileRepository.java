package com.epilogo.epilogo.repository;

import com.epilogo.epilogo.model.S3File;
import com.epilogo.epilogo.model.S3File.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.parser.Entity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface S3FileRepository extends JpaRepository<S3File, Long> {

    List<S3File> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    Optional<S3File> findFirstByEntityIdAndEntityTypeOrderByUploadDateDesc(Long entityId, EntityType entityType);

    List<S3File> findByUrlExpirationTimeBefore(LocalDateTime expirationTime);

    List<S3File> findByUrlExpirationTimeIsNull();

}