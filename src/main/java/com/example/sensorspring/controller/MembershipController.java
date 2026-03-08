package com.example.sensorspring.controller;

import com.example.sensorspring.dto.MembershipInfoResponse;
import com.example.sensorspring.dto.UpgradeRequest;
import com.example.sensorspring.dto.RenewRequest;
import com.example.sensorspring.dto.SimpleResponse;
import com.example.sensorspring.entity.MembershipLevel;
import com.example.sensorspring.entity.MembershipLog;
import com.example.sensorspring.entity.User;
import com.example.sensorspring.entity.UserMembership;
import com.example.sensorspring.repository.UserRepository;
import com.example.sensorspring.service.MembershipService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会员控制器
 * 参照 Sensor_Server 的 membership_controller.py 实现
 */
@RestController
@RequestMapping("/api/membership")
public class MembershipController {
    
    private final MembershipService membershipService;
    private final UserRepository userRepository;
    
    public MembershipController(MembershipService membershipService, UserRepository userRepository) {
        this.membershipService = membershipService;
        this.userRepository = userRepository;
    }
    
    /**
     * 获取当前用户会员信息
     */
    @GetMapping("/info")
    public ResponseEntity<MembershipInfoResponse> getMembershipInfo(@AuthenticationPrincipal User user) {
        UserMembership membership = membershipService.getUserMembership(user);
        return ResponseEntity.ok(toMembershipInfoResponse(membership));
    }
    
    /**
     * 获取所有会员等级
     */
    @GetMapping("/levels")
    public ResponseEntity<List<MembershipLevel>> listMembershipLevels() {
        List<MembershipLevel> levels = membershipService.getAllLevels();
        return ResponseEntity.ok(levels);
    }
    
    /**
     * 升级会员
     */
    @PostMapping("/upgrade")
    public ResponseEntity<MembershipInfoResponse> upgradeMembership(
            @AuthenticationPrincipal User user,
            @RequestBody UpgradeRequest request) {
        UserMembership membership = membershipService.upgradeMembership(
            user, 
            request.getLevelId(), 
            request.getDurationDays()
        );
        return ResponseEntity.ok(toMembershipInfoResponse(membership));
    }
    
    /**
     * 续费会员
     */
    @PostMapping("/renew")
    public ResponseEntity<MembershipInfoResponse> renewMembership(
            @AuthenticationPrincipal User user,
            @RequestBody RenewRequest request) {
        UserMembership membership = membershipService.renewMembership(
            user, 
            request.getDurationDays()
        );
        return ResponseEntity.ok(toMembershipInfoResponse(membership));
    }
    
    /**
     * 取消自动续费
     */
    @PostMapping("/cancel")
    public ResponseEntity<SimpleResponse> cancelAutoRenew(@AuthenticationPrincipal User user) {
        membershipService.cancelAutoRenew(user);
        return ResponseEntity.ok(new SimpleResponse("已取消自动续费"));
    }
    
    /**
     * 获取会员历史
     */
    @GetMapping("/history")
    public ResponseEntity<Page<MembershipLog>> getMembershipHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<MembershipLog> history = membershipService.getMembershipHistory(user, page, size);
        return ResponseEntity.ok(history);
    }
    
    /**
     * 初始化会员等级（管理员接口）
     */
    @PostMapping("/init")
    public ResponseEntity<SimpleResponse> initMembershipLevels() {
        membershipService.initMembershipLevels();
        return ResponseEntity.ok(new SimpleResponse("会员等级初始化完成"));
    }
    
    /**
     * 转换为会员信息响应
     */
    private MembershipInfoResponse toMembershipInfoResponse(UserMembership membership) {
        MembershipInfoResponse response = new MembershipInfoResponse();
        response.setMembershipId(membership.getId());
        response.setUserId(membership.getUser().getId());
        response.setLevelId(membership.getLevel().getId());
        response.setLevelName(membership.getLevel().getLevelName());
        response.setLevelCode(membership.getLevel().getLevelCode());
        response.setStorageLimit(membership.getLevel().getStorageLimit());
        response.setStorageLimitFormatted(formatBytes(membership.getLevel().getStorageLimit()));
        response.setMaxFileSize(membership.getLevel().getMaxFileSize());
        response.setMaxFileSizeFormatted(formatBytes(membership.getLevel().getMaxFileSize()));
        response.setMaxFileCount(membership.getLevel().getMaxFileCount());
        response.setStorageUsed(membership.getStorageUsed());
        response.setStorageUsedFormatted(formatBytes(membership.getStorageUsed()));
        response.setFileCount(membership.getFileCount());
        response.setStorageUsagePercentage(membership.getStorageUsagePercentage());
        response.setStorageFull(membership.isStorageFull());
        response.setStartDate(membership.getStartDate());
        response.setEndDate(membership.getEndDate());
        response.setEndDateFormatted(membership.isPermanent() ? "永久" : 
            (membership.getEndDate() != null ? membership.getEndDate().toString() : "永久"));
        response.setActive(membership.getIsActive());
        response.setCanShareFiles(membership.getLevel().getCanShareFiles());
        response.setCanCreatePublicLinks(membership.getLevel().getCanCreatePublicLinks());
        return response;
    }
    
    /**
     * 格式化字节数
     */
    private String formatBytes(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", size, units[unitIndex]);
    }
}
