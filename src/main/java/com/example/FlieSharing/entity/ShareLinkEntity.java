package com.example.FlieSharing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "share_links")
public class ShareLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "file_id")
    private Long fileId;

    // Renamed from uniqueId to shareIdentifier to explicitly match database column
    @Column(name = "share_identifier", nullable = false, unique = true)
    private String shareIdentifier; // This maps to the 'share_identifier' column

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private int downloadCount;

    @Column(name = "is_active", nullable = false) // Ensure this field exists and is handled
    private boolean isActive;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    // Renamed getter/setter to match new field name
    public String getShareIdentifier() {
        return shareIdentifier;
    }

    public void setShareIdentifier(String shareIdentifier) {
        this.shareIdentifier = shareIdentifier;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
