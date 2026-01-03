package com.botfutbol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa un voto en una encuesta.
 */
@Entity
@Table(name = "poll_votes")
public class PollVote {
    
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "poll_id", nullable = false)
    private GroupPoll poll;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "selected_option_index", nullable = false)
    private Integer selectedOptionIndex;
    
    @Column(name = "voted_at", nullable = false)
    private LocalDateTime votedAt;
    
    public PollVote() {
        this.id = UUID.randomUUID().toString();
        this.votedAt = LocalDateTime.now();
    }
    
    public PollVote(GroupPoll poll, User user, Integer selectedOptionIndex) {
        this();
        this.poll = poll;
        this.user = user;
        this.selectedOptionIndex = selectedOptionIndex;
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public GroupPoll getPoll() {
        return poll;
    }
    
    public void setPoll(GroupPoll poll) {
        this.poll = poll;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Integer getSelectedOptionIndex() {
        return selectedOptionIndex;
    }
    
    public void setSelectedOptionIndex(Integer selectedOptionIndex) {
        this.selectedOptionIndex = selectedOptionIndex;
    }
    
    public LocalDateTime getVotedAt() {
        return votedAt;
    }
    
    public void setVotedAt(LocalDateTime votedAt) {
        this.votedAt = votedAt;
    }
}

