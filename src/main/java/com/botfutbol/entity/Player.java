package com.botfutbol.entity;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entidad que representa un jugador en el sistema.
 * Almacena información básica del jugador como nombre, habilidad y estado de pago.
 */
@Entity
@Table(name = "players")
public class Player {

    @Id
    private String id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "skill_level")
    private int skillLevel; // 1-10, para armar equipos balanceados

    @Column(name = "position", length = 10)
    private String position; // NUEVO: posición (POR, DEF, MED, DEL)

    @Column(name = "total_debt")
    private double totalDebt; // Deuda acumulada

    @Column(name = "total_paid")
    private double totalPaid; // Total pagado

    @Column(name = "games_played")
    private int gamesPlayed;

    @Column(name = "goals_scored")
    private int goalsScored;

    @Column(name = "attended")
    private boolean attended; // Si asistió al partido actual

    @Column(name = "activo")
    private boolean activo = true; // Por defecto activo

    public Player() {
        this.id = UUID.randomUUID().toString();
        this.skillLevel = 5; // Nivel medio por defecto
        this.position = "MED"; // Por defecto mediocampista
        this.totalDebt = 0;
        this.totalPaid = 0;
        this.gamesPlayed = 0;
        this.goalsScored = 0;
        this.attended = false;
        this.activo = true;
    }

    public Player(String name) {
        this();
        this.name = name;
    }

    public Player(String name, int skillLevel, String position) {
        this(name);
        this.skillLevel = skillLevel;
        this.position = position;
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

    public int getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(int skillLevel) {
        this.skillLevel = skillLevel;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getTotalDebt() {
        return totalDebt;
    }

    public void setTotalDebt(double totalDebt) {
        this.totalDebt = totalDebt;
    }

    public double getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(double totalPaid) {
        this.totalPaid = totalPaid;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getGoalsScored() {
        return goalsScored;
    }

    public void setGoalsScored(int goalsScored) {
        this.goalsScored = goalsScored;
    }

    public boolean isAttended() {
        return attended;
    }

    public void setAttended(boolean attended) {
        this.attended = attended;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", skillLevel=" + skillLevel +
                ", position='" + position + '\'' +
                ", totalDebt=" + totalDebt +
                ", totalPaid=" + totalPaid +
                ", gamesPlayed=" + gamesPlayed +
                ", goalsScored=" + goalsScored +
                '}';
    }
}
