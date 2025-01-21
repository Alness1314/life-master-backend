package com.alness.lifemaster.assistance.dto.request;

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
public class AssistanceRequest {
    private String workDate;
    private String timeEntry;
    private String departureTime;
    private Boolean onTime;
    private Boolean retard;
    private Boolean justifiedAbsence;
    private Boolean unjustifiedAbsence;
}
