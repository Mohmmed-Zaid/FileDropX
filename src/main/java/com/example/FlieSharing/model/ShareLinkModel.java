package com.example.FlieSharing.model;

import java.time.LocalDateTime;

    public class ShareLinkModel {
        private Long id;
        private Long fileId;
        private String uniqueId;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private Integer downloadCount = 0;

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

        public String getUniqueId() {
            return uniqueId;
        }

        public void setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
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

        public Integer getDownloadCount() {
            return downloadCount;
        }

        public void setDownloadCount(Integer downloadCount) {
            this.downloadCount = downloadCount;
        }

        @Override
        public String toString() {
            return "ShareLinkModel{" +
                    "id=" + id +
                    ", fileId=" + fileId +
                    ", uniqueId='" + uniqueId + '\'' +
                    ", createdAt=" + createdAt +
                    ", expiresAt=" + expiresAt +
                    ", downloadCount=" + downloadCount +
                    '}';
        }
}
