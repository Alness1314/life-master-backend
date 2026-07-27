package com.alness.lifemaster.operations.audit;

import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {
    List<AuditEventEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
