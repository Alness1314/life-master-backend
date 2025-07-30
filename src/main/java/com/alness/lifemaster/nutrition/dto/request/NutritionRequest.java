package com.alness.lifemaster.nutrition.dto.request;

import java.util.List;

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
public class NutritionRequest {
    private String dateTimeConsumption;
    private List<FoodRequest> food;
    private String mealType;
    private String notes;
}
