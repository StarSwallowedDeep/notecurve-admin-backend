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
import com.notecurve.kafka.producer.AdminEventProducer;

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
    private final AdminEventProducer adminEventProducer;

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

    // --- 동기화 (Feign GET 유지) ---
    @Transactional
    public void syncWithMainServer(String token) {
        try {
            log.info(">>>> [시작] 메인 서버 데이터 동기화 시도 중...");

            List<AdminUserDTO> mainUsers = authServiceClient.getAllUsers(token);
            if (mainUsers != null) {
                mainUsers.forEach(u -> userRepository.save(AdminUser.builder()
                        .id(u.getId()).loginId(u.getLoginId()).name(u.getName()).role(u.getRole()).build()));
            }

            List<AdminPostDTO> mainPosts = authServiceClient.getAllPosts(token);
            if (mainPosts != null) {
                mainPosts.forEach(p -> postRepository.save(AdminPost.builder()
                        .id(p.getId()).userId(p.getUserId()).title(p.getTitle()).userName(p.getUserName()).date(p.getDate()).build()));
            }

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

            refreshStats();
            log.info(">>>> [성공] 모든 데이터 동기화 완료!");
        } catch (Exception e) {
            log.error(">>>> [실패] 동기화 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("메인 서버 동기화에 실패했습니다.");
        }
    }

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

    @Transactional
    public void deleteUser(Long id) {
        AdminStats stats = getOrInitStats();

        // 통계 감소
        stats.decrementUsers();
        long commentCount = commentRepository.countByUserId(id);
        stats.decrementCommentsBy(commentCount);
        long postCount = postRepository.countByUserId(id);
        for (int i = 0; i < postCount; i++) stats.decrementPosts();
        long boardCount = messageBoardRepository.countByUserId(id);
        for (int i = 0; i < boardCount; i++) stats.decrementMessageBoards();
        adminStatsRepository.save(stats);

        // 관련 데이터 삭제
        commentRepository.deleteByUserId(id);
        messageBoardRepository.deleteByUserId(id);
        postRepository.deleteByUserId(id);
        userRepository.deleteById(id);

        adminEventProducer.sendAdminEvent("DELETED", "USER", id);
        log.info(">>>> [어드민] 유저 ID {} 삭제 이벤트 발행", id);
    }

    @Transactional
    public void updateUserRole(Long id, String role) {
        // 관리 서버 DB 즉시 처리
        userRepository.findById(id).ifPresent(user -> {
            userRepository.save(AdminUser.builder()
                    .id(user.getId())
                    .loginId(user.getLoginId())
                    .name(user.getName())
                    .role(role)
                    .build());
        });

        // 메인 서버에 카프카 이벤트 발행 (targetId에 id, type에 role 정보 포함)
        adminEventProducer.sendAdminRoleEvent(id, role);
        log.info(">>>> [어드민] 유저 ID {} 권한 변경 이벤트 발행 (role={})", id, role);
    }

    @Transactional
    public void deletePost(Long id) {
        AdminStats stats = getOrInitStats();
        stats.decrementPosts();
        adminStatsRepository.save(stats);
        postRepository.deleteById(id);

        adminEventProducer.sendAdminEvent("DELETED", "POST", id);
        log.info(">>>> [어드민] 게시글 ID {} 삭제 이벤트 발행", id);
    }

    @Transactional
    public void deleteMessageBoard(Long id) {
        AdminStats stats = getOrInitStats();
        stats.decrementMessageBoards();
        long commentCount = commentRepository.countByMessageBoardId(id);
        stats.decrementCommentsBy(commentCount);
        adminStatsRepository.save(stats);
        commentRepository.deleteByMessageBoardId(id);
        messageBoardRepository.deleteById(id);

        adminEventProducer.sendAdminEvent("DELETED", "MESSAGE_BOARD", id);
        log.info(">>>> [어드민] 메시지 보드 ID {} 삭제 이벤트 발행", id);
    }

    @Transactional
    public void deleteComment(Long id) {
        AdminStats stats = getOrInitStats();
        stats.decrementComments();
        adminStatsRepository.save(stats);
        commentRepository.deleteById(id);

        adminEventProducer.sendAdminEvent("DELETED", "COMMENT", id);
        log.info(">>>> [어드민] 댓글 ID {} 삭제 이벤트 발행", id);
    }

    private AdminStats getOrInitStats() {
        return adminStatsRepository.findById(1L)
                .orElseGet(() -> adminStatsRepository.save(AdminStats.init()));
    }
}
