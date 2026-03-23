package com.notecurve.kafka.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import java.time.LocalDate;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PostEvent {
    private String type;
    private Long postId;
    private Long userId;
    private String title;
    private String userName;
    private LocalDate date;
}
