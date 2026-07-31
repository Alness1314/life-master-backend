package com.alness.lifemaster.operations.audit;

import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID>,
        JpaSpecificationExecutor<AuditEventEntity> {
    List<AuditEventEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
