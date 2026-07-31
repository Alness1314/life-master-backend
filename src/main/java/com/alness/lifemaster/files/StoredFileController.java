package com.alness.lifemaster.files;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users/{userId}/files")
@RequiredArgsConstructor
public class StoredFileController {
    private final StoredFileService service;

    @GetMapping
    public List<StoredFileResponse> findAll(@PathVariable UUID userId) {
        return service.findAll(userId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public StoredFileResponse save(@PathVariable UUID userId,
            @RequestParam(defaultValue = "OTHER") FilePurpose purpose,
            @RequestPart("file") MultipartFile file) {
        return service.save(userId, purpose, file);
    }

    @GetMapping("/{id}/metadata")
    public StoredFileResponse metadata(@PathVariable UUID userId, @PathVariable UUID id) {
        StoredFileEntity value = service.findOwned(userId, id);
        return new StoredFileResponse(value.getId(), value.getOriginalName(), value.getContentType(),
                value.getSizeBytes(), value.getSha256(), value.getPurpose(), value.getCreatedAt(),
                value.getUpdatedAt());
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<FileSystemResource> content(@PathVariable UUID userId, @PathVariable UUID id) {
        StoredFileEntity metadata = service.findOwned(userId, id);
        Path path = service.content(userId, id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(metadata.getOriginalName()).build().toString())
                .body(new FileSystemResource(path));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StoredFileResponse replace(@PathVariable UUID userId, @PathVariable UUID id,
            @RequestParam(defaultValue = "OTHER") FilePurpose purpose,
            @RequestPart("file") MultipartFile file) {
        return service.replace(userId, id, purpose, file);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID userId, @PathVariable UUID id) {
        service.delete(userId, id);
    }
}
