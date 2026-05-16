package com.botfutbol.dto;

public class PlayerResponseDTO {

    private String name;
    private double skillLevel;
    private int goalsScored;
    private int gamesPlayed;
    private long totalPaid;
    private long totalDebt;
    private boolean attended;
    private String position;
    private int edadMin;
    private int edadMax;
    private int nivelTecnico;
    private int estadoFisico;
    private String intensidad;
    private String piernaHabil;
    private String notas;
    private String observaciones;
    private String attendanceStatus;
    private int edadFisica;
    private double balanceScore;

    public PlayerResponseDTO() {}

    public PlayerResponseDTO(String name, double skillLevel, int goalsScored,
                             int gamesPlayed, long totalPaid, long totalDebt, boolean attended) {
        this.name = name;
        this.skillLevel = skillLevel;
        this.goalsScored = goalsScored;
        this.gamesPlayed = gamesPlayed;
        this.totalPaid = totalPaid;
        this.totalDebt = totalDebt;
        this.attended = attended;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getSkillLevel() { return skillLevel; }
    public void setSkillLevel(double skillLevel) { this.skillLevel = skillLevel; }

    public int getGoalsScored() { return goalsScored; }
    public void setGoalsScored(int goalsScored) { this.goalsScored = goalsScored; }

    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }

    public long getTotalPaid() { return totalPaid; }
    public void setTotalPaid(long totalPaid) { this.totalPaid = totalPaid; }

    public long getTotalDebt() { return totalDebt; }
    public void setTotalDebt(long totalDebt) { this.totalDebt = totalDebt; }

    public boolean isAttended() { return attended; }
    public void setAttended(boolean attended) { this.attended = attended; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

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
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }

    public int getEdadFisica() { return edadFisica; }
    public void setEdadFisica(int edadFisica) { this.edadFisica = edadFisica; }

    public double getBalanceScore() { return balanceScore; }
    public void setBalanceScore(double balanceScore) { this.balanceScore = balanceScore; }
}
