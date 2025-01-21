package com.alness.lifemaster.vault.dto.response;

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
public class VaultResponse {
    private UUID id;
    private String siteName;
    private String siteUrl;
    private String username;
    private String passwordEncrypted;
    private String notes;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private Boolean erased;
}
