package com.alness.lifemaster.notes.service;

import java.util.List;
import java.util.Map;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.notes.dto.request.NotesRequest;
import com.alness.lifemaster.notes.dto.response.NotesResponse;

public interface NotesService {
    public List<NotesResponse> find(String userId, Map<String, String> params);
    public NotesResponse findOne(String userId, String id);
    public NotesResponse save(String userId, NotesRequest request);
    public NotesResponse update(String userId, String id, NotesRequest request);
    public ResponseServerDto delete(String userId, String id);
}
