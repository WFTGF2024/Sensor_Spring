package com.example.sensorspring.repository;

import com.example.sensorspring.entity.MembershipLog;
import com.example.sensorspring.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembershipLogRepository extends JpaRepository<MembershipLog, Long> {
    
    Page<MembershipLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    Page<MembershipLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
