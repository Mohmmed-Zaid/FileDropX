package com.example.FlieSharing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/files")
public class FileController {

    @GetMapping("")
    public String home() {
        return "home"; // maps to /files
    }

    @GetMapping("/list")
    public String listFiles() {
        return "list-files"; // maps to /files/list
    }

    @GetMapping("/share")
    public String shareFile() {
        return "share-file"; // maps to /files/share
    }
}
