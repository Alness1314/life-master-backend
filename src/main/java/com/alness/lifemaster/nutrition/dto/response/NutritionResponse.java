package com.alness.lifemaster.nutrition.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
public class NutritionResponse {
    private UUID id;
    private LocalDateTime dateTimeConsumption;
    private List<FoodResponse> food;
    private String mealType;
    private String notes;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private Boolean erased;
}
