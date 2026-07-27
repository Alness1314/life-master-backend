package com.alness.lifemaster.finance.paymentmethod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethodEntity, UUID> {
    List<PaymentMethodEntity> findAllByUserIdAndErasedFalseOrderByName(UUID userId);
    Optional<PaymentMethodEntity> findByIdAndUserIdAndErasedFalse(UUID id, UUID userId);
    Optional<PaymentMethodEntity> findByUserIdAndNameIgnoreCaseAndErasedFalse(UUID userId, String name);
}
