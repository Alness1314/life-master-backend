package com.alness.lifemaster.nutrition.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class NutritionRequest {
    @NotBlank
    private String dateTimeConsumption;

    @Valid
    @Size(max = 50)
    private List<FoodRequest> food;

    @NotBlank
    @Size(max = 256)
    private String name;

    @NotBlank
    @Size(max = 128)
    private String mealType;

    @Size(max = 4000)
    private String notes;

    private Boolean removePhoto;
}
