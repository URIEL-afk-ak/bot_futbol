package com.botfutbol.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un equipo en un partido.
 * Contiene la lista de jugadores asignados al equipo.
 */
public class Team {
    
    private String id;
    private String name; // Ej: "Equipo A", "Equipo B"
    private List<Player> players;
    private int goals;
    
    public Team() {
        this.players = new ArrayList<>();
        this.goals = 0;
    }
    
    public Team(String id, String name) {
        this();
        this.id = id;
        this.name = name;
    }
    
    public void addPlayer(Player player) {
        this.players.add(player);
    }
    
    public void removePlayer(Player player) {
        this.players.remove(player);
    }
    
    public int getTotalSkillLevel() {
        return players.stream()
                .mapToInt(Player::getSkillLevel)
                .sum();
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<Player> getPlayers() {
        return players;
    }
    
    public void setPlayers(List<Player> players) {
        this.players = players;
    }
    
    public int getGoals() {
        return goals;
    }
    
    public void setGoals(int goals) {
        this.goals = goals;
    }
    
    @Override
    public String toString() {
        return "Team{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", players=" + players.size() +
                ", goals=" + goals +
                '}';
    }
}
