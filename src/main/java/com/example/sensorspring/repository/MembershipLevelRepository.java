package com.example.sensorspring.repository;

import com.example.sensorspring.entity.MembershipLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipLevelRepository extends JpaRepository<MembershipLevel, Long> {
    
    Optional<MembershipLevel> findByLevelCode(String levelCode);
    
    List<MembershipLevel> findByIsActiveTrueOrderByDisplayOrderAscPriorityAsc();
    
    Optional<MembershipLevel> findFirstByIsActiveTrueOrderByDisplayOrderAscPriorityAsc();
    
    boolean existsByLevelCode(String levelCode);
}
