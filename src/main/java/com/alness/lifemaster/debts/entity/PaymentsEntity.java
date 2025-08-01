package com.alness.lifemaster.debts.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "payments")
@Data
public class PaymentsEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "payment_date", nullable = false, columnDefinition = "date")
    private LocalDate paymentDate;

    @Column(name = "amount_paid", nullable = false, columnDefinition = "numeric(21,8)")
    private BigDecimal amountPaid;

    @Column(name = "payment_method", nullable = true, columnDefinition = "character varying(256)")
    private String paymentMethod;

    @Column(name = "is_paid", nullable = false, columnDefinition = "boolean")
    private Boolean isPaid;

    @Column(name = "notes", nullable = true, columnDefinition = "text")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "debts_id", nullable = true)
    private DebtsEntity debts;
}
