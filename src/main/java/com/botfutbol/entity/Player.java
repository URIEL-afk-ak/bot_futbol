package com.botfutbol.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(
    name = "players",
    uniqueConstraints = @UniqueConstraint(name = "uk_player_name_user", columnNames = {"name", "user_id"})
)
public class Player {

    @Id
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "skill_level")
    private double skillLevel; // 1-10 fallback si no hay nivelTecnico

    @Column(name = "position", length = 10)
    private String position; // POR, DEF, MED, DEL

    @Column(name = "total_debt")
    private double totalDebt;

    @Column(name = "total_paid")
    private double totalPaid;

    @Column(name = "games_played")
    private int gamesPlayed;

    @Column(name = "goals_scored")
    private int goalsScored;

    @Column(name = "attended")
    private boolean attended;

    @Column(name = "activo")
    private boolean activo = true;

    // Atributos del jugador
    @Column(name = "edad_min")
    private int edadMin;

    @Column(name = "edad_max")
    private int edadMax;

    @Column(name = "nivel_tecnico")
    private int nivelTecnico; // 1-10

    @Column(name = "estado_fisico")
    private int estadoFisico; // 1-5

    @Column(name = "intensidad", length = 10)
    private String intensidad; // BAJA, MEDIA, ALTA

    @Column(name = "pierna_habil", length = 15)
    private String piernaHabil; // DERECHA, IZQUIERDA, AMBAS

    @Column(name = "notas", length = 500)
    private String notas;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "attendance_status", length = 20)
    private String attendanceStatus; // CONFIRMADO, NO_VA, DUDOSO

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Player() {
        this.id = UUID.randomUUID().toString();
        this.skillLevel = 5.0;
        this.position = "MED";
        this.totalDebt = 0;
        this.totalPaid = 0;
        this.gamesPlayed = 0;
        this.goalsScored = 0;
        this.attended = false;
        this.activo = true;
        this.attendanceStatus = "NO_VA";
    }

    public Player(String name) {
        this();
        this.name = name;
    }

    public Player(String name, double skillLevel, String position) {
        this(name);
        this.skillLevel = skillLevel;
        this.position = position;
    }

    // Calcula la edad física del jugador basada en su condición real
    public int getEdadFisica() {
        if (edadMin <= 0) return 0;
        int edadMed = (edadMin + (edadMax > 0 ? edadMax : edadMin)) / 2;
        int bonus = 0;
        switch (estadoFisico) {
            case 5 -> bonus += 8;
            case 4 -> bonus += 4;
            case 3 -> bonus += 0;
            case 2 -> bonus -= 3;
            case 1 -> bonus -= 6;
        }
        if ("ALTA".equals(intensidad)) bonus += 5;
        else if ("BAJA".equals(intensidad)) bonus -= 5;
        return Math.max(15, Math.min(80, edadMed - bonus));
    }

    // Calcula el score compuesto para el balanceo de equipos
    public double getBalanceScore() {
        if (nivelTecnico > 0) {
            double tecnico = nivelTecnico / 10.0;
            double fisico = estadoFisico > 0 ? estadoFisico / 5.0 : 0.5;
            double intScore = "ALTA".equals(intensidad) ? 1.0 : "MEDIA".equals(intensidad) ? 0.5 : 0.0;
            double raw = (tecnico * 0.5) + (fisico * 0.3) + (intScore * 0.2);
            int ef = getEdadFisica();
            if (ef > 0) {
                double ageFactor = Math.max(0.3, 1.0 - Math.max(0, ef - 25) * 0.008);
                raw = raw * ageFactor;
            }
            return raw * 10.0;
        }
        return skillLevel;
    }

    // Getters y Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getSkillLevel() { return skillLevel; }
    public void setSkillLevel(double skillLevel) { this.skillLevel = skillLevel; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public double getTotalDebt() { return totalDebt; }
    public void setTotalDebt(double totalDebt) { this.totalDebt = totalDebt; }

    public double getTotalPaid() { return totalPaid; }
    public void setTotalPaid(double totalPaid) { this.totalPaid = totalPaid; }

    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }

    public int getGoalsScored() { return goalsScored; }
    public void setGoalsScored(int goalsScored) { this.goalsScored = goalsScored; }

    public boolean isAttended() { return attended; }
    public void setAttended(boolean attended) {
        this.attended = attended;
        // Sync attendanceStatus
        if (attended && !"CONFIRMADO".equals(this.attendanceStatus)) {
            this.attendanceStatus = "CONFIRMADO";
        } else if (!attended && "CONFIRMADO".equals(this.attendanceStatus)) {
            this.attendanceStatus = "NO_VA";
        }
    }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public int getEdadMin() { return edadMin; }
    public void setEdadMin(int edadMin) { this.edadMin = edadMin; }

    public int getEdadMax() { return edadMax; }
    public void setEdadMax(int edadMax) { this.edadMax = edadMax; }

    public int getNivelTecnico() { return nivelTecnico; }
    public void setNivelTecnico(int nivelTecnico) { this.nivelTecnico = nivelTecnico; }

    public int getEstadoFisico() { return estadoFisico; }
    public void setEstadoFisico(int estadoFisico) { this.estadoFisico = estadoFisico; }

    public String getIntensidad() { return intensidad; }
    public void setIntensidad(String intensidad) { this.intensidad = intensidad; }

    public String getPiernaHabil() { return piernaHabil; }
    public void setPiernaHabil(String piernaHabil) { this.piernaHabil = piernaHabil; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
        this.attended = "CONFIRMADO".equals(attendanceStatus);
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    @Override
    public String toString() {
        return "Player{id='" + id + "', name='" + name + "', skillLevel=" + skillLevel + "}";
    }
}
