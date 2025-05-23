package com.example.FlieSharing.service;

import com.example.FlieSharing.entity.FileEntity;
import com.example.FlieSharing.model.FileModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {
    List<FileModel> getAll();
    ResponseEntity<?> uploadFile(MultipartFile file, String uploadBy) throws IOException;
    // shareFile(int id) is technically in the interface, but the controller now uses ShareLinkService directly for link creation.
    // Keeping it for compatibility with the original interface if other parts relied on it.
    ResponseEntity<?> shareFile(int id);
    FileEntity getFileById(Long id);
    ResponseEntity<?> deleteFile(Long id); // Consistent parameter type
}
