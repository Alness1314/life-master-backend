package com.alness.lifemaster.files;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFileEntity, UUID> {
    Optional<StoredFileEntity> findByIdAndUserIdAndErasedFalse(UUID id, UUID userId);
    List<StoredFileEntity> findAllByUserIdAndErasedFalseOrderByCreatedAtDesc(UUID userId);
    boolean existsByIdAndUserIdAndErasedFalse(UUID id, UUID userId);
}
