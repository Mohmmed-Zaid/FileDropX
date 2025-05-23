package com.example.FlieSharing.service;

import com.example.FlieSharing.entity.ShareLinkEntity;
import com.example.FlieSharing.model.ShareLinkModel;
import com.example.FlieSharing.repository.ShareLinkRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShareLinkService {

    @Autowired
    private ShareLinkRepository shareLinkRepository;

    private ShareLinkModel convertToModel(ShareLinkEntity entity) {
        ShareLinkModel model = new ShareLinkModel();
        BeanUtils.copyProperties(entity, model);
        return model;
    }

    @Transactional
    public ShareLinkModel createShareLink(Long fileId, LocalDateTime expiryTime) {
        ShareLinkEntity shareLink = new ShareLinkEntity();
        shareLink.setFileId(fileId);
        shareLink.setShareIdentifier(UUID.randomUUID().toString()); // Set shareIdentifier
        shareLink.setCreatedAt(LocalDateTime.now());
        shareLink.setExpiresAt(expiryTime);
        shareLink.setDownloadCount(0); // Initialize download count
        shareLink.setActive(true); // Set isActive to true by default when creating

        ShareLinkEntity savedLink = shareLinkRepository.save(shareLink);
        System.out.println("ShareLinkService: Created share link for file ID " + fileId + " with identifier: " + savedLink.getShareIdentifier());
        return convertToModel(savedLink);
    }

    // Renamed from getShareLinkByUniqueId
    public ShareLinkModel getShareLinkByShareIdentifier(String shareIdentifier) {
        System.out.println("ShareLinkService: Attempting to retrieve share link by identifier: " + shareIdentifier);
        Optional<ShareLinkEntity> entityOptional = shareLinkRepository.findByShareIdentifier(shareIdentifier); // Call renamed repository method
        if (entityOptional.isPresent()) {
            System.out.println("ShareLinkService: Share link found for identifier: " + shareIdentifier);
        } else {
            System.out.println("ShareLinkService: Share link NOT found for identifier: " + shareIdentifier);
        }
        return entityOptional.map(this::convertToModel).orElse(null);
    }

    @Transactional
    public void incrementDownloadCount(String shareIdentifier) { // Parameter name updated for consistency
        System.out.println("ShareLinkService: Incrementing download count for identifier: " + shareIdentifier);
        shareLinkRepository.findByShareIdentifier(shareIdentifier).ifPresent(shareLink -> { // Call renamed repository method
            shareLink.setDownloadCount(shareLink.getDownloadCount() + 1);
            shareLinkRepository.save(shareLink);
            System.out.println("ShareLinkService: Download count incremented for " + shareIdentifier + ". New count: " + shareLink.getDownloadCount());
        });
    }

    @Transactional
    public void deleteShareLinksByFileId(Long fileId) {
        System.out.println("ShareLinkService: Deleting share links for file ID: " + fileId);
        shareLinkRepository.deleteByFileId(fileId);
        System.out.println("ShareLinkService: Share links deleted for file ID: " + fileId);
    }
}
