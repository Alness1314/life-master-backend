package com.alness.lifemaster.debts.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.alness.lifemaster.users.entity.UserEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "debts")
@Data
public class DebtsEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "creditor_name", nullable = false, columnDefinition = "character varying(256)")
    private String creditorName;

    @Column(name = "total_amount", nullable = false, columnDefinition = "numeric(21,8)")
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false, columnDefinition = "character varying(128)")
    private String currency;

    @Column(name = "has_interest", nullable = false, columnDefinition = "boolean")
    private Boolean hasInterest;

    @Column(name = "number_of_payments", nullable = false, columnDefinition = "bigint")
    private Integer numberOfPayments;

    @Column(name = "payments_made", nullable = false, columnDefinition = "bigint")
    private Integer paymentsMade;

    @Column(name = "due_date", nullable = false, columnDefinition = "date")
    private LocalDate dueDate;

    @Column(name = "is_fully_paid", nullable = false, columnDefinition = "boolean")
    private Boolean isFullyPaid;

    @Column(name = "notes", nullable = true, columnDefinition = "text")
    private String notes;

    @OneToMany(mappedBy = "debts", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<PaymentsEntity> payments;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "create_at", nullable = false, columnDefinition = "timestamp without time zone")
    private LocalDateTime createAt;

    @Column(name = "update_at", nullable = false, updatable = true, columnDefinition = "timestamp without time zone")
    private LocalDateTime updateAt;

    @Column(nullable = false, columnDefinition = "boolean")
    private Boolean erased;

    @PrePersist()
    public void init() {
        setCreateAt(LocalDateTime.now());
        setUpdateAt(LocalDateTime.now());
        setErased(false);
    }

    @PreUpdate
    private void preUpdate() {
        setUpdateAt(LocalDateTime.now());
    }

}
