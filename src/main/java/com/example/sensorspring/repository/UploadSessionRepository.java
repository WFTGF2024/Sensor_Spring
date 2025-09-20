package com.example.sensorspring.repository;

import com.example.sensorspring.entity.UploadSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadSessionRepository extends JpaRepository<UploadSession, String> {}
