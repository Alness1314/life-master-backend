package com.alness.lifemaster.app.service;

import com.alness.lifemaster.app.dto.JwtDto;

public interface DecodeJwtService {
    public JwtDto decodeJwt(String jwtToken);
    public Boolean isValidToken(String jwtToken);
}
