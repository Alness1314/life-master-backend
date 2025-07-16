package com.alness.lifemaster.modules.service;

import java.util.List;
import java.util.Map;

import com.alness.lifemaster.common.dto.ResponseServerDto;
import com.alness.lifemaster.modules.dto.ModuleDto;
import com.alness.lifemaster.modules.dto.request.ModuleRequest;
import com.alness.lifemaster.modules.dto.response.ModuleResponse;

public interface ModuleService {
    public ResponseServerDto multiSave(List<ModuleRequest> module);
    public ModuleResponse createModule(ModuleRequest module);
    public ModuleResponse updateModule(String id, ModuleRequest moduleDetails);
    public ResponseServerDto deleteModule(String id);
    public ModuleResponse getModuleById(String id);
    public List<ModuleResponse> getAllModules();
    public ModuleResponse assignChildToParent(String parentId, String childId);
    public List<ModuleDto> find(Map<String, String> params);
}
