package com.example.FlieSharing.service;

import com.example.FlieSharing.entity.FileEntity;
import com.example.FlieSharing.model.FileModel;
import com.example.FlieSharing.repository.FileRepository;
import com.example.FlieSharing.exception.FileNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired // Inject ShareLinkService to handle related share link deletions
    private ShareLinkService shareLinkService;

    private FileModel convertToModel(FileEntity entity) {
        FileModel model = new FileModel();
        BeanUtils.copyProperties(entity, model);
        return model;
    }

    @Override
    public List<FileModel> getAll() {
        try {
            System.out.println("Service: Fetching all files from database");
            List<FileEntity> entities = fileRepository.findAll();
            System.out.println("Service: Found " + entities.size() + " files in database");

            return entities.stream()
                    .map(this::convertToModel)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Service: Error fetching files: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public ResponseEntity<?> uploadFile(MultipartFile file, String uploadBy) throws IOException {
        System.out.println("Service: Starting file upload process");

        try {
            if (file == null || file.isEmpty()) {
                System.err.println("Service: File is null or empty");
                throw new IllegalArgumentException("File cannot be empty");
            }

            if (uploadBy == null || uploadBy.trim().isEmpty()) {
                System.out.println("Service: UploadBy is empty, setting to Anonymous");
                uploadBy = "Anonymous";
            }

            System.out.println("Service: Processing file upload:");
            System.out.println("  - File name: " + file.getOriginalFilename());
            System.out.println("  - File size: " + file.getSize() + " bytes");
            System.out.println("  - Content type: " + file.getContentType());
            System.out.println("  - Uploaded by: " + uploadBy);

            if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
                System.err.println("Service: File size exceeds 10MB limit");
                throw new IllegalArgumentException("File size should not exceed 10MB");
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.trim().isEmpty()) {
                System.err.println("Service: File name is null or empty");
                throw new IllegalArgumentException("File name cannot be empty");
            }

            FileEntity entity = new FileEntity();
            entity.setFileName(fileName.trim());
            entity.setUploadedBy(uploadBy.trim());
            entity.setUploadTime(LocalDateTime.now());
            entity.setExpiryTime(LocalDateTime.now().plusDays(1)); // Default file expiry

            byte[] fileData = file.getBytes();
            System.out.println("Service: Successfully converted file to byte array, size: " + fileData.length);
            entity.setFileData(fileData);

            System.out.println("Service: Attempting to save file to database");
            FileEntity savedEntity = fileRepository.save(entity);
            System.out.println("Service: File saved successfully with ID: " + savedEntity.getId());

            Optional<FileEntity> verifyEntity = fileRepository.findById(savedEntity.getId());
            if (verifyEntity.isPresent()) {
                System.out.println("Service: Verified file exists in database after save");
            } else {
                System.err.println("Service: WARNING - File not found in database after save operation");
            }

            return ResponseEntity.ok().body(convertToModel(savedEntity));

        } catch (IOException e) {
            System.err.println("Service: IOException during file upload: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (IllegalArgumentException e) {
            System.err.println("Service: Validation error during file upload: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Service: Unexpected error during file upload: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<?> shareFile(int id) {
        System.out.println("Service: shareFile(int id) called (might be deprecated soon). ID: " + id);
        try {
            Long longId = (long) id;
            Optional<FileEntity> entityOptional = fileRepository.findById(longId);

            if (entityOptional.isPresent()) {
                System.out.println("Service: File found for sharing, ID: " + id);
                return ResponseEntity.ok().body(convertToModel(entityOptional.get()));
            } else {
                System.err.println("Service: File not found for sharing, ID: " + id);
                throw new FileNotFoundException("File with ID " + id + " not found");
            }
        } catch (Exception e) {
            System.err.println("Service: Error sharing file with ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public FileEntity getFileById(Long id) {
        System.out.println("Service: Fetching file by ID: " + id);

        try {
            Optional<FileEntity> entityOptional = fileRepository.findById(id);

            if (entityOptional.isPresent()) {
                System.out.println("Service: File found with ID: " + id);
                return entityOptional.get();
            } else {
                System.out.println("Service: File not found with ID: " + id);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Service: Error fetching file by ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Transactional // Added @Transactional annotation here
    public ResponseEntity<?> deleteFile(Long id) { // Consolidated to use Long id
        System.out.println("Service: Starting file deletion process for ID: " + id);

        try {
            Optional<FileEntity> fileOptional = fileRepository.findById(id);

            if (!fileOptional.isPresent()) {
                System.out.println("Service: File with ID " + id + " not found for deletion");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("File with ID " + id + " not found");
            }

            FileEntity fileToDelete = fileOptional.get();
            System.out.println("Service: Found file to delete - Name: " + fileToDelete.getFileName() +
                    ", Uploaded by: " + fileToDelete.getUploadedBy());

            // --- IMPORTANT: Delete associated ShareLink records first ---
            shareLinkService.deleteShareLinksByFileId(id);

            fileRepository.deleteById(id);
            System.out.println("Service: Delete command executed for file ID: " + id);

            Optional<FileEntity> deletedFileCheck = fileRepository.findById(id);

            if (!deletedFileCheck.isPresent()) {
                System.out.println("Service: File with ID " + id + " successfully deleted and verified");
                return ResponseEntity.ok().body("File deleted successfully");
            } else {
                System.err.println("Service: File with ID " + id + " still exists after deletion attempt");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("File deletion failed - file still exists in database");
            }

        } catch (Exception e) {
            System.err.println("Service: Error during deletion of file ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting file: " + e.getMessage());
        }
    }

    // Additional helper method to check database connectivity
    public boolean isDatabaseConnected() {
        try {
            long count = fileRepository.count();
            System.out.println("Service: Database connectivity check - Total files count: " + count);
            return true;
        } catch (Exception e) {
            System.err.println("Service: Database connectivity check failed: " + e.getMessage());
            return false;
        }
    }

    // Additional helper method to get file count
    public long getFileCount() {
        try {
            return fileRepository.count();
        } catch (Exception e) {
            System.err.println("Service: Error getting file count: " + e.getMessage());
            return 0;
        }
    }
}
