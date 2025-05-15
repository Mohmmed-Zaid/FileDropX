package com.example.FlieSharing.repository;

import com.example.FlieSharing.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long>{
    List<FileEntity> findByExpiryTimeBefore(LocalDateTime now);

}
