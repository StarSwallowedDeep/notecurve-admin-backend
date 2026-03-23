package com.notecurve.admin.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class AdminStats {

    @Id
    private Long id;
    private long totalUsers;
    private long totalPosts;
    private long totalComments;
    private long totalMessageBoards;

    public static AdminStats init() {
        AdminStats stats = new AdminStats();
        stats.id = 1L;
        stats.totalUsers = 0;
        stats.totalPosts = 0;
        stats.totalComments = 0;
        stats.totalMessageBoards = 0;
        return stats;
    }

    public void syncAllStats(long totalUsers, long totalPosts, long totalComments, long totalMessageBoards) {
        this.totalUsers = totalUsers;
        this.totalPosts = totalPosts;
        this.totalComments = totalComments;
        this.totalMessageBoards = totalMessageBoards;
    }

    public void incrementUsers() {
        this.totalUsers++;
    }

    public void decrementUsers() {
        if (this.totalUsers > 0) this.totalUsers--;
    }

    public void incrementPosts() {
        this.totalPosts++;
    }

    public void decrementPosts() {
        if (this.totalPosts > 0) this.totalPosts--;
    }

    public void incrementComments() {
        this.totalComments++;
    }

    public void decrementComments() {
        if (this.totalComments > 0) this.totalComments--;
    }

    public void decrementCommentsBy(long count) {
        this.totalComments = Math.max(0, this.totalComments - count);
    }
    
    public void incrementMessageBoards() {
        this.totalMessageBoards++;
    }

    public void decrementMessageBoards() {
        if (this.totalMessageBoards > 0) this.totalMessageBoards--;
    }
}
