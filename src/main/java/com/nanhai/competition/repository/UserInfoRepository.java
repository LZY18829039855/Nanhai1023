package com.nanhai.competition.repository;

import com.nanhai.competition.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户信息数据访问层
 */
@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    
    /**
     * 查找所有未删除的用户信息
     */
    @Query("SELECT u FROM UserInfo u WHERE u.isDeleted = 'N'")
    List<UserInfo> findAllActive();
    
    /**
     * 根据工号查找未删除的用户信息
     */
    @Query("SELECT u FROM UserInfo u WHERE u.employId = ?1 AND u.isDeleted = 'N'")
    Optional<UserInfo> findByEmployIdAndIsDeleted(String employId);
    
    /**
     * 根据英文名查找未删除的用户信息
     */
    @Query("SELECT u FROM UserInfo u WHERE u.userEngName = ?1 AND u.isDeleted = 'N'")
    Optional<UserInfo> findByUserEngNameAndIsDeleted(String userEngName);
    
    /**
     * 根据工号查找用户信息（包含已删除的）
     */
    UserInfo findByEmployId(String employId);
    
    /**
     * 根据英文名查找用户信息（包含已删除的）
     */
    UserInfo findByUserEngName(String userEngName);
    
    /**
     * 根据删除状态统计用户数量
     */
    Long countByIsDeleted(String isDeleted);
    
    /**
     * 根据组类型统计用户数量（仅未删除的用户）
     */
    @Query("SELECT COUNT(u) FROM UserInfo u WHERE u.groupType = ?1 AND u.isDeleted = 'N'")
    Long countByGroupType(String groupType);
    
    /**
     * 根据小组统计用户数量
     */
    Long countBySubGroup(String subGroup);
}
