package com.alness.lifemaster.app.service;

import com.alness.lifemaster.app.dto.ResponseServer;
import com.alness.lifemaster.common.dto.ResponseDto;

import jakarta.servlet.http.HttpServletRequest;

public interface AppConfigService {
    public ResponseDto createDefaultValues();
    public ResponseServer checkStatusSession(HttpServletRequest request);
}
