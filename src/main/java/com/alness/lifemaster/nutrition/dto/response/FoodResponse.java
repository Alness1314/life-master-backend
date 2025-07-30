package com.alness.lifemaster.nutrition.dto.response;

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
public class FoodResponse {
    private UUID id;
    private String foodName;
    private Integer calories;
    private String unitMeasurement;
    private String quantity;
}
