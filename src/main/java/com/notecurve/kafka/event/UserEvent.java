package com.notecurve.kafka.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
    private String type;
    private Long userId;
    private String loginId;
    private String name;
    private String role;
}
