package com.alness.lifemaster.notes.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alness.lifemaster.common.dto.ResponseDto;
import com.alness.lifemaster.notes.dto.request.NotesRequest;
import com.alness.lifemaster.notes.dto.response.NotesResponse;
import com.alness.lifemaster.notes.service.NotesService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.prefix}/usuarios")
@Tag(name = "Notes", description = ".")
public class NotesController {
    @Autowired
    private NotesService notesService;

    @GetMapping("/{userId}/notes")
    public ResponseEntity<List<NotesResponse>> findAll(@PathVariable String userId,
            @RequestParam Map<String, String> parameters) {
        List<NotesResponse> response = notesService.find(userId, parameters);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{userId}/notes/{id}")
    public ResponseEntity<NotesResponse> findOne(@PathVariable String userId, @PathVariable String id) {
        NotesResponse response = notesService.findOne(userId, id);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping("/{userId}/notes")
    public ResponseEntity<NotesResponse> save(@PathVariable String userId,
            @Valid @RequestBody NotesRequest request) {
        NotesResponse response = notesService.save(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{userId}/notes/{id}")
    public ResponseEntity<NotesResponse> update(@PathVariable String userId, @PathVariable String id,
            @RequestBody NotesRequest request) {
        NotesResponse response = notesService.update(userId, id, request);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{userId}/notes/{id}")
    public ResponseEntity<ResponseDto> delete(@PathVariable String userId, @PathVariable String id) {
        ResponseDto response = notesService.delete(userId, id);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}
