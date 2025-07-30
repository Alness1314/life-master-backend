package com.alness.lifemaster.exercises.dto;

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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExercisesResponse {     
    private UUID id;
    private String notes;
    private LocalDate trainingDate;
    private String startTime;
    private String endTime;
    private String activityType;
    private Integer durationMinutes;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private Boolean erased;
}
