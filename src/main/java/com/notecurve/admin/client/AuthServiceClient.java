package com.notecurve.admin.client;

import com.notecurve.admin.dto.AdminUserDTO;
import com.notecurve.admin.dto.AdminPostDTO;
import com.notecurve.admin.dto.AdminCommentDTO;
import com.notecurve.admin.dto.AdminMessageBoardDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "auth-service", url = "${main.server.url}")
public interface AuthServiceClient {

    // --- 유저 관리 ---
    @GetMapping("/api/users/internal/all")
    List<AdminUserDTO> getAllUsers(@RequestHeader("Cookie") String cookie);

    @DeleteMapping("/api/users/internal/{id}")
    void deleteUser(@RequestHeader("Cookie") String cookie, 
                    @PathVariable("id") Long id);

    @PatchMapping("/api/users/internal/{id}/role")
    void updateUserRole(
        @RequestHeader("Cookie") String cookie, 
        @PathVariable("id") Long id, 
        @RequestParam("role") String role
    );

    // --- 게시글 관리 ---
    @GetMapping("/api/posts/internal/all")
    List<AdminPostDTO> getAllPosts(@RequestHeader("Cookie") String cookie);

    @DeleteMapping("/api/posts/internal/{id}")
    void deletePost(@RequestHeader("Cookie") String cookie, 
                    @PathVariable("id") Long id);

    // --- 메시지 보드 관리 ---
    @GetMapping("/api/message-boards/internal/all")
    List<AdminMessageBoardDTO> getAllMessageBoards(@RequestHeader("Cookie") String cookie);

    @DeleteMapping("/api/message-boards/internal/{id}")
    void deleteMessageBoard(@RequestHeader("Cookie") String cookie, 
                            @PathVariable("id") Long id);

    // --- 댓글 관리 ---
    @GetMapping("/api/comments/internal/all")
    List<AdminCommentDTO> getAllComments(@RequestHeader("Cookie") String cookie);

    @DeleteMapping("/api/comments/internal/{id}")
    void deleteComment(@RequestHeader("Cookie") String cookie, 
                       @PathVariable("id") Long id);
}
