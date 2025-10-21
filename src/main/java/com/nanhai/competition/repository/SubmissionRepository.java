package com.nanhai.competition.repository;

import com.nanhai.competition.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 提交记录Repository
 */
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    
    /**
     * 根据用户ID查找提交记录
     */
    List<Submission> findByUserId(Long userId);
    
    /**
     * 根据分支名称查找提交记录
     */
    List<Submission> findByBranch(String branch);
    
    /**
     * 根据通过用例数范围查找提交记录
     */
    List<Submission> findByPassedGreaterThanEqual(Integer minPassed);
    
    /**
     * 根据通过用例数查找提交记录，按完成时间升序排序
     */
    List<Submission> findByPassedOrderByCompletionTimeAsc(Integer passed);
    
    /**
     * 根据完成时间范围查找提交记录
     */
    List<Submission> findByCompletionTimeLessThanEqual(Integer maxCompletionTime);
    
    /**
     * 统计用户的提交记录数量
     */
    Long countByUserId(Long userId);
    
    /**
     * 计算平均完成时间
     */
    @Query("SELECT AVG(s.completionTime) FROM Submission s WHERE s.completionTime IS NOT NULL")
    Double calculateAverageCompletionTime();
    
    /**
     * 计算平均通过用例数
     */
    @Query("SELECT AVG(s.passed) FROM Submission s WHERE s.passed IS NOT NULL")
    Double calculateAveragePassed();
    
    /**
     * 获取每个用户的最好通过用例数
     */
    @Query("SELECT s.userId, MAX(s.passed) FROM Submission s WHERE s.passed IS NOT NULL GROUP BY s.userId")
    List<Object[]> findMaxPassedByUser();
    
    /**
     * 获取AI组用户的最好通过用例数总和
     */
    @Query(value = "SELECT COALESCE(SUM(max_passed), 0) FROM (" +
           "SELECT MAX(s.passed) as max_passed " +
           "FROM submissions s " +
           "INNER JOIN user_info u ON s.user_id = u.id " +
           "WHERE u.group_type = 'AI组' AND s.passed IS NOT NULL " +
           "GROUP BY s.user_id" +
           ") as user_max", nativeQuery = true)
    Long getAiGroupMaxPassedSum();
    
    /**
     * 获取非AI组用户的最好通过用例数总和
     */
    @Query(value = "SELECT COALESCE(SUM(max_passed), 0) FROM (" +
           "SELECT MAX(s.passed) as max_passed " +
           "FROM submissions s " +
           "INNER JOIN user_info u ON s.user_id = u.id " +
           "WHERE u.group_type = '非AI组' AND s.passed IS NOT NULL " +
           "GROUP BY s.user_id" +
           ") as user_max", nativeQuery = true)
    Long getNonAiGroupMaxPassedSum();
    
    /**
     * 获取所有用户的最好通过用例数总和
     */
    @Query(value = "SELECT COALESCE(SUM(max_passed), 0) FROM (" +
           "SELECT MAX(s.passed) as max_passed " +
           "FROM submissions s " +
           "INNER JOIN user_info u ON s.user_id = u.id " +
           "WHERE s.passed IS NOT NULL " +
           "GROUP BY s.user_id" +
           ") as user_max", nativeQuery = true)
    Long getAllUsersMaxPassedSum();
    
        /**
         * 获取指定小组的最好通过用例数总和
         */
        @Query(value = "SELECT COALESCE(SUM(max_passed), 0) FROM (" +
               "SELECT MAX(s.passed) as max_passed " +
               "FROM submissions s " +
               "INNER JOIN user_info u ON s.user_id = u.id " +
               "WHERE u.sub_group = ?1 AND s.passed IS NOT NULL " +
               "GROUP BY s.user_id" +
               ") as user_max", nativeQuery = true)
        Long getSubGroupMaxPassedSum(String subGroup);
        
        /**
         * 获取AI组全部通过（通过数为20）的平均用时
         */
        @Query(value = "SELECT AVG(s.completion_time) FROM submissions s " +
               "INNER JOIN user_info u ON s.user_id = u.id " +
               "WHERE u.group_type = 'AI组' AND s.passed = 20", nativeQuery = true)
        Double getAiGroupFullPassAverageTime();
        
        /**
         * 获取非AI组全部通过（通过数为20）的平均用时
         */
        @Query(value = "SELECT AVG(s.completion_time) FROM submissions s " +
               "INNER JOIN user_info u ON s.user_id = u.id " +
               "WHERE u.group_type = '非AI组' AND s.passed = 20", nativeQuery = true)
        Double getNonAiGroupFullPassAverageTime();
        
        /**
         * 获取指定小组中通过测试的人数（通过用例数为20的人数）
         */
        @Query(value = "SELECT COUNT(DISTINCT s.user_id) FROM submissions s " +
               "INNER JOIN user_info u ON s.user_id = u.id " +
               "WHERE u.sub_group = ?1 AND s.passed = 20", nativeQuery = true)
        Long getSubGroupFullPassUserCount(String subGroup);
        
        /**
         * 获取指定小组中全部通过（通过用例数为20）的平均用时
         */
        @Query(value = "SELECT AVG(s.completion_time) FROM submissions s " +
               "INNER JOIN user_info u ON s.user_id = u.id " +
               "WHERE u.sub_group = ?1 AND s.passed = 20", nativeQuery = true)
        Double getSubGroupFullPassAverageTime(String subGroup);
    
    /**
     * 获取指定小组的用户数量
     */
    @Query("SELECT COUNT(u) FROM UserInfo u WHERE u.subGroup = ?1 AND u.isDeleted = 'N'")
    Long countBySubGroup(String subGroup);

    /**
     * 按组别获取全部通过（passed=20）人员最早用时（每人一条，按用时升序）
     */
    @Query(value = "SELECT s.user_id, MIN(s.completion_time) AS min_time, MIN(s.submit_time) AS min_submit_time " +
           "FROM submissions s " +
           "INNER JOIN user_info u ON s.user_id = u.id " +
           "WHERE u.group_type = ?1 AND s.passed = 20 " +
           "GROUP BY s.user_id " +
           "ORDER BY min_time ASC", nativeQuery = true)
    List<Object[]> findFullPassUsersByGroup(String groupType);
    
    /**
     * 获取最近的提交记录（按提交时间降序）
     */
    List<Submission> findTop10ByOrderBySubmitTimeDesc();
}

