package com.botfutbol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa la relación entre un usuario y un grupo.
 * Almacena el rol del usuario en el grupo (ADMIN o MEMBER).
 */
@Entity
@Table(
    name = "group_members",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_group_user", 
        columnNames = {"group_id", "user_id"}
    )
)
public class GroupMember {
    
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MemberRole role;
    
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
    
    public enum MemberRole {
        ADMIN,    // Creador del grupo o administrador
        MEMBER    // Miembro regular
    }
    
    public GroupMember() {
        this.id = UUID.randomUUID().toString();
        this.role = MemberRole.MEMBER;
        this.joinedAt = LocalDateTime.now();
    }
    
    public GroupMember(Group group, User user, MemberRole role) {
        this();
        this.group = group;
        this.user = user;
        this.role = role;
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
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public MemberRole getRole() {
        return role;
    }
    
    public void setRole(MemberRole role) {
        this.role = role;
    }
    
    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
    
    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
    
    @Override
    public String toString() {
        return "GroupMember{" +
                "id='" + id + '\'' +
                ", group=" + (group != null ? group.getId() : null) +
                ", user=" + (user != null ? user.getId() : null) +
                ", role=" + role +
                ", joinedAt=" + joinedAt +
                '}';
    }
}



