package com.alness.lifemaster.income.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IncomeResponse {
    private UUID id;
    private String source;
    private String description;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private Boolean erased;
}
