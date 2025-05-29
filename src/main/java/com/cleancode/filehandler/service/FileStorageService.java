package com.cleancode.filehandler.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Path;

@Service
public interface FileStorageService {

    String storeFile(MultipartFile file) throws IOException;

    Path loadFile(String fileName);

    void deleteFile(String fileName) throws IOException;

    String renameFile(String oldFileName,String newFileName) throws IOException;

    String moveFile(String fileName,String newLocation) throws IOException;
}
