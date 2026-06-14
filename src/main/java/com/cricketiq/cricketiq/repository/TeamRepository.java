package com.cricketiq.cricketiq.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cricketiq.cricketiq.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
    
}
