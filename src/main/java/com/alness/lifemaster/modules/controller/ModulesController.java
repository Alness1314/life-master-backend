package com.alness.lifemaster.modules.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alness.lifemaster.app.dto.ResponseServer;
import com.alness.lifemaster.modules.dto.ModuleDto;
import com.alness.lifemaster.modules.dto.request.ModuleRequest;
import com.alness.lifemaster.modules.dto.response.ModuleResponse;
import com.alness.lifemaster.modules.service.ModuleService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("${api.prefix}/modules")
@Tag(name = "Modules", description = ".")
public class ModulesController {
    @Autowired
    private ModuleService moduleService;
    
    @GetMapping
    public ResponseEntity<List<ModuleResponse>> findAll(@RequestParam Map<String, String> param) {
        List<ModuleResponse> response = moduleService.getAllModules();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleResponse> findOne(@PathVariable String id) {
        ModuleResponse response = moduleService.getModuleById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ModuleResponse> create(@RequestBody ModuleRequest request) {
        ModuleResponse response = moduleService.createModule(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/all")
    public ResponseEntity<ResponseServer> createAll(@RequestBody List<ModuleRequest> request) {
        ResponseServer response = moduleService.multiSave(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping()
    public ResponseEntity<ModuleResponse> asignmodule(@RequestParam String parentModuleId, @RequestParam String childModuleId) {
        ModuleResponse response = moduleService.assignChildToParent(parentModuleId, childModuleId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ModuleDto>> find(@RequestParam Map<String, String> param) {
        List<ModuleDto> response = moduleService.find(param);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    

}
