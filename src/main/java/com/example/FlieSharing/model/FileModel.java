package com.example.FlieSharing.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileModel {

    private int id;
    private String filename;
    private String uploadedBy;
    private LocalDateTime dateTime;
    private LocalDateTime expiryTime;
    private byte[] fileData;

}
