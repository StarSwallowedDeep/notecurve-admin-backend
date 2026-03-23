package com.notecurve.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCommentDTO {
    
    private Long id;
    private Long userId;
    private String content;
    private String userName;
    private Long messageBoardId;
    private String messageBoardTitle;
}
