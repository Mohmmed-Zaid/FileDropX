package com.example.FlieSharing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/files")
public class FileController {

    @GetMapping("")
    public String home() {
        return "home"; // returns home.html
    }

    @GetMapping("/list")
    public String listFiles() {
        return "list-files"; // create list-files.html
    }

    @GetMapping("/share")
    public String shareFile() {
        return "share-file"; // create share-file.html
    }
}
