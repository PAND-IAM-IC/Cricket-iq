package com.cricketiq.cricketiq.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "delivery")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "innings_id")
    private Innings innings;

    @Column(name = "over_number")
    private int overNumber;

    @Column(name = "ball_number")
    private int ballNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "striker_id")
    private Player striker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "non_striker_id")
    private Player nonStriker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bowler_id")
    private Player bowler;

    @Column(name = "runs_off_bat")
    private int runsOffBat;

    private int extras;

    @Column(name = "extra_type")
    private String extraType;

    private boolean wicket;

    @Column(name = "dismissal_kind")
    private String dismissalKind;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_out_id")
    private Player playerOut;

}
