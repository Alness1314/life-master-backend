package com.alness.lifemaster.modules.dto;

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
public class ModuleDto {
    private String id;
    private String name;
    private String route;
    private String iconName;
    private String level;
    private String description;
    private Boolean erased;
    private Boolean isParent;
    
}
