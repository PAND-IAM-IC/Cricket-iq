package com.cricketiq.cricketiq.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cricketiq.cricketiq.entity.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}
