package com.example.FlieSharing.controller;

import com.example.FlieSharing.entity.FileEntity;
import com.example.FlieSharing.model.FileModel;
import com.example.FlieSharing.model.ShareLinkModel;
import com.example.FlieSharing.service.FileService;
import com.example.FlieSharing.service.ShareLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/dropfilex")
public class FileController {
    @Autowired
    private FileService fileService;

    @Autowired
    private ShareLinkService shareLinkService;

    // Home page (after login)
    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String name = oauth2User.getAttribute("name");
            String email = oauth2User.getAttribute("email");

            model.addAttribute("username", name != null ? name : "User");
            model.addAttribute("email", email);
        }
        return "home";
    }

    @GetMapping("/files")
    public String listFiles(Model model, Authentication authentication) {
        try {
            // Get user info for display
            if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                String username = oauth2User.getAttribute("name");
                model.addAttribute("username", username != null ? username : "Anonymous");
            } else {
                model.addAttribute("username", "Anonymous");
            }

            List<FileModel> files = fileService.getAll();
            System.out.println("Controller: Retrieved " + files.size() + " files for display");

            model.addAttribute("files", files);
            return "list-files";
        } catch (Exception e) {
            System.err.println("Controller: Error loading files page: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading files: " + e.getMessage());
            model.addAttribute("files", List.of()); // Empty list as fallback
            return "list-files";
        }
    }

    @PostMapping("/files/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam(value = "uploadedBy", required = false) String uploadedBy,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== FILE UPLOAD REQUEST ===");
            System.out.println("File original name: " + (file != null ? file.getOriginalFilename() : "null"));
            System.out.println("File size: " + (file != null ? file.getSize() : "0") + " bytes");
            System.out.println("File empty: " + (file != null ? file.isEmpty() : "true"));
            System.out.println("UploadedBy parameter: " + uploadedBy);

            // Validate file
            if (file == null || file.isEmpty()) {
                System.err.println("Controller: File is null or empty");
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
                return "redirect:/dropfilex/files";
            }

            // Validate file size (10MB limit)
            if (file.getSize() > 10 * 1024 * 1024) {
                System.err.println("Controller: File size exceeds limit: " + file.getSize());
                redirectAttributes.addFlashAttribute("error", "File size must be less than 10MB");
                return "redirect:/dropfilex/files";
            }

            // Determine who uploaded the file
            String finalUploadedBy = "Anonymous";

            // First check the form parameter
            if (uploadedBy != null && !uploadedBy.trim().isEmpty()) {
                finalUploadedBy = uploadedBy.trim();
            } else {
                // Fall back to authentication info
                if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
                    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                    String authName = oauth2User.getAttribute("name");
                    if (authName != null && !authName.trim().isEmpty()) {
                        finalUploadedBy = authName.trim();
                    }
                }
            }

            System.out.println("Controller: Final uploadedBy value: " + finalUploadedBy);

            // Call service to upload file
            ResponseEntity<?> result = fileService.uploadFile(file, finalUploadedBy);

            if (result.getStatusCode().is2xxSuccessful()) {
                System.out.println("Controller: File upload successful");
                redirectAttributes.addFlashAttribute("message", "File '" + file.getOriginalFilename() + "' uploaded successfully!");
            } else {
                System.err.println("Controller: File upload failed with status: " + result.getStatusCode());
                redirectAttributes.addFlashAttribute("error", "Failed to upload file");
            }

        } catch (IllegalArgumentException e) {
            System.err.println("Controller: Validation error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            System.err.println("Controller: IO error during upload: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error reading file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Controller: Unexpected error during upload: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error uploading file: " + e.getMessage());
        }

        return "redirect:/dropfilex/files";
    }

    @GetMapping("/files/share/{id}")
    public String shareFile(@PathVariable Long id, Model model) {
        try {
            System.out.println("Controller: Share request for file ID: " + id);

            FileEntity file = fileService.getFileById(id);

            if (file != null) {
                // Generate a unique share link with UUID
                String uniqueId = UUID.randomUUID().toString();

                // Create share link that expires in 7 days
                ShareLinkModel shareLink = new ShareLinkModel();
                shareLink.setFileId(id);
                shareLink.setUniqueId(uniqueId);
                shareLink.setCreatedAt(LocalDateTime.now());
                shareLink.setExpiresAt(LocalDateTime.now().plusDays(7));

                // Save the share link
                shareLinkService.createShareLink(shareLink);

                // Generate the shareable URL
                String shareUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/dropfilex/s/{uniqueId}")
                        .buildAndExpand(uniqueId)
                        .toUriString();

                System.out.println("Controller: Generated share URL: " + shareUrl);

                model.addAttribute("shareUrl", shareUrl);
                model.addAttribute("file", file);
                model.addAttribute("expiresIn", "7 days");
                return "share-file";
            } else {
                System.err.println("Controller: File not found for sharing, ID: " + id);
                return "redirect:/dropfilex/files?error=notfound";
            }
        } catch (Exception e) {
            System.err.println("Controller: Error sharing file: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/dropfilex/files?error=share";
        }
    }

    @GetMapping("/s/{uniqueId}")
    public String accessSharedFile(@PathVariable String uniqueId, Model model) {
        try {
            System.out.println("Controller: Accessing shared file with ID: " + uniqueId);

            ShareLinkModel shareLink = shareLinkService.getShareLinkByUniqueId(uniqueId);

            if (shareLink == null) {
                System.out.println("Controller: Share link not found: " + uniqueId);
                return "link-expired";
            }

            // Check if link has expired
            if (LocalDateTime.now().isAfter(shareLink.getExpiresAt())) {
                System.out.println("Controller: Share link expired: " + uniqueId);
                return "link-expired";
            }

            // Get the file
            FileEntity file = fileService.getFileById(shareLink.getFileId());
            if (file == null) {
                System.out.println("Controller: File not found for shared link: " + uniqueId);
                return "file-not-found";
            }

            // Calculate remaining time
            Duration remainingTime = Duration.between(LocalDateTime.now(), shareLink.getExpiresAt());
            long remainingDays = remainingTime.toDays();

            System.out.println("Controller: Shared file access successful, remaining days: " + remainingDays);

            model.addAttribute("file", file);
            model.addAttribute("remainingDays", remainingDays);
            model.addAttribute("uniqueId", uniqueId);
            return "shared-file-view";
        } catch (Exception e) {
            System.err.println("Controller: Error accessing shared file: " + e.getMessage());
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/s/download/{uniqueId}")
    public ResponseEntity<Resource> downloadSharedFile(@PathVariable String uniqueId) {
        try {
            System.out.println("Controller: Download request for shared file: " + uniqueId);

            ShareLinkModel shareLink = shareLinkService.getShareLinkByUniqueId(uniqueId);

            if (shareLink == null || LocalDateTime.now().isAfter(shareLink.getExpiresAt())) {
                System.out.println("Controller: Share link not found or expired: " + uniqueId);
                return ResponseEntity.notFound().build();
            }

            FileEntity file = fileService.getFileById(shareLink.getFileId());

            if (file == null || file.getFileData() == null) {
                System.out.println("Controller: File or file data not found for shared download: " + uniqueId);
                return ResponseEntity.notFound().build();
            }

            // Track download
            shareLinkService.incrementDownloadCount(uniqueId);

            ByteArrayResource resource = new ByteArrayResource(file.getFileData());

            System.out.println("Controller: Shared file download successful: " + file.getFileName());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getFileName() + "\"")
                    .contentLength(file.getFileData().length)
                    .body(resource);
        } catch (Exception e) {
            System.err.println("Controller: Error downloading shared file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            System.out.println("Controller: Download request for file ID: " + id);

            FileEntity file = fileService.getFileById(id);

            if (file == null || file.getFileData() == null) {
                System.out.println("Controller: File or file data not found for download: " + id);
                return ResponseEntity.notFound().build();
            }

            ByteArrayResource resource = new ByteArrayResource(file.getFileData());

            System.out.println("Controller: File download successful: " + file.getFileName());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getFileName() + "\"")
                    .contentLength(file.getFileData().length)
                    .body(resource);
        } catch (Exception e) {
            System.err.println("Controller: Error downloading file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    @PostMapping("/files/delete/{id}")
    @ResponseBody // Indicates that the return value should be bound to the web response body
    public ResponseEntity<?> deleteFile(@PathVariable Long id) {
        try {
            System.out.println("=== FILE DELETE REQUEST ===");
            System.out.println("Controller: Delete request received for file ID: " + id);

            // Validate ID
            if (id == null || id <= 0) {
                System.err.println("Controller: Invalid file ID: " + id);
                return ResponseEntity.badRequest().body("Invalid file ID provided.");
            }

            // Check if file exists before attempting deletion
            FileEntity existingFile = fileService.getFileById(id);
            if (existingFile == null) {
                System.err.println("Controller: File not found for deletion, ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found with ID: " + id);
            }

            // Call service to delete file
            ResponseEntity<?> serviceResponse = fileService.deleteFile(Math.toIntExact(id)); // Cast to int for service method
            System.out.println("Controller: Delete service response status: " + serviceResponse.getStatusCode());

            if (serviceResponse.getStatusCode().is2xxSuccessful()) {
                System.out.println("Controller: File deleted successfully: " + id);
                return ResponseEntity.ok().body("File deleted successfully.");
            } else {
                System.err.println("Controller: File deletion failed in service for ID: " + id + ". Service response: " + serviceResponse.getBody());
                // Return the error from the service
                return ResponseEntity.status(serviceResponse.getStatusCode()).body(serviceResponse.getBody());
            }
        } catch (Exception e) {
            System.err.println("Controller: Exception during file deletion for ID: " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting file: " + e.getMessage());
        }
    }
}
