package com.example.sensorspring.repository;

import com.example.sensorspring.entity.FileAsset;
import com.example.sensorspring.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FileAssetRepository extends JpaRepository<FileAsset, Long> {
    Page<FileAsset> findByOwner(User owner, Pageable pageable);
    Optional<FileAsset> findByIdAndOwner(Long id, User owner);
}
