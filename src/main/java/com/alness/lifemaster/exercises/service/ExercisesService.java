package com.alness.lifemaster.exercises.service;

import java.util.List;
import java.util.Map;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.exercises.dto.ExercisesRequest;
import com.alness.lifemaster.exercises.dto.ExercisesResponse;

public interface ExercisesService {
    public ExercisesResponse save(String userId, ExercisesRequest request);
    public List<ExercisesResponse> find(String userId, Map<String, String> params);
    public ExercisesResponse findOne(String userId, String id);
    public ExercisesResponse update(String userId, String id, ExercisesRequest request);
    public ResponseServerDto delete(String userId, String id);
}
