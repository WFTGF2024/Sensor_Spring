package com.example.sensorspring.repository;

import com.example.sensorspring.entity.FileAsset;
import com.example.sensorspring.entity.FileBlob;
import com.example.sensorspring.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface FileAssetRepository extends JpaRepository<FileAsset, Long> {
    Page<FileAsset> findByOwner(User owner, Pageable pageable);
    Optional<FileAsset> findByIdAndOwner(Long id, User owner);
    
    long countByOwner(User owner);
    
    boolean existsByOwnerAndBlob(User owner, FileBlob blob);
    
    @Query("SELECT COALESCE(SUM(f.blob.size), 0) FROM FileAsset f WHERE f.owner = :owner")
    long sumSizeByOwner(@Param("owner") User owner);
}
