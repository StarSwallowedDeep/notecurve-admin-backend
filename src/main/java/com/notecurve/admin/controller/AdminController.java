package com.notecurve.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.notecurve.admin.dto.AdminUserDTO;
import com.notecurve.admin.dto.AdminPostDTO;
import com.notecurve.admin.dto.AdminCommentDTO;
import com.notecurve.admin.dto.AdminStatsDTO;
import com.notecurve.admin.dto.AdminMessageBoardDTO;
import com.notecurve.admin.service.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 토큰 추출 (sync 전용으로만 사용)
    private String getExtractToken() {
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        String token = (credentials != null) ? credentials.toString() : null;
        if (token == null || token.isEmpty()) return null;
        String pureToken = token.replace("Bearer ", "");
        return "token=" + pureToken;
    }

    // 데이터 전체 동기화 (Feign GET 유지)
    @PostMapping("/sync")
    public ResponseEntity<String> sync() {
        String token = getExtractToken();
        if (token == null) {
            return ResponseEntity.status(401).body("인증 토큰이 없습니다.");
        }
        adminService.syncWithMainServer(token);
        return ResponseEntity.ok("모든 데이터 동기화가 성공적으로 완료되었습니다!");
    }

    // --- 유저 관리 ---
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<String> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        adminService.updateUserRole(id, role);
        return ResponseEntity.ok("유저 권한이 변경되었습니다.");
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok("유저가 삭제되었습니다.");
    }

    // --- 게시글 관리 ---
    @GetMapping("/posts")
    public ResponseEntity<List<AdminPostDTO>> getAllPosts() {
        return ResponseEntity.ok(adminService.getAllPosts());
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Long id) {
        adminService.deletePost(id);
        return ResponseEntity.ok("게시글이 삭제되었습니다.");
    }

    // --- 메시지 보드 관리 ---
    @GetMapping("/message-boards")
    public ResponseEntity<List<AdminMessageBoardDTO>> getAllMessageBoards() {
        return ResponseEntity.ok(adminService.getAllMessageBoards());
    }

    @DeleteMapping("/message-boards/{id}")
    public ResponseEntity<String> deleteMessageBoard(@PathVariable Long id) {
        adminService.deleteMessageBoard(id);
        return ResponseEntity.ok("메시지 보드와 관련 댓글이 삭제되었습니다.");
    }

    // --- 댓글 관리 ---
    @GetMapping("/comments")
    public ResponseEntity<List<AdminCommentDTO>> getAllComments() {
        return ResponseEntity.ok(adminService.getAllComments());
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable Long id) {
        adminService.deleteComment(id);
        return ResponseEntity.ok("댓글이 삭제되었습니다.");
    }

    // --- 통계 ---
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
}
