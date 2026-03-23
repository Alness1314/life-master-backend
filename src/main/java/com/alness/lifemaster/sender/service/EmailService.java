package com.alness.lifemaster.sender.service;

import com.alness.lifemaster.common.dto.ResponseDto;
import com.alness.lifemaster.sender.dto.EmailRequest;

public interface EmailService {
    public ResponseDto sendEmail(EmailRequest request);
}
