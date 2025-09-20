package com.example.sensorspring.repository;

import com.example.sensorspring.entity.FileBlob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileBlobRepository extends JpaRepository<FileBlob, String> {}
