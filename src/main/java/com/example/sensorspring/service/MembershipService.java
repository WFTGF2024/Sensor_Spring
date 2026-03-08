package com.example.sensorspring.service;

import com.example.sensorspring.entity.MembershipLevel;
import com.example.sensorspring.entity.MembershipLog;
import com.example.sensorspring.entity.User;
import com.example.sensorspring.entity.UserMembership;
import com.example.sensorspring.exception.BadRequestException;
import com.example.sensorspring.exception.NotFoundException;
import com.example.sensorspring.repository.MembershipLevelRepository;
import com.example.sensorspring.repository.MembershipLogRepository;
import com.example.sensorspring.repository.UserMembershipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * 会员服务
 * 参照 Sensor_Server 的 membership_service.py 实现
 */
@Service
public class MembershipService {
    
    private static final Logger logger = LoggerFactory.getLogger(MembershipService.class);
    
    private final MembershipLevelRepository levelRepository;
    private final UserMembershipRepository membershipRepository;
    private final MembershipLogRepository logRepository;
    
    public MembershipService(MembershipLevelRepository levelRepository,
                           UserMembershipRepository membershipRepository,
                           MembershipLogRepository logRepository) {
        this.levelRepository = levelRepository;
        this.membershipRepository = membershipRepository;
        this.logRepository = logRepository;
    }
    
    /**
     * 获取用户会员信息
     */
    public UserMembership getUserMembership(User user) {
        return membershipRepository.findActiveByUser(user)
            .orElseGet(() -> createDefaultMembership(user));
    }
    
    /**
     * 获取用户会员信息（按ID）
     */
    public UserMembership getUserMembership(Long userId) {
        return membershipRepository.findActiveByUserId(userId)
            .orElseGet(() -> {
                User user = new User();
                user.setId(userId);
                return createDefaultMembership(user);
            });
    }
    
    /**
     * 创建默认会员（免费用户）
     */
    @Transactional
    private UserMembership createDefaultMembership(User user) {
        MembershipLevel defaultLevel = levelRepository.findByLevelCode("free")
            .orElseGet(this::createDefaultFreeLevel);
        
        UserMembership membership = new UserMembership();
        membership.setUser(user);
        membership.setLevel(defaultLevel);
        membership.setStartDate(Instant.now());
        membership.setEndDate(null); // 永久
        membership.setIsActive(true);
        membership.setStorageUsed(0L);
        membership.setFileCount(0);
        
        return membershipRepository.save(membership);
    }
    
    /**
     * 创建默认免费等级
     */
    @Transactional
    public MembershipLevel createDefaultFreeLevel() {
        if (levelRepository.existsByLevelCode("free")) {
            return levelRepository.findByLevelCode("free").orElseThrow();
        }
        
        MembershipLevel free = new MembershipLevel();
        free.setLevelName("普通用户");
        free.setLevelCode("free");
        free.setDisplayOrder(1);
        free.setDescription("免费用户，基础文件存储");
        free.setStorageLimit(1073741824L); // 1GB
        free.setMaxFileSize(52428800L); // 50MB
        free.setMaxFileCount(100);
        free.setDownloadSpeedLimit(0L);
        free.setUploadSpeedLimit(0L);
        free.setDailyDownloadLimit(0);
        free.setDailyUploadLimit(0);
        free.setCanShareFiles(true);
        free.setCanCreatePublicLinks(false);
        free.setPriority(1);
        free.setIsActive(true);
        
        return levelRepository.save(free);
    }
    
