package com.notecurve.admin.repository;

import com.notecurve.admin.domain.AdminPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AdminPostRepository extends JpaRepository<AdminPost, Long> {
    
    @Modifying
    @Query("UPDATE AdminPost p SET p.userName = :newName WHERE p.userId = :userId")
    void updateUserNameByUserId(@Param("userId") Long userId, @Param("newName") String newName);

    // 유저 삭제 시 사용
    @Modifying
    @Transactional
    void deleteByUserId(Long userId);

    long countByUserId(Long userId);
}
