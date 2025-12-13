package com.botfutbol.dto;

public class PlayerStatsDTO {
    private String name;
    private int goals;
    private int matches;
    private int assists;

    public PlayerStatsDTO() {}

    public PlayerStatsDTO(String name, int goals, int matches, int assists) {
        this.name = name;
        this.goals = goals;
        this.matches = matches;
        this.assists = assists;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getGoals() { return goals; }
    public void setGoals(int goals) { this.goals = goals; }

    public int getMatches() { return matches; }
    public void setMatches(int matches) { this.matches = matches; }

    public int getAssists() { return assists; }
    public void setAssists(int assists) { this.assists = assists; }
}