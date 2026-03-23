package com.notecurve.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminStatsDTO {

    private long totalUsers;
    private long totalPosts;
    private long totalComments;
    private long totalMessageBoards;
}
