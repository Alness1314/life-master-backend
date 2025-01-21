package com.alness.lifemaster.vault.dto.request;

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
public class VaultRequest {
    private String siteName;
    private String siteUrl;
    private String username;
    private String passwordEncrypted;
    private String notes;
}