    /**
     * 初始化会员等级数据
     */
    @Transactional
    public void initMembershipLevels() {
        if (levelRepository.count() > 0) {
            return;
        }
        
        // 免费用户
        createDefaultFreeLevel();
        
        // 白银会员
        MembershipLevel silver = new MembershipLevel();
        silver.setLevelName("白银会员");
        silver.setLevelCode("silver");
        silver.setDisplayOrder(2);
        silver.setDescription("白银会员，支持文件分享");
        silver.setStorageLimit(5368709120L); // 5GB
        silver.setMaxFileSize(104857600L); // 100MB
        silver.setMaxFileCount(500);
        silver.setDownloadSpeedLimit(0L);
        silver.setUploadSpeedLimit(0L);
        silver.setDailyDownloadLimit(0);
        silver.setDailyUploadLimit(0);
        silver.setCanShareFiles(true);
        silver.setCanCreatePublicLinks(false);
        silver.setPriority(2);
        silver.setIsActive(true);
        levelRepository.save(silver);
        
        // 黄金会员
        MembershipLevel gold = new MembershipLevel();
        gold.setLevelName("黄金会员");
        gold.setLevelCode("gold");
        gold.setDisplayOrder(3);
        gold.setDescription("黄金会员，支持公开链接");
        gold.setStorageLimit(10737418240L); // 10GB
        gold.setMaxFileSize(209715200L); // 200MB
        gold.setMaxFileCount(1000);
        gold.setDownloadSpeedLimit(0L);
        gold.setUploadSpeedLimit(0L);
        gold.setDailyDownloadLimit(100);
        gold.setDailyUploadLimit(0);
        gold.setCanShareFiles(true);
        gold.setCanCreatePublicLinks(true);
        gold.setPriority(3);
        gold.setIsActive(true);
        levelRepository.save(gold);
        
        // 钻石会员
        MembershipLevel diamond = new MembershipLevel();
        diamond.setLevelName("钻石会员");
        diamond.setLevelCode("diamond");
        diamond.setDisplayOrder(4);
        diamond.setDescription("钻石会员，最高权限");
        diamond.setStorageLimit(53687091200L); // 50GB
        diamond.setMaxFileSize(1073741824L); // 1GB
        diamond.setMaxFileCount(10000);
        diamond.setDownloadSpeedLimit(0L);
        diamond.setUploadSpeedLimit(0L);
        diamond.setDailyDownloadLimit(1000);
        diamond.setDailyUploadLimit(500);
        diamond.setCanShareFiles(true);
        diamond.setCanCreatePublicLinks(true);
        diamond.setPriority(4);
        diamond.setIsActive(true);
        levelRepository.save(diamond);
        
        logger.info("会员等级初始化完成");
    }
    
    /**
     * 获取所有会员等级
     */
    public List<MembershipLevel> getAllLevels() {
        return levelRepository.findByIsActiveTrueOrderByDisplayOrderAscPriorityAsc();
    }
    
    /**
     * 升级会员
     */
    @Transactional
    public UserMembership upgradeMembership(User user, Long levelId, Integer durationDays) {
        MembershipLevel newLevel = levelRepository.findById(levelId)
            .orElseThrow(() -> new NotFoundException("会员等级不存在"));
        
        Optional<UserMembership> existingOpt = membershipRepository.findActiveByUser(user);
        MembershipLevel oldLevel = existingOpt.map(UserMembership::getLevel).orElse(null);
        
        UserMembership membership;
        if (existingOpt.isPresent()) {
            membership = existingOpt.get();
            membership.setLevel(newLevel);
            membership.setStartDate(Instant.now());
            membership.setIsActive(true);
        } else {
            membership = createDefaultMembership(user);
            membership.setLevel(newLevel);
            membership.setStartDate(Instant.now());
        }
        
        // 设置结束时间
        if (durationDays != null && durationDays > 0) {
            membership.setEndDate(Instant.now().plus(durationDays, ChronoUnit.DAYS));
        } else {
            membership.setEndDate(null); // 永久
        }
        
        membership = membershipRepository.save(membership);
        
        // 记录日志
        createLog(user, "upgrade", 
            "升级到 " + newLevel.getLevelName() + " (" + newLevel.getLevelCode() + ")",
            oldLevel, newLevel, user);
        
        logger.info("用户 {} 升级会员到 {}", user.getId(), newLevel.getLevelCode());
        return membership;
    }
    
