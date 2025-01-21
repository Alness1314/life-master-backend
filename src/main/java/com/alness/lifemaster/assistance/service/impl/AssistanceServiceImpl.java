package com.alness.lifemaster.assistance.service.impl;

import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alness.lifemaster.assistance.dto.request.AssistanceRequest;
import com.alness.lifemaster.assistance.dto.response.AssistanceResponse;
import com.alness.lifemaster.assistance.entity.AssistanceEntity;
import com.alness.lifemaster.assistance.repository.AssistanceRepository;
import com.alness.lifemaster.assistance.service.AssistanceService;
import com.alness.lifemaster.common.dto.ResponseDto;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.utils.ApiCodes;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AssistanceServiceImpl implements AssistanceService {
    @Autowired
    private AssistanceRepository assistanceRepository;

    ModelMapper modelMapper = new ModelMapper();

    @PostConstruct
    public void init() {
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.getConfiguration().setFieldMatchingEnabled(true);
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    @Override
    public AssistanceResponse findOne(String id) {
        AssistanceEntity assistance = assistanceRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        "Entity not found"));
        return mapperDto(assistance);

    }

    @Override
    public List<AssistanceResponse> find() {
        return assistanceRepository.findAll()
                .stream().map(this::mapperDto)
                .toList();
    }

    @Override
    public AssistanceResponse save(AssistanceRequest request) {
        try {
            AssistanceEntity newAssistance = modelMapper.map(request, AssistanceEntity.class);
            newAssistance = assistanceRepository.save(newAssistance);
            return mapperDto(newAssistance);
        } catch (Exception e) {
            log.error("Error {}", e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
            "Error to save assistance");
        }
    }

    @Override
    public AssistanceResponse update(String id, AssistanceRequest request) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public ResponseDto delete(String id) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    private AssistanceResponse mapperDto(AssistanceEntity source) {
        return modelMapper.map(source, AssistanceResponse.class);
    }
}
