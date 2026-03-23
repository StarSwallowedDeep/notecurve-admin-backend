package com.notecurve.kafka.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MessageBoardEvent {
    private String type;
    private Long boardId;
    private Long userId;
    private String title;
    private String userName;
    private String createdAt;
}
