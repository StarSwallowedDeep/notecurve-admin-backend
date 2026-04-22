package com.notecurve.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.notecurve.kafka.event.AdminEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ADMIN_TOPIC = "admin-events";

    // 삭제 이벤트 발행
    public void sendAdminEvent(String type, String target, Long targetId) {
        try {
            AdminEvent event = new AdminEvent(type, target, targetId, null);
            kafkaTemplate.send(ADMIN_TOPIC, event);
            log.info("AdminEvent 발행: type={}, target={}, targetId={}", type, target, targetId);
        } catch (Exception e) {
            log.warn("AdminEvent 발행 실패: {}", e.getMessage());
        }
    }

    // 권한 변경 이벤트 발행
    public void sendAdminRoleEvent(Long userId, String role) {
        try {
            AdminEvent event = new AdminEvent("ROLE_UPDATED", "USER", userId, role);
            kafkaTemplate.send(ADMIN_TOPIC, event);
            log.info("AdminEvent 발행: type=ROLE_UPDATED, userId={}, role={}", userId, role);
        } catch (Exception e) {
            log.warn("AdminEvent 발행 실패: {}", e.getMessage());
        }
    }
}
