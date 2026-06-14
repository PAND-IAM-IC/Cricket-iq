package com.cricketiq.cricketiq.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cricketiq.cricketiq.entity.Innings;

public interface InningsRepository extends JpaRepository<Innings, Long> {

}
