package com.example.FlieSharing.service;

import com.example.FlieSharing.entity.FileEntity;
import com.example.FlieSharing.model.FileModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {

    public List<FileModel> getAll();

    public ResponseEntity<?> uploadFile(MultipartFile file, String uploadBy) throws IOException;

    public ResponseEntity<?> shareFile(int id);

    public ResponseEntity<?> deleteFile(int id);

    public FileEntity getFileById(Long id);

}
