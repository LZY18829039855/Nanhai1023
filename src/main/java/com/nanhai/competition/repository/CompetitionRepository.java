package com.nanhai.competition.repository;

import com.nanhai.competition.entity.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 比赛Repository
 */
@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {
}

