package com.botfutbol.dto;

import java.time.LocalDateTime;
import java.util.List;

public class GroupPollDTO {
    private String id;
    private String groupId;
    private Long createdByUserId;
    private String createdByUserName;
    private String question;
    private List<String> options;
    private boolean isMultipleChoice;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private boolean isActive;
    private List<Integer> voteCounts; // Conteo de votos por opción
    private int totalVotes;
    private Integer userVoteIndex; // Índice de la opción que votó el usuario actual (null si no votó)
    
    public GroupPollDTO() {
    }
    
    public GroupPollDTO(String id, String groupId, Long createdByUserId, String createdByUserName,
                       String question, List<String> options, boolean isMultipleChoice,
                       LocalDateTime expiresAt, LocalDateTime createdAt, boolean isActive,
                       List<Integer> voteCounts, int totalVotes, Integer userVoteIndex) {
        this.id = id;
        this.groupId = groupId;
        this.createdByUserId = createdByUserId;
        this.createdByUserName = createdByUserName;
        this.question = question;
        this.options = options;
        this.isMultipleChoice = isMultipleChoice;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.isActive = isActive;
        this.voteCounts = voteCounts;
        this.totalVotes = totalVotes;
        this.userVoteIndex = userVoteIndex;
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public Long getCreatedByUserId() {
        return createdByUserId;
    }
    
    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
    
    public String getCreatedByUserName() {
        return createdByUserName;
    }
    
    public void setCreatedByUserName(String createdByUserName) {
        this.createdByUserName = createdByUserName;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public List<String> getOptions() {
        return options;
    }
    
    public void setOptions(List<String> options) {
        this.options = options;
    }
    
    public boolean isMultipleChoice() {
        return isMultipleChoice;
    }
    
    public void setMultipleChoice(boolean multipleChoice) {
        isMultipleChoice = multipleChoice;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public List<Integer> getVoteCounts() {
        return voteCounts;
    }
    
    public void setVoteCounts(List<Integer> voteCounts) {
        this.voteCounts = voteCounts;
    }
    
    public int getTotalVotes() {
        return totalVotes;
    }
    
    public void setTotalVotes(int totalVotes) {
        this.totalVotes = totalVotes;
    }
    
    public Integer getUserVoteIndex() {
        return userVoteIndex;
    }
    
    public void setUserVoteIndex(Integer userVoteIndex) {
        this.userVoteIndex = userVoteIndex;
    }
}

