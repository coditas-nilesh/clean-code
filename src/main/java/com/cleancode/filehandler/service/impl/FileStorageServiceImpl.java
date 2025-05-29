package com.cleancode.filehandler.service.impl;

import com.cleancode.filehandler.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final Path storageLocation;

    public FileStorageServiceImpl(@Value("${file.storage.location}") String storagePath) {
        this.storageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageLocation);
            log.info("Storage directory initialized at: {}", this.storageLocation);
        } catch (IOException ex) {
            log.error("Could not create the directory where the uploaded files will be stored.", ex);
            throw new RuntimeException("Could not initialize storage", ex);
        }
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(storageLocation);
    }

    /**
     * Uploads and stores a file with a unique name.
     *
     * @param file Multipart file to be uploaded
     * @return The generated unique file name
     */
    @Override
    public String storeFile(MultipartFile file) throws IOException {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";

        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String uniqueFileName = UUID.randomUUID() + extension;

        Path targetLocation = storageLocation.resolve(uniqueFileName);

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        log.info("File stored successfully: {}", uniqueFileName);
        return uniqueFileName;
    }

    /**
     * Validates file from the storage.
     *
     * @param file Name of the file to validate
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file.");
        }
    }

    /**
     * Retrieves a file as a Path object using its unique name.
     *
     * @param fileName Name of the stored file
     * @return Path to the file
     */
    @Override
    public Path loadFile(String fileName) {
        Path filePath = storageLocation.resolve(fileName).normalize();
        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found: " + fileName);
        }
        return filePath;
    }

    /**
     * Deletes a file from the storage.
     *
     * @param fileName Name of the file to delete
     */
    @Override
    public void deleteFile(String fileName) throws IOException {
        Path filePath = loadFile(fileName);
        Files.delete(filePath);
        log.info("File deleted successfully: {}", fileName);
    }

    /**
     * Renames a file in the storage.
     *
     * @param oldFileName Existing file name
     * @param newFileName New name for the file
     */
    @Override
    public String renameFile(String oldFileName, String newFileName) throws IOException {
        Path oldFilePath = loadFile(oldFileName);
        Path newFilePath = storageLocation.resolve(newFileName).normalize();

        if (Files.exists(newFilePath)) {
            throw new RuntimeException("File with new name already exists: " + newFileName);
        }

        Files.move(oldFilePath, newFilePath,StandardCopyOption.REPLACE_EXISTING);
        log.info("File renamed from {} to {}", oldFileName, newFileName);
        return newFileName;
    }

    /**
     * Moves a file to a different location within the storage directory.
     *
     * @param filename File to move
     * @param newLocation newLocation under storage location to move file into
     */
    @Override
    public String moveFile(String filename, String newLocation) throws IOException {
        Path oldFilePath = loadFile(filename);
        Path newDir = Paths.get(newLocation).toAbsolutePath().normalize();

        Files.createDirectories(newDir);
        Path newFilePath = newDir.resolve(filename);

        Files.move(oldFilePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("File moved to {}/{}", newLocation, filename);
        return newFilePath.toString();
    }
}
