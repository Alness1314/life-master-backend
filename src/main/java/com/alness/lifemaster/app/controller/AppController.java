package com.alness.lifemaster.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

import com.alness.lifemaster.app.dto.ResponseServer;
import com.alness.lifemaster.app.service.AppConfigService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class AppController {
    @Autowired
    private AppConfigService appConfigService;

    @GetMapping("/")
    public RedirectView redirectToSwagger(HttpServletRequest request) {
        log.info("Request URL: {}", request.getRequestURL());
        log.info("Request Method: {}", request.getMethod());
        log.info("Client IP: {}", request.getRemoteAddr());
        log.info("User-Agent: {}", request.getHeader("User-Agent"));
        return new RedirectView("/swagger-ui/index.html");
    }

    @GetMapping("/api/v1/auth/check-session")
    public ResponseEntity<ResponseServer> checkSession(HttpServletRequest request) {
        ResponseServer response = appConfigService.checkStatusSession(request);
        return new ResponseEntity<>(response, response.getCode());
    }
}
