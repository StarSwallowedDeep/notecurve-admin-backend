package com.notecurve.admin.repository;

import com.notecurve.admin.domain.AdminStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminStatsRepository extends JpaRepository<AdminStats, Long> {
}
