package com.cricketiq.cricketiq.ingestion;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CricsheetMatch(Info info, List<InningsJson> innings) {
    public CricsheetMatch {
        innings = innings == null ? List.of() : List.copyOf(innings);
    }

    public record Info(String city, List<String> dates, String matchType, Outcome outcome,
            String season, List<String> teams, String venue,
            Map<String, List<String>> players, Registry registry) {
        public Info {
            dates = dates == null ? List.of() : List.copyOf(dates);
            teams = teams == null ? List.of() : List.copyOf(teams);
            players = players == null ? Map.of() : Map.copyOf(players);
        }
    }

    public record Outcome(String winner) {
    }

    public record Registry(Map<String, String> people) {
        public Registry {
            people = people == null ? Map.of() : Map.copyOf(people);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InningsJson(String team, List<OverJson> overs) {
        public InningsJson {
            overs = overs == null ? List.of() : List.copyOf(overs);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OverJson(int over, List<DeliveryJson> deliveries) {
        public OverJson {
            deliveries = deliveries == null ? List.of() : List.copyOf(deliveries);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DeliveryJson(String batter, String bowler, String nonStriker, Runs runs, Map<String, Integer> extras,
            List<WicketJson> wickets) {
        public DeliveryJson {
            wickets = wickets == null ? List.of() : List.copyOf(wickets);
            extras = extras == null ? Map.of() : Map.copyOf(extras);
        }
    }

    public record Runs(int batter, int extras, int total) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WicketJson(String kind, String playerOut) {
    }
}
