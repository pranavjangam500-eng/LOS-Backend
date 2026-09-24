package com.bank.los.bank.master.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "session_activity", schema = "identity")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionActivity {

    @Id
    @Column(length = 100)
    private String jti;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "last_seen", nullable = false)
    private LocalDateTime lastSeen;

    @Column(name = "timeout_secs", nullable = false)
    @Builder.Default
    private Integer timeoutSecs = 1800;

    @Column(nullable = false)
    @Builder.Default
    private Boolean invalidated = false;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
