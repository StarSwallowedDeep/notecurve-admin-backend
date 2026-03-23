package com.notecurve.admin.repository;

import com.notecurve.admin.domain.AdminPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminPostRepository extends JpaRepository<AdminPost, Long> {
    
    @Modifying
    @Query("UPDATE AdminPost p SET p.userName = :newName WHERE p.userId = :userId")
    void updateUserNameByUserId(@Param("userId") Long userId, @Param("newName") String newName);
}
