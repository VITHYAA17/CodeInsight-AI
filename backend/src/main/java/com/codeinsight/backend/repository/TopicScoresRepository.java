package com.codeinsight.backend.repository;

import com.codeinsight.backend.entity.TopicScores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicScoresRepository extends JpaRepository<TopicScores, Long> {
    Optional<TopicScores> findByUserIdAndTopicName(Long userId, String topicName);
    List<TopicScores> findByUserId(Long userId);
    List<TopicScores> findByUserIdOrderByStrengthScoreDesc(Long userId);
}
