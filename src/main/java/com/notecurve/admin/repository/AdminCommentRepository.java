package com.notecurve.admin.repository;

import com.notecurve.admin.domain.AdminComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AdminCommentRepository extends JpaRepository<AdminComment, Long> {
    
    @Modifying
    @Query("UPDATE AdminComment c SET c.userName = :newName WHERE c.userId = :userId")
    void updateUserNameByUserId(@Param("userId") Long userId, @Param("newName") String newName);

    long countByMessageBoardId(Long messageBoardId);

    @Modifying
    @Transactional
    void deleteByMessageBoardId(Long messageBoardId);
}
