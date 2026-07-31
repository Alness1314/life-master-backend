package com.alness.lifemaster.nutrition.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
public class FoodRequest {
    @NotBlank
    @Size(max = 256)
    private String foodName;

    @PositiveOrZero
    private Integer calories;

    @Size(max = 128)
    private String unitMeasurement;

    @NotBlank
    @Size(max = 256)
    private String quantity;
}
