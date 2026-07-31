package com.alness.lifemaster.files;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.time.LocalDate;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StoredFileService {
    private static final Set<String> IMAGE_TYPES = Set.of("image/png", "image/jpeg", "image/webp");
    private final StoredFileRepository repository;
    private final UserRepository userRepository;
    private final FileStorage storage;

    @Value("${app.files.max-size-bytes:10485760}")
    private long maxSize;

    public StoredFileResponse save(UUID userId, FilePurpose purpose, MultipartFile file) {
        validate(purpose, file);
        Path temporary = copyToTemporary(file);
        String relativePath = buildPath(file.getOriginalFilename());
        try {
            storage.store(temporary, relativePath);
            registerRollbackDelete(relativePath);
            StoredFileEntity entity = new StoredFileEntity();
            entity.setUser(userRepository.findById(userId).orElseThrow(() -> notFound(userId)));
            apply(entity, purpose, file, relativePath);
            return toResponse(repository.save(entity));
        } finally {
            deleteTemporary(temporary);
        }
    }

    public StoredFileResponse replace(UUID userId, UUID id, FilePurpose purpose, MultipartFile file) {
        validate(purpose, file);
        StoredFileEntity entity = findOwned(userId, id);
        String previousPath = entity.getStoragePath();
        Path temporary = copyToTemporary(file);
        String relativePath = buildPath(file.getOriginalFilename());
        try {
            storage.store(temporary, relativePath);
            registerRollbackDelete(relativePath);
            apply(entity, purpose, file, relativePath);
            StoredFileResponse response = toResponse(repository.save(entity));
            registerAfterCommitDelete(previousPath);
            return response;
        } finally {
            deleteTemporary(temporary);
        }
    }

    @Transactional(readOnly = true)
    public List<StoredFileResponse> findAll(UUID userId) {
        return repository.findAllByUserIdAndErasedFalseOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public StoredFileEntity findOwned(UUID userId, UUID id) {
        return repository.findByIdAndUserIdAndErasedFalse(id, userId).orElseThrow(() -> notFound(id));
    }

    @Transactional(readOnly = true)
    public Path content(UUID userId, UUID id) {
        return storage.load(findOwned(userId, id).getStoragePath());
    }

    public void delete(UUID userId, UUID id) {
        StoredFileEntity entity = findOwned(userId, id);
        entity.setErased(true);
        repository.save(entity);
        registerAfterCommitDelete(entity.getStoragePath());
    }

    private void validate(FilePurpose purpose, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw badRequest("El archivo no puede estar vacío.");
        }
        if (file.getSize() > maxSize) {
            throw badRequest("El archivo excede el tamaño máximo permitido.");
        }
        String name = safeName(file.getOriginalFilename());
        if (!StringUtils.hasText(name) || name.contains("..")) {
            throw badRequest("El nombre del archivo es inválido.");
        }
        if (purpose == FilePurpose.PROFILE_IMAGE && !IMAGE_TYPES.contains(file.getContentType())) {
            throw badRequest("La imagen de perfil debe ser PNG, JPEG o WEBP.");
        }
    }

    private void apply(StoredFileEntity entity, FilePurpose purpose, MultipartFile file, String relativePath) {
        entity.setOriginalName(safeName(file.getOriginalFilename()));
        entity.setContentType(StringUtils.hasText(file.getContentType())
                ? file.getContentType() : "application/octet-stream");
        entity.setSizeBytes(file.getSize());
        entity.setPurpose(purpose == null ? FilePurpose.OTHER : purpose);
        entity.setStoragePath(relativePath);
        entity.setSha256(sha256(storage.load(relativePath)));
    }

    private Path copyToTemporary(MultipartFile file) {
        try {
            Path value = Files.createTempFile("lifemaster-upload-", ".tmp");
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, value, StandardCopyOption.REPLACE_EXISTING);
            }
            return value;
        } catch (IOException exception) {
            throw storageFailure();
        }
    }

    private String buildPath(String originalName) {
        String extension = StringUtils.getFilenameExtension(safeName(originalName));
        String fileName = UUID.randomUUID() + (StringUtils.hasText(extension) ? "." + extension.toLowerCase() : "");
        LocalDate date = LocalDate.now();
        return date.getYear() + "/" + String.format("%02d", date.getMonthValue()) + "/" + fileName;
    }

    private String sha256(Path file) {
        try (InputStream input = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw storageFailure();
        }
    }

    private String safeName(String name) {
        String value = name == null ? "archivo" : name.replace("\\", "/");
        value = value.substring(value.lastIndexOf('/') + 1).trim();
        return value.length() > 256 ? value.substring(value.length() - 256) : value;
    }

    private StoredFileResponse toResponse(StoredFileEntity value) {
        return new StoredFileResponse(value.getId(), value.getOriginalName(), value.getContentType(),
                value.getSizeBytes(), value.getSha256(), value.getPurpose(), value.getCreatedAt(),
                value.getUpdatedAt());
    }

    private void registerRollbackDelete(String path) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) storage.delete(path);
            }
        });
    }

    private void registerAfterCommitDelete(String path) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            storage.delete(path);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { storage.delete(path); }
        });
    }

    private void deleteTemporary(Path path) {
        try { Files.deleteIfExists(path); } catch (IOException ignored) { }
    }

    private RestExceptionHandler badRequest(String message) {
        return new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST, message);
    }

    private RestExceptionHandler notFound(Object id) {
        return new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                "Archivo no encontrado: " + id);
    }

    private RestExceptionHandler storageFailure() {
        return new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo procesar el archivo.");
    }
}
