package com.example.FlieSharing.repository;

import com.example.FlieSharing.entity.ShareLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ShareLinkRepository extends JpaRepository<ShareLinkEntity, Long> {
    // Renamed from findByUniqueId to match the new field name in ShareLinkEntity
    Optional<ShareLinkEntity> findByShareIdentifier(String shareIdentifier);

    @Modifying
    @Transactional
    @Query("DELETE FROM ShareLinkEntity sl WHERE sl.fileId = :fileId")
    void deleteByFileId(Long fileId);
}
