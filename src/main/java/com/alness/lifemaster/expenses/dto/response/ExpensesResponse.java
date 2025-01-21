package com.alness.lifemaster.expenses.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.alness.lifemaster.categories.dto.response.CategoryResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpensesResponse {
    private UUID id;
    private String bankOrEntity;
    private String description;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private CategoryResponse category;
    private Boolean paymentStatus;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private Boolean erased;
}
