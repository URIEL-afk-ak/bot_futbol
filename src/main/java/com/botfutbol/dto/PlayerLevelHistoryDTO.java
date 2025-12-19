package com.botfutbol.dto;

public class PlayerLevelHistoryDTO {
    private String name;
    private int previousLevel;
    private double averageLevel;

    public PlayerLevelHistoryDTO() {}

    public PlayerLevelHistoryDTO(String name, int previousLevel, double averageLevel) {
        this.name = name;
        this.previousLevel = previousLevel;
        this.averageLevel = averageLevel;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPreviousLevel() { return previousLevel; }
    public void setPreviousLevel(int previousLevel) { this.previousLevel = previousLevel; }

    public double getAverageLevel() { return averageLevel; }
    public void setAverageLevel(double averageLevel) { this.averageLevel = averageLevel; }
}

