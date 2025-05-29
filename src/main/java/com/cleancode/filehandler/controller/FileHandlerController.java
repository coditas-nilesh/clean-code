package com.cleancode.filehandler.controller;

import com.cleancode.filehandler.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

@RestController
@RequestMapping("/files")
@Slf4j
public class FileHandlerController {

    private final FileStorageService storageService;

    public FileHandlerController(FileStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String uniqueFileId = storageService.storeFile(file);
        return ResponseEntity.ok(uniqueFileId);
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws MalformedURLException {
        Path filePath = storageService.loadFile(fileName);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }
        String contentDisposition = "attachment; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileName) throws IOException {
        storageService.deleteFile(fileName);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rename")
    public ResponseEntity<String> renameFile(@RequestParam String oldFileName,
                                             @RequestParam String newFileName) throws IOException {
        String renamedFile = storageService.renameFile(oldFileName, newFileName);
        return ResponseEntity.ok(renamedFile);
    }

    @PostMapping("/move")
    public ResponseEntity<String> moveFile(@RequestParam String fileName,
                                           @RequestParam String newLocation) throws IOException {
        String newPath = storageService.moveFile(fileName, newLocation);
        return ResponseEntity.ok(newPath);
    }
}
