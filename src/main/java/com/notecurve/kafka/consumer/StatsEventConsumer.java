package com.notecurve.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
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
import com.notecurve.kafka.event.UserEvent;
import com.notecurve.kafka.event.PostEvent;
import com.notecurve.kafka.event.CommentEvent;
import com.notecurve.kafka.event.MessageBoardEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsEventConsumer {

    private final AdminStatsRepository adminStatsRepository;
    private final AdminUserRepository userRepository;
    private final AdminPostRepository postRepository;
    private final AdminCommentRepository commentRepository;
    private final AdminMessageBoardRepository messageBoardRepository;

    // 유저 가입 소식
    @Transactional
    @KafkaListener(topics = "user-events", groupId = "admin-service")
    public void consumeUserEvent(UserEvent event) {
        log.info(">>>> [어드민] 유저 이벤트 도착: {}", event);
        AdminStats stats = getOrInitStats();

        if ("CREATED".equals(event.getType())) {
            // 유저 생성 시
            stats.incrementUsers();
            userRepository.save(AdminUser.builder()
                    .id(event.getUserId())
                    .loginId(event.getLoginId())
                    .name(event.getName())
                    .role(event.getRole())
                    .build());
        }
        else if ("UPDATED".equals(event.getType())) {
            // 유저 이름이나 권한 변경 시 DB 정보만 갱신
            userRepository.findById(event.getUserId()).ifPresent(user -> {
                userRepository.save(AdminUser.builder()
                        .id(user.getId())
                        .loginId(user.getLoginId())
                        .name(event.getName())
                        .role(event.getRole())
                        .build());
                log.info(">>>> [어드민] 유저 ID {} 정보 업데이트 완료 (이름: {})", event.getUserId(), event.getName());
            });
            postRepository.updateUserNameByUserId(event.getUserId(), event.getName());
            commentRepository.updateUserNameByUserId(event.getUserId(), event.getName());
            messageBoardRepository.updateUserNameByUserId(event.getUserId(), event.getName());
        }
        else if ("DELETED".equals(event.getType())) {
            stats.decrementUsers(); // 통계 숫자 -1
            userRepository.deleteById(event.getUserId());
            
            // (선택 사항) 해당 유저가 쓴 글과 댓글도 관리자 DB에서 같이 지우고 싶다면 아래 추가
            // postRepository.deleteByUserId(event.getUserId()); 
            // commentRepository.deleteByUserId(event.getUserId());
            
            log.info(">>>> [어드민] 유저 ID {} 삭제 및 통계 반영 완료", event.getUserId());
        }
        
        adminStatsRepository.save(stats);
    }

    // 게시글 작성 소식
    @Transactional
    @KafkaListener(topics = "post-events", groupId = "admin-service")
    public void consumePostEvent(PostEvent event) {
        log.info(">>>> [어드민] 게시글 이벤트 도착: {}", event);
        AdminStats stats = getOrInitStats();

        if ("CREATED".equals(event.getType())) {
            stats.incrementPosts();
            postRepository.save(AdminPost.builder()
                    .id(event.getPostId())
                    .title(event.getTitle())
                    .userId(event.getUserId())
                    .userName(event.getUserName())
                    .date(event.getDate())
                    .build());
        } else if ("DELETED".equals(event.getType())) {
            stats.decrementPosts();
            postRepository.deleteById(event.getPostId());
        }
        adminStatsRepository.save(stats);
    }

    // 메시지 보드 작성 소식
    @Transactional
    @KafkaListener(topics = "message-board-events", groupId = "admin-service")
    public void consumeMessageBoardEvent(MessageBoardEvent event) {
        log.info(">>>> [어드민] 메시지 보드 이벤트 도착: {}", event);
        AdminStats stats = getOrInitStats();

        if ("CREATED".equals(event.getType())) {
            stats.incrementMessageBoards(); // 통계 +1
            messageBoardRepository.save(AdminMessageBoard.builder()
                    .id(event.getBoardId())
                    .userId(event.getUserId())
                    .title(event.getTitle())
                    .userName(event.getUserName())
                    .createdAt(event.getCreatedAt())
                    .build());
            log.info(">>>> [어드민] 게시판 ID {} 생성 및 통계 반영 완료", event.getBoardId());
            
        } else if ("DELETED".equals(event.getType())) {
            stats.decrementMessageBoards(); // 통계 -1
            long commentCount = commentRepository.countByMessageBoardId(event.getBoardId());
            stats.decrementCommentsBy(commentCount);
            commentRepository.deleteByMessageBoardId(event.getBoardId());
            messageBoardRepository.deleteById(event.getBoardId());
        }

        adminStatsRepository.save(stats);
    }

    // 댓글 작성 소식
    @Transactional
    @KafkaListener(topics = "comment-events", groupId = "admin-service")
    public void consumeCommentEvent(CommentEvent event) {
        log.info(">>>> [어드민] 댓글 이벤트 도착: {}", event);
        AdminStats stats = getOrInitStats();

        if ("CREATED".equals(event.getType())) {
            stats.incrementComments();
            commentRepository.save(AdminComment.builder()
                    .id(event.getCommentId())
                    .userId(event.getUserId())
                    .content(event.getContent())
                    .userName(event.getUserName())
                    .messageBoardId(event.getMessageBoardId()) 
                    .messageBoardTitle(event.getMessageBoardTitle())
                    .build());
        } else if ("DELETED".equals(event.getType())) {
            stats.decrementComments();
            commentRepository.deleteById(event.getCommentId());
        }
        adminStatsRepository.save(stats);
    }

    // 통계 데이터가 없으면 새로 만드는 도우미 메서드
    private AdminStats getOrInitStats() {
        return adminStatsRepository.findById(1L)
                .orElseGet(() -> adminStatsRepository.save(AdminStats.init()));
    }
}
