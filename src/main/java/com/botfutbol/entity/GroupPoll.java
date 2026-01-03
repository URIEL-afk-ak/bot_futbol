package com.botfutbol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que representa una encuesta en el chat de un grupo.
 */
@Entity
@Table(name = "group_polls")
public class GroupPoll {
    
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    
    @ManyToOne
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;
    
    @Column(nullable = false, length = 500)
    private String question;
    
    @ElementCollection
    @CollectionTable(name = "poll_options", joinColumns = @JoinColumn(name = "poll_id"))
    @Column(name = "option_text", length = 200)
    private List<String> options = new ArrayList<>();
    
    @Column(name = "is_multiple_choice", nullable = false)
    private boolean isMultipleChoice = false;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "event_id")
    private String eventId; // ID del evento asociado (opcional, para encuestas de asistencia)
    
    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PollVote> votes = new ArrayList<>();
    
    public GroupPoll() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }
    
    public GroupPoll(Group group, User createdBy, String question, List<String> options) {
        this();
        this.group = group;
        this.createdBy = createdBy;
        this.question = question;
        this.options = options != null ? options : new ArrayList<>();
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Group getGroup() {
        return group;
    }
    
    public void setGroup(Group group) {
        this.group = group;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
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
    
    public List<PollVote> getVotes() {
        return votes;
    }
    
    public void setVotes(List<PollVote> votes) {
        this.votes = votes;
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}

