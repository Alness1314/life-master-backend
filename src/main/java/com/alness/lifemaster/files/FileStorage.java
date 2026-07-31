package com.alness.lifemaster.files;

import java.nio.file.Path;

public interface FileStorage {
    void store(Path source, String relativePath);
    Path load(String relativePath);
    void delete(String relativePath);
}
