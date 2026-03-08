package com.example.sensorspring.repository;

import com.example.sensorspring.entity.User;
import com.example.sensorspring.entity.UserMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {
    
    Optional<UserMembership> findByUser(User user);
    
    Optional<UserMembership> findByUserId(Long userId);
    
    @Query("SELECT um FROM UserMembership um JOIN FETCH um.level WHERE um.user = :user AND um.isActive = true")
    Optional<UserMembership> findActiveByUser(@Param("user") User user);
    
    @Query("SELECT um FROM UserMembership um JOIN FETCH um.level WHERE um.user.id = :userId AND um.isActive = true")
    Optional<UserMembership> findActiveByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COALESCE(SUM(um.storageUsed), 0) FROM UserMembership um WHERE um.isActive = true")
    Long getTotalStorageUsed();
    
    @Query("SELECT COUNT(um) FROM UserMembership um WHERE um.isActive = true AND um.level.id = :levelId")
    Long countByLevelId(@Param("levelId") Long levelId);
    
    boolean existsByUser(User user);
}