    /**
     * 续费会员
     */
    @Transactional
    public UserMembership renewMembership(User user, Integer durationDays) {
        UserMembership membership = membershipRepository.findActiveByUser(user)
            .orElseThrow(() -> new BadRequestException("您还没有会员，请先开通会员"));
        
        if (membership.isPermanent()) {
            return membership; // 永久会员不需要续费
        }
        
        Instant newEndDate;
        if (membership.getEndDate() != null && membership.getEndDate().isAfter(Instant.now())) {
            newEndDate = membership.getEndDate().plus(durationDays, ChronoUnit.DAYS);
        } else {
            newEndDate = Instant.now().plus(durationDays, ChronoUnit.DAYS);
        }
        
        membership.setEndDate(newEndDate);
        membership = membershipRepository.save(membership);
        
        // 记录日志
        createLog(user, "renew", "续费 " + durationDays + " 天",
            membership.getLevel(), membership.getLevel(), user);
        
        logger.info("用户 {} 续费会员 {} 天", user.getId(), durationDays);
        return membership;
    }
    
    /**
     * 取消自动续费
     */
    @Transactional
    public UserMembership cancelAutoRenew(User user) {
        UserMembership membership = membershipRepository.findActiveByUser(user)
            .orElseThrow(() -> new BadRequestException("您还没有会员"));
        
        membership.setAutoRenew(false);
        return membershipRepository.save(membership);
    }
    
    /**
     * 更新存储使用量
     */
    @Transactional
    public void updateStorageUsage(User user, Long fileSizeDelta, int fileCountDelta) {
        UserMembership membership = membershipRepository.findByUser(user)
            .orElseGet(() -> createDefaultMembership(user));
        
        membership.setStorageUsed(Math.max(0, membership.getStorageUsed() + fileSizeDelta));
        membership.setFileCount(Math.max(0, membership.getFileCount() + fileCountDelta));
        
        membershipRepository.save(membership);
    }
    
    /**
     * 检查存储限制
     */
    public void checkStorageLimit(User user, Long fileSize) {
        UserMembership membership = getUserMembership(user);
        MembershipLevel level = membership.getLevel();
        
        // 检查文件大小限制
        if (fileSize > level.getMaxFileSize()) {
            throw new BadRequestException(
                String.format("文件大小超过限制。当前文件: %.2fMB，最大允许: %.2fMB",
                    fileSize / 1024.0 / 1024.0,
                    level.getMaxFileSize() / 1024.0 / 1024.0));
        }
        
        // 检查存储容量限制
        if (membership.getStorageUsed() + fileSize > level.getStorageLimit()) {
            throw new BadRequestException(
                String.format("存储空间不足。当前使用: %.2fMB，限制: %.2fMB",
                    membership.getStorageUsed() / 1024.0 / 1024.0,
                    level.getStorageLimit() / 1024.0 / 1024.0));
        }
        
        // 检查文件数量限制
        if (membership.getFileCount() >= level.getMaxFileCount()) {
            throw new BadRequestException(
                String.format("文件数量已达到上限。当前: %d个，最大: %d个",
                    membership.getFileCount(), level.getMaxFileCount()));
        }
    }
    
    /**
     * 获取会员历史
     */
    public Page<MembershipLog> getMembershipHistory(User user, int page, int size) {
        return logRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(page, size));
    }
    
    /**
     * 创建会员日志
     */
    private void createLog(User user, String actionType, String actionDetail,
                          MembershipLevel oldLevel, MembershipLevel newLevel, User operator) {
        MembershipLog log = new MembershipLog();
        log.setUser(user);
        log.setActionType(actionType);
        log.setActionDetail(actionDetail);
        log.setOldLevel(oldLevel);
        log.setNewLevel(newLevel);
        log.setOperator(operator);
        logRepository.save(log);
    }
}
