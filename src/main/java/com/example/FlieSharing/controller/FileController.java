package com.example.FlieSharing.controller;

import com.example.FlieSharing.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;

@Controller
public class FileController {

    @Autowired
    private FileService fileService;

    // Root mapping to serve landing page
    @GetMapping("/")
    public String landing() {
        return "home"; // Will load templates/home.html
    }

    // Alternative access to home page
    @GetMapping("/home")
    public String home() {
        return "home"; // Will load templates/home.html
    }

    // File listing page
    @GetMapping("/files")
    public String listFiles(Model model) {
        model.addAttribute("files", fileService.getAll());
        return "list-files"; // Will load templates/list-files.html
    }

    // File upload endpoint
    @PostMapping("/files/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam("uploadedBy") String uploadedBy) throws IOException {
        fileService.uploadFile(file, uploadedBy);
        return "redirect:/files"; // Redirect to file listing after upload
    }

    // File sharing endpoint
    @GetMapping("/files/share/{id}")
    public String shareFile(@PathVariable int id, Model model) {
        ResponseEntity<?> fileModel = fileService.shareFile(id);

        if (fileModel.hasBody()) {
            // Generate share URL dynamically for the file
            String currentUrl = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();
            model.addAttribute("shareUrl", currentUrl);
            model.addAttribute("file", fileModel.getBody());
            return "share-file"; // Will load templates/share-file.html
        } else {
            return "redirect:/files"; // Redirect back to file list if file not found
        }
    }
}