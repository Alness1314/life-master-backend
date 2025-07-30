package com.alness.lifemaster.exercises.dto;

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
public class ExercisesRequest {
    private String notes;
    private String trainingDate;
    private String startTime;
    private String endTime;
    private String activityType;
    private Integer durationMinutes;
}
