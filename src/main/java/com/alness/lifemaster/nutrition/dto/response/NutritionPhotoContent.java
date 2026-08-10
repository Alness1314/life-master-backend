package com.alness.lifemaster.nutrition.dto.response;

import java.nio.file.Path;

public record NutritionPhotoContent(Path path, String contentType, String originalName) {
}
