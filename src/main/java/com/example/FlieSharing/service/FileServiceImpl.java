package com.example.FlieSharing.service;

import com.example.FlieSharing.entity.FileEntity;
import com.example.FlieSharing.model.FileModel;
import com.example.FlieSharing.repository.FileRepository;
import com.example.FlieSharing.exception.FileNotFoundException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileRepository fileRepository;

    private FileModel convertToModel(FileEntity entity) {
        FileModel model = new FileModel();
        BeanUtils.copyProperties(entity, model);
        return model;
    }

    @Override
    public List<FileModel> getAll() {
        List<FileEntity> entities = fileRepository.findAll();
        return entities.stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<?> uploadFile(MultipartFile file, String uploadBy) throws IOException {
        FileEntity entity = new FileEntity();
        entity.setFileName(file.getOriginalFilename());
        entity.setUploadedBy(uploadBy);
        entity.setUploadTime(LocalDateTime.now());
        entity.setExpiryTime(LocalDateTime.now().plusDays(1));
        entity.setFileData(file.getBytes());

        fileRepository.save(entity);

        return ResponseEntity.ok().body(convertToModel(entity));
    }

    @Override
    public ResponseEntity<?> shareFile(int id) {
        Optional<FileEntity> entity = fileRepository.findById((long) id);  // Cast to match Long type
        if (entity.isPresent()) {
            return ResponseEntity.ok().body(convertToModel(entity.get()));
        } else {
            throw new FileNotFoundException("File with ID " + id + " not found");
        }
    }

    @Override
    public ResponseEntity<?> deleteFile(int id) {
        Optional<FileEntity> entity = fileRepository.findById((long) id);  // Cast to match Long type
        if (entity.isPresent()) {
            fileRepository.delete(entity.get());
            return ResponseEntity.ok().body("File Deleted Successfully");
        } else {
            throw new FileNotFoundException("File with ID " + id + " not found");
        }
    }
}
