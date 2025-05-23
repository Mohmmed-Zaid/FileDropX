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
import org.springframework.transaction.annotation.Transactional; // Import for @Transactional

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileRepository fileRepository;

    // You might need to inject ShareLinkRepository if you have foreign key constraints
    // @Autowired
    // private ShareLinkRepository shareLinkRepository;

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
            return List.of(); // Return empty list instead of null
        }
    }

    @Override
    public ResponseEntity<?> uploadFile(MultipartFile file, String uploadBy) throws IOException {
        System.out.println("Service: Starting file upload process");

        try {
            // Validate inputs
            if (file == null || file.isEmpty()) {
                System.err.println("Service: File is null or empty");
                throw new IllegalArgumentException("File cannot be empty");
            }

            if (uploadBy == null || uploadBy.trim().isEmpty()) {
                System.out.println("Service: UploadBy is empty, setting to Anonymous");
                uploadBy = "Anonymous";
            }

            // Log file details
            System.out.println("Service: Processing file upload:");
            System.out.println("  - File name: " + file.getOriginalFilename());
            System.out.println("  - File size: " + file.getSize() + " bytes");
            System.out.println("  - Content type: " + file.getContentType());
            System.out.println("  - Uploaded by: " + uploadBy);

            // Validate file size (10MB limit)
            if (file.getSize() > 10 * 1024 * 1024) {
                System.err.println("Service: File size exceeds 10MB limit");
                throw new IllegalArgumentException("File size should not exceed 10MB");
            }

            // Validate file name
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.trim().isEmpty()) {
                System.err.println("Service: File name is null or empty");
                throw new IllegalArgumentException("File name cannot be empty");
            }

            // Create and populate entity
            FileEntity entity = new FileEntity();
            entity.setFileName(fileName.trim());
            entity.setUploadedBy(uploadBy.trim());
            entity.setUploadTime(LocalDateTime.now());
            entity.setExpiryTime(LocalDateTime.now().plusDays(1));

            // Convert file to byte array
            byte[] fileData = file.getBytes();
            System.out.println("Service: Successfully converted file to byte array, size: " + fileData.length);
            entity.setFileData(fileData);

            // Save to database
            System.out.println("Service: Attempting to save file to database");
            FileEntity savedEntity = fileRepository.save(entity);
            System.out.println("Service: File saved successfully with ID: " + savedEntity.getId());

            // Verify the save operation
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
        System.out.println("Service: Attempting to share file with ID: " + id);

        try {
            Long longId = (long) id;
            Optional<FileEntity> entityOptional = fileRepository.findById(longId);

            if (entityOptional.isPresent()) {
                System.out.println("Service: File found for sharing, ID: " + id);
                FileEntity entity = entityOptional.get();
                return ResponseEntity.ok().body(convertToModel(entity));
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
    public ResponseEntity<?> deleteFile(int id) {
        System.out.println("Service: Starting file deletion process for ID: " + id);

        try {
            Long longId = (long) id;

            // First check if the file exists
            Optional<FileEntity> fileOptional = fileRepository.findById(longId);

            if (!fileOptional.isPresent()) {
                System.out.println("Service: File with ID " + id + " not found for deletion");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("File with ID " + id + " not found");
            }

            FileEntity fileToDelete = fileOptional.get();
            System.out.println("Service: Found file to delete - Name: " + fileToDelete.getFileName() +
                    ", Uploaded by: " + fileToDelete.getUploadedBy());

            // --- IMPORTANT: Handle Foreign Key Constraints if they exist ---
            // If your ShareLinkEntity has a foreign key to FileEntity,
            // you MUST delete associated ShareLink records first,
            // or configure cascade deletion in your database/JPA mapping.
            // Example if you had a ShareLinkRepository:
            // shareLinkRepository.deleteByFileId(longId); // You'd need to implement this method

            // Perform the deletion
            fileRepository.deleteById(longId);
            System.out.println("Service: Delete command executed for file ID: " + id);

            // Verify deletion by checking if file still exists
            // This check might sometimes return true immediately after deleteById if the transaction
            // hasn't committed yet, but for most cases, it's a good immediate verification.
            Optional<FileEntity> deletedFileCheck = fileRepository.findById(longId);

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
