package com.cricketiq.cricketiq.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "player")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "registry_id")
    private String registryId;

    @Column(name = "batting_style")
    private String battingStyle;

    @Column(name = "bowling_style")
    private String bowlingStyle;

    @Column(name = "primary_role")
    private String primaryRole;
}
