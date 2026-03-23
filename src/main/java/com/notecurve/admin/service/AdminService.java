package com.notecurve.admin.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.notecurve.admin.domain.AdminStats;
import com.notecurve.admin.domain.AdminUser;
import com.notecurve.admin.domain.AdminPost;
import com.notecurve.admin.domain.AdminComment;
import com.notecurve.admin.domain.AdminMessageBoard;
import com.notecurve.admin.repository.AdminStatsRepository;
import com.notecurve.admin.repository.AdminUserRepository;
import com.notecurve.admin.repository.AdminPostRepository;
import com.notecurve.admin.repository.AdminCommentRepository;
import com.notecurve.admin.repository.AdminMessageBoardRepository;
import com.notecurve.admin.dto.AdminUserDTO;
import com.notecurve.admin.dto.AdminPostDTO;
import com.notecurve.admin.dto.AdminCommentDTO;
import com.notecurve.admin.dto.AdminStatsDTO;
import com.notecurve.admin.dto.AdminMessageBoardDTO;
import com.notecurve.admin.client.AuthServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminUserRepository userRepository;
    private final AdminPostRepository postRepository;
    private final AdminCommentRepository commentRepository;
    private final AdminMessageBoardRepository messageBoardRepository;
    private final AdminStatsRepository adminStatsRepository;
    private final AuthServiceClient authServiceClient;

    // --- 조회 로직 (관리자 DB 기준) ---
    public List<AdminUserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> AdminUserDTO.builder()
                        .id(u.getId()).loginId(u.getLoginId()).name(u.getName()).role(u.getRole()).build())
                .toList();
    }

    public List<AdminPostDTO> getAllPosts() {
        return postRepository.findAll().stream()
                .map(p -> AdminPostDTO.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .userName(p.getUserName())
                        .date(p.getDate())
                        .build())
                .toList();
    }

    public List<AdminMessageBoardDTO> getAllMessageBoards() {
        return messageBoardRepository.findAll().stream()
                .map(b -> AdminMessageBoardDTO.builder()
                        .id(b.getId())
                        .title(b.getTitle())
                        .userName(b.getUserName())
                        .createdAt(b.getCreatedAt())
                        .build())
                .toList();
    }

    public List<AdminCommentDTO> getAllComments() {
        return commentRepository.findAll().stream()
                .map(c -> AdminCommentDTO.builder()
                        .id(c.getId())
                        .content(c.getContent())
                        .userName(c.getUserName())
                        .messageBoardId(c.getMessageBoardId())
                        .messageBoardTitle(c.getMessageBoardTitle())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminStatsDTO getStats() {
        AdminStats stats = adminStatsRepository.findById(1L)
                .orElseGet(() -> adminStatsRepository.save(AdminStats.init()));
        return new AdminStatsDTO(stats.getTotalUsers(), stats.getTotalPosts(), stats.getTotalComments(), stats.getTotalMessageBoards());
    }

    // --- 동기화 및 관리 로직 ---
    @Transactional
    public void syncWithMainServer(String token) {
        try {
            log.info(">>>> [시작] 메인 서버 데이터 동기화 시도 중...");

            // 유저 동기화
            List<AdminUserDTO> mainUsers = authServiceClient.getAllUsers(token);
            if (mainUsers != null) {
                mainUsers.forEach(u -> userRepository.save(AdminUser.builder()
                        .id(u.getId()).loginId(u.getLoginId()).name(u.getName()).role(u.getRole()).build()));
            }

            // 게시글 동기화
            List<AdminPostDTO> mainPosts = authServiceClient.getAllPosts(token);
            if (mainPosts != null) {
                mainPosts.forEach(p -> postRepository.save(AdminPost.builder()
                        .id(p.getId()).userId(p.getUserId()).title(p.getTitle()).userName(p.getUserName()).date(p.getDate()).build()));
            }

            // 메시지 보드 동기화
            List<AdminMessageBoardDTO> mainBoards = authServiceClient.getAllMessageBoards(token);
            if (mainBoards != null) {
                mainBoards.forEach(b -> messageBoardRepository.save(AdminMessageBoard.builder()
                        .id(b.getId())
                        .userId(b.getUserId())
                        .title(b.getTitle())
                        .userName(b.getUserName())
                        .createdAt(b.getCreatedAt())
                        .build()));
            }

            // 댓글 동기화 
            List<AdminCommentDTO> mainComments = authServiceClient.getAllComments(token);
            if (mainComments != null) {
                mainComments.forEach(c -> commentRepository.save(AdminComment.builder()
                        .id(c.getId())
                        .userId(c.getUserId())
                        .content(c.getContent())
                        .userName(c.getUserName())
                        .messageBoardId(c.getMessageBoardId())
                        .messageBoardTitle(c.getMessageBoardTitle())
                        .build()));
            }

            // 통계 정보 최종 갱신
            refreshStats();

            log.info(">>>> [성공] FeignClient를 통한 모든 데이터 동기화 완료! 🥳");
        } catch (Exception e) {
            log.error(">>>> [실패] 동기화 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("메인 서버 동기화에 실패했습니다.");
        }
    }

    // DB의 실제 레코드 수를 세어 AdminStats 테이블을 최신화
    @Transactional
    public void refreshStats() {
        AdminStats stats = adminStatsRepository.findById(1L).orElseGet(AdminStats::init);
        stats.syncAllStats(
            userRepository.count(),
            postRepository.count(),
            commentRepository.count(),
            messageBoardRepository.count()
        );
        adminStatsRepository.save(stats);
    }

    public void deleteUser(String token, Long id) {
        authServiceClient.deleteUser(token, id);
    }

    public void updateUserRole(String token, Long id, String role) {
        authServiceClient.updateUserRole(token, id, role);
    }

    public void deletePost(String token, Long id) {
        authServiceClient.deletePost(token, id);
    }

    public void deleteMessageBoard(String token, Long id) {
        authServiceClient.deleteMessageBoard(token, id);
    }

    public void deleteComment(String token, Long id) {
        authServiceClient.deleteComment(token, id);
    }
}
