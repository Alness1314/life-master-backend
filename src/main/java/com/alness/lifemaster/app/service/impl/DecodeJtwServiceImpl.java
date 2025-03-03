package com.alness.lifemaster.app.service.impl;

import org.apache.commons.codec.binary.Base64;

import org.springframework.stereotype.Service;

import com.alness.lifemaster.app.dto.BodyDto;
import com.alness.lifemaster.app.dto.HeaderDto;
import com.alness.lifemaster.app.dto.JwtDto;
import com.alness.lifemaster.app.service.DecodeJwtService;
import com.google.gson.Gson;

@Service
public class DecodeJtwServiceImpl implements DecodeJwtService{

    @Override
    public JwtDto decodeJwt(String jwtToken) {
        String[] parts = jwtToken.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format. It should have three parts separated by dots.");
        }

        Base64 base64Url = new Base64(true);
        Gson gson = new Gson();

        String headerJson = new String(base64Url.decode(parts[0]));
       
        String bodyJson = new String(base64Url.decode(parts[1]));
       

        HeaderDto headerDto = gson.fromJson(headerJson, HeaderDto.class);
        BodyDto bodyDto = gson.fromJson(bodyJson, BodyDto.class);

        return new JwtDto(headerDto, bodyDto);
    }

    @Override
    public Boolean isValidToken(String jwtToken) {
        JwtDto jwtDto = decodeJwt(jwtToken);
        return compareUnixTime(unixTimeNow(), jwtDto.getBody().getExp());
    }

    private Boolean compareUnixTime(Long unixTimeNow, Long unixTimeIn) {
        return unixTimeIn > unixTimeNow;
    }

    private Long unixTimeNow() {
        return System.currentTimeMillis() / 1000;
    }
    
}
