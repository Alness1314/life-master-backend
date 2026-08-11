package com.alness.lifemaster.operations.report.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alness.lifemaster.operations.report.ReportRange;

public record NutritionReportResponse(
        ReportRange range,
        long meals,
        long foods,
        long foodsWithCalories,
        long totalKnownCalories,
        Map<String, Long> mealsByType,
        List<Item> items) {

    public record Item(
            UUID id,
            LocalDateTime consumedAt,
            String name,
            String mealType,
            String notes,
            UUID photoId,
            List<FoodItem> foods) {
    }

    public record FoodItem(
            UUID id,
            String name,
            Integer calories,
            String quantity,
            String unitMeasurement) {
    }
}
