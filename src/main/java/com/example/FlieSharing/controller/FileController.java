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
        // Get user info for display
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            model.addAttribute("username", oauth2User.getAttribute("name"));
        }

        List<FileModel> files = fileService.getAll();
        if (files.isEmpty()) {
            System.out.println("No files found in the database.");
        } else {
            files.forEach(file -> System.out.println("Retrieved File: " + file));
        }
        model.addAttribute("files", files);
        return "list-files";
    }

    @PostMapping("/files/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) throws IOException {
        try {
            String uploadedBy = "Anonymous";
            if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                uploadedBy = oauth2User.getAttribute("name");
            }

            fileService.uploadFile(file, uploadedBy);
            redirectAttributes.addFlashAttribute("message", "File uploaded successfully!");
        } catch (Exception e) {
            System.err.println("Error uploading file: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error uploading file: " + e.getMessage());
        }
        return "redirect:/dropfilex/files";
    }

    @GetMapping("/files/share/{id}")
    public String shareFile(@PathVariable Long id, Model model) {
        try {
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

                model.addAttribute("shareUrl", shareUrl);
                model.addAttribute("file", file);
                model.addAttribute("expiresIn", "7 days");
                return "share-file";
            } else {
                return "redirect:/dropfilex/files?error=notfound";
            }
        } catch (Exception e) {
            System.err.println("Error sharing file: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/dropfilex/files?error=share";
        }
    }

    @GetMapping("/s/{uniqueId}")
    public String accessSharedFile(@PathVariable String uniqueId, Model model) {
        try {
            ShareLinkModel shareLink = shareLinkService.getShareLinkByUniqueId(uniqueId);

            if (shareLink == null) {
                return "link-expired";
            }

            // Check if link has expired
            if (LocalDateTime.now().isAfter(shareLink.getExpiresAt())) {
                return "link-expired";
            }

            // Get the file
            FileEntity file = fileService.getFileById(shareLink.getFileId());
            if (file == null) {
                return "file-not-found";
            }

            // Calculate remaining time
            Duration remainingTime = Duration.between(LocalDateTime.now(), shareLink.getExpiresAt());
            long remainingDays = remainingTime.toDays();

            model.addAttribute("file", file);
            model.addAttribute("remainingDays", remainingDays);
            model.addAttribute("uniqueId", uniqueId);
            return "shared-file-view";
        } catch (Exception e) {
            System.err.println("Error accessing shared file: " + e.getMessage());
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/s/download/{uniqueId}")
    public ResponseEntity<Resource> downloadSharedFile(@PathVariable String uniqueId) {
        try {
            ShareLinkModel shareLink = shareLinkService.getShareLinkByUniqueId(uniqueId);

            if (shareLink == null || LocalDateTime.now().isAfter(shareLink.getExpiresAt())) {
                return ResponseEntity.notFound().build();
            }

            FileEntity file = fileService.getFileById(shareLink.getFileId());

            if (file == null || file.getFileData() == null) {
                return ResponseEntity.notFound().build();
            }

            // Track download
            shareLinkService.incrementDownloadCount(uniqueId);

            ByteArrayResource resource = new ByteArrayResource(file.getFileData());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getFileName() + "\"")
                    .contentLength(file.getFileData().length)
                    .body(resource);
        } catch (Exception e) {
            System.err.println("Error downloading shared file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        FileEntity file = fileService.getFileById(id);

        if (file == null || file.getFileData() == null) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayResource resource = new ByteArrayResource(file.getFileData());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName() + "\"")
                .contentLength(file.getFileData().length)
                .body(resource);
    }

    @PostMapping("/files/delete/{id}")
    public String deleteFile(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Debug log
        System.out.println("Delete request received for file ID: " + id);

        try {
            // Call service to delete file
            ResponseEntity<?> response = fileService.deleteFile(Math.toIntExact(id));
            System.out.println("Delete service response: " + response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful()) {
                // Add success message
                redirectAttributes.addFlashAttribute("message", "File deleted successfully");
            } else {
                // Add error message
                redirectAttributes.addFlashAttribute("error", "Failed to delete file");
            }
        } catch (Exception e) {
            System.err.println("Exception in controller while deleting file: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        // Always redirect back to the files list after attempting deletion
        return "redirect:/dropfilex/files";
    }
}