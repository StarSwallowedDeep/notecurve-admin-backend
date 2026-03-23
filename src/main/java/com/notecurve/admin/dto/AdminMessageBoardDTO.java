package com.notecurve.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminMessageBoardDTO {

    private Long id;
    private Long userId;
    private String title;
    private String userName;
    private String createdAt;
}
