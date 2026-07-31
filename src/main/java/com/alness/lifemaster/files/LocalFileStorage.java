package com.alness.lifemaster.files;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.utils.ApiCodes;

import org.springframework.http.HttpStatus;

@Component
public class LocalFileStorage implements FileStorage {
    private final Path root;

    public LocalFileStorage(@Value("${app.files.root:./data/files}") String root) {
        this.root = resolveRoot(root);
        try {
            Files.createDirectories(this.root);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo inicializar el almacenamiento de archivos.", exception);
        }
    }

    @Override
    public void store(Path source, String relativePath) {
        Path target = resolve(relativePath);
        try {
            Files.createDirectories(target.getParent());
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            try {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException inner) {
                throw storageError(inner);
            }
        } catch (IOException exception) {
            throw storageError(exception);
        }
    }

    @Override
    public Path load(String relativePath) {
        Path value = resolve(relativePath);
        if (!Files.isRegularFile(value)) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_404, HttpStatus.NOT_FOUND,
                    "Archivo no encontrado.");
        }
        return value;
    }

    @Override
    public void delete(String relativePath) {
        try {
            Files.deleteIfExists(resolve(relativePath));
        } catch (IOException exception) {
            throw storageError(exception);
        }
    }

    private Path resolve(String relativePath) {
        Path value = root.resolve(relativePath).normalize();
        if (!value.startsWith(root)) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    "Ruta de archivo inválida.");
        }
        return value;
    }

    private Path resolveRoot(String configuredRoot) {
        Path configured = Path.of(configuredRoot);
        if (configured.isAbsolute()) {
            return configured.normalize();
        }
        return applicationDirectory().resolve(configured).normalize().toAbsolutePath();
    }

    private Path applicationDirectory() {
        try {
            URI location = LocalFileStorage.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI();
            Path codeSource = Path.of(location).toAbsolutePath().normalize();
            if (Files.isRegularFile(codeSource)) {
                return codeSource.getParent();
            }
        } catch (Exception ignored) {
            // En ejecución desde IDE no existe un JAR físico; se usa la carpeta de trabajo.
        }
        return Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

    private RestExceptionHandler storageError(Exception exception) {
        return new RestExceptionHandler(ApiCodes.API_CODE_500, HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo completar la operación de almacenamiento.");
    }
}
