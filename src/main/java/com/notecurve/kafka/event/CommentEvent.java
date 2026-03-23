package com.notecurve.kafka.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommentEvent {
    private String type;
    private Long commentId;
    private Long userId;
    private String content;
    private String userName;
    private Long messageBoardId;
    private String messageBoardTitle;
}
