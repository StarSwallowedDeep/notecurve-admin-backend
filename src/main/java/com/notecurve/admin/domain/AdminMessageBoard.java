package com.notecurve.admin.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminMessageBoard {
    @Id
    private Long id;
    private Long userId;
    private String title;
    private String userName;
    private String createdAt;
}
