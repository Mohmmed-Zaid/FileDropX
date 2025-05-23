package com.example.FlieSharing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "files")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;

    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Lob // Indicates that this field should be stored as a large object (BLOB, BYTEA)
    @Column(name = "file_data", columnDefinition = "LONGBLOB", nullable = false) // Explicitly set for MySQL
    private byte[] fileData;


    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    @Override
    public String toString() {
        return "FileEntity{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", uploadedBy='" + uploadedBy + '\'' +
                ", uploadTime=" + uploadTime +
                ", expiryTime=" + expiryTime +
                '}';
    }
}
