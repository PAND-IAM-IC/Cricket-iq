-- V1: initial cricket schema
-- Tables are ordered so each one's foreign-key targets already exist above it.

CREATE TABLE team (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(256) NOT NULL UNIQUE
);

CREATE TABLE player (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(256) NOT NULL,
    registry_id   VARCHAR(256) NOT NULL UNIQUE,
    batting_style VARCHAR(50),
    bowling_style VARCHAR(50),
    primary_role  VARCHAR(50)
);

CREATE TABLE cricket_match (
    id             BIGSERIAL PRIMARY KEY,
    match_date     DATE NOT NULL,
    season         VARCHAR(20),
    format         VARCHAR(20) NOT NULL,
    venue          VARCHAR(160) NOT NULL,
    city           VARCHAR(120),
    team1_id       BIGINT NOT NULL REFERENCES team(id),
    team2_id       BIGINT NOT NULL REFERENCES team(id),
    winner_team_id BIGINT REFERENCES team(id)        -- nullable: ties / no-result have no winner
);

CREATE TABLE innings (
    id              BIGSERIAL PRIMARY KEY,
    match_id        BIGINT NOT NULL REFERENCES cricket_match(id),
    batting_team_id BIGINT NOT NULL REFERENCES team(id),
    innings_number  INT NOT NULL
);

CREATE TABLE delivery (
    id             BIGSERIAL PRIMARY KEY,
    innings_id     BIGINT NOT NULL REFERENCES innings(id),
    over_number    INT NOT NULL,
    ball_number    INT NOT NULL,
    striker_id     BIGINT NOT NULL REFERENCES player(id),
    non_striker_id BIGINT NOT NULL REFERENCES player(id),
    bowler_id      BIGINT NOT NULL REFERENCES player(id),
    runs_off_bat   INT NOT NULL DEFAULT 0,
    extras         INT NOT NULL DEFAULT 0,
    extra_type     VARCHAR(20),
    wicket         BOOLEAN NOT NULL DEFAULT FALSE,
    dismissal_kind VARCHAR(40),
    player_out_id  BIGINT REFERENCES player(id)
);

CREATE INDEX idx_delivery_striker    ON delivery(striker_id);
CREATE INDEX idx_delivery_bowler     ON delivery(bowler_id);
CREATE INDEX idx_delivery_innings    ON delivery(innings_id);
CREATE INDEX idx_delivery_player_out ON delivery(player_out_id);