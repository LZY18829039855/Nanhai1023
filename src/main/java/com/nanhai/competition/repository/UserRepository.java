package com.nanhai.competition.repository;

import com.nanhai.competition.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    User findByUsername(String username);
    
    List<User> findByGroupType(String groupType);
    
    List<User> findBySubGroup(String subGroup);
    
    Long countByGroupType(String groupType);
    
    Long countBySubGroup(String subGroup);
}

