package com.notecurve.admin.repository;

import com.notecurve.admin.domain.AdminMessageBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminMessageBoardRepository extends JpaRepository<AdminMessageBoard, Long> {
    
    @Modifying
    @Query("UPDATE AdminMessageBoard b SET b.userName = :newName WHERE b.userId = :userId")
    void updateUserNameByUserId(@Param("userId") Long userId, @Param("newName") String newName);
}
