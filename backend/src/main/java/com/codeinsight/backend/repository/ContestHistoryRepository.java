package com.codeinsight.backend.repository;

import com.codeinsight.backend.entity.ContestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContestHistoryRepository extends JpaRepository<ContestHistory, Long> {
    List<ContestHistory> findByUserId(Long userId);
    List<ContestHistory> findByUserIdAndPlatform(Long userId, String platform);
    List<ContestHistory> findByUserIdOrderByContestDateDesc(Long userId);
    List<ContestHistory> findByUserIdAndContestDateBetweenOrderByContestDateDesc(Long userId, LocalDate startDate, LocalDate endDate);
}
