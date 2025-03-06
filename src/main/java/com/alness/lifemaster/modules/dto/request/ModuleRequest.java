package com.alness.lifemaster.modules.dto.request;

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
public class ModuleRequest {
    private String name;
    private String route;
    private String iconName;
    private String level;
    private String description;
    private Boolean isParent;
    private List<String> profile;
}
