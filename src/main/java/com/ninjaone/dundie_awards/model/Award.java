package com.ninjaone.dundie_awards.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.NonNull;

import java.time.Instant;

@Entity
@Table(name = "awards")
@Getter
@Setter
@NoArgsConstructor
public class Award {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AwardType type;

    @NonNull
    @Column(name = "awarded_at", nullable = false)
    private Instant awardedAt;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Builder
    private Award(AwardType type, Instant awardedAt, Employee employee) {
        this.type = type;
        this.awardedAt = awardedAt;
        this.employee = employee;
    }
}
