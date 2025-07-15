package com.alness.lifemaster.notes.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.common.keys.Filters;
import com.alness.lifemaster.common.messages.Messages;
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
import com.alness.lifemaster.utils.FuncUtils;
import com.alness.lifemaster.utils.LoggerUtil;

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
        return notesRepository.findAll(filterWithParameters(FuncUtils.integrateUser(userId, params)))
                .stream()
                .map(this::mapperDto)
                .toList();
    }

    @Override
    public NotesResponse findOne(String userId, String id) {
        NotesEntity note = notesRepository
                .findOne(filterWithParameters(Map.of(Filters.KEY_ID, id, Filters.KEY_USER, userId)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));
        return mapperDto(note);
    }

    @Override
    public NotesResponse save(String userId, NotesRequest request) {
        NotesEntity newNote = mapper.map(request, NotesEntity.class);
        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, userId)));
        newNote.setUser(user);
        try {
            newNote = notesRepository.save(newNote);
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    Messages.DATA_INTEGRITY);
        } catch (Exception e) {
            LoggerUtil.logError(e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    Messages.ERROR_ENTITY_UPDATE);
        }
        return mapperDto(newNote);
    }

    @Override
    public NotesResponse update(String userId, String id, NotesRequest request) {
        NotesEntity existingNote = notesRepository
                .findOne(filterWithParameters(Map.of(Filters.KEY_ID, id, Filters.KEY_USER, userId)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));

        // Validar que la nota pertenece al usuario
        if (!existingNote.getUser().getId().toString().equals(userId)) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_403, HttpStatus.FORBIDDEN,
                    Messages.FORBIDEN_UPDATE_DATA);
        }

        // Actualizar campos usando el mapper
        mapper.map(request, existingNote);

        try {
            existingNote = notesRepository.save(existingNote);
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    Messages.DATA_INTEGRITY);
        } catch (Exception e) {
            LoggerUtil.logError(e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                    Messages.ERROR_ENTITY_UPDATE);
        }

        return mapperDto(existingNote);
    }

    @Override
    public ResponseServerDto delete(String userId, String id) {
        NotesEntity note = notesRepository
                .findOne(filterWithParameters(Map.of(Filters.KEY_ID, id, Filters.KEY_USER, userId)))
                .orElseThrow(() -> new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                        String.format(Messages.NOT_FOUND, id)));
        try {
            notesRepository.delete(note);
            return new ResponseServerDto(String.format(Messages.ENTITY_DELETE, id), HttpStatus.ACCEPTED, true);
        } catch (DataIntegrityViolationException ex) {
            LoggerUtil.logError(ex);
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    Messages.DATA_INTEGRITY);
        } catch (Exception e) {
            LoggerUtil.logError(e);
            throw new RestExceptionHandler(ApiCodes.API_CODE_409, HttpStatus.CONFLICT,
                    String.format(Messages.ERROR_ENTITY_DELETE, e.getMessage()));
        }
    }

    private NotesResponse mapperDto(NotesEntity source) {
        return mapper.map(source, NotesResponse.class);
    }

    public Specification<NotesEntity> filterWithParameters(Map<String, String> params) {
        return new NotesSpecification().getSpecificationByFilters(params);
    }
}
