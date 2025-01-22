package com.alness.lifemaster.notes.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alness.lifemaster.common.dto.ResponseDto;
import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.notes.dto.request.NotesRequest;
import com.alness.lifemaster.notes.dto.response.NotesResponse;
import com.alness.lifemaster.notes.entity.NotesEntity;
import com.alness.lifemaster.notes.repository.NotesRepository;
import com.alness.lifemaster.notes.service.NotesService;
import com.alness.lifemaster.notes.specification.NotesSpecification;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotesServiceImpl implements NotesService {
    @Autowired
    private NotesRepository notesRepository;

    @Autowired
    private UserRepository userRepository;

    private ModelMapper mapper = new ModelMapper();

    @PostConstruct
    public void init() {
        configureModelMapper();
    }

    private void configureModelMapper() {
        mapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true)
                .setMatchingStrategy(MatchingStrategies.STRICT);
    }

    @Override
    public List<NotesResponse> find(String userId, Map<String, String> params) {
        params.put("user", userId);
        return notesRepository.findAll(filterWithParameters(params)).stream()
                .map(this::mapperDto)
                .toList();
    }

    @Override
    public NotesResponse findOne(String userId, String id) {
        NotesEntity note = notesRepository.findOne(filterWithParameters(Map.of("id", id, "user", userId)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        "Notes not found."));
        return mapperDto(note);
    }

    @Override
    public NotesResponse save(String userId, NotesRequest request) {
        NotesEntity newNote = mapper.map(request, NotesEntity.class);
        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(
                        () -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND, "User not found."));
        newNote.setUser(user);
        try {
            newNote = notesRepository.save(newNote);
        } catch (Exception e) {
            log.error("Error to save note {}", e.getMessage());
            e.printStackTrace();
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error to save note.");
        }
        return mapperDto(newNote);
    }

    @Override
    public NotesResponse update(String userId, String id, NotesRequest request) {
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public ResponseDto delete(String userId, String id) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    private NotesResponse mapperDto(NotesEntity source) {
        return mapper.map(source, NotesResponse.class);
    }

    public Specification<NotesEntity> filterWithParameters(Map<String, String> params) {
        return new NotesSpecification().getSpecificationByFilters(params);
    }
}
