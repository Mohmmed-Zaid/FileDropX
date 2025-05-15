package com.example.FlieSharing.controller;

import com.example.FlieSharing.model.FileModel;
import com.example.FlieSharing.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Controller
public class FileController {
    @Autowired
    private FileService fileService;

    @GetMapping("/")
    public String landing() {
        return "home";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/files")
    public String listFiles(Model model) {
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
                             @RequestParam("uploadedBy") String uploadedBy) throws IOException {
        try {
            fileService.uploadFile(file, uploadedBy);
        } catch (Exception e) {
            System.err.println("Error uploading file: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/files?error=upload";
        }
        return "redirect:/files";
    }

    @GetMapping("/files/share/{id}")
    public String shareFile(@PathVariable int id, Model model) {
        try {
            ResponseEntity<?> fileModel = fileService.shareFile(id);
            if (fileModel.hasBody()) {
                String currentUrl = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();
                model.addAttribute("shareUrl", currentUrl);
                model.addAttribute("file", fileModel.getBody());
                return "share-file";
            } else {
                return "redirect:/files?error=notfound";
            }
        } catch (Exception e) {
            System.err.println("Error sharing file: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/files?error=share";
        }
    }

    @PostMapping("/files/delete/{id}")
    public String deleteFile(@PathVariable int id, RedirectAttributes redirectAttributes) {
        // Debug log
        System.out.println("Delete request received for file ID: " + id);

        try {
            // Call service to delete file
            ResponseEntity<?> response = fileService.deleteFile(id);
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
        return "redirect:/files";
    }
}