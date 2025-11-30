package com.ninjaone.dundie_awards.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.NonNull;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private long id;

    @NonNull
    @Column(name = "first_name")
    private String firstName;

    @NonNull
    @Column(name = "last_name")
    private String lastName;

    @NonNull
    @Column(name = "dundie_awards")
    private Integer dundieAwards;

    @NonNull
    @ManyToOne
    private Organization organization;

    @NonNull
    @OneToMany(mappedBy = "employee",
           fetch = FetchType.LAZY,
           cascade = CascadeType.ALL,
           orphanRemoval = false)
    private List<Award> awards = new ArrayList<>();

    @Builder
    private Employee(String firstName, String lastName, Integer dundieAwards, Organization organization, List<Award> awards) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dundieAwards = dundieAwards;
        this.organization = organization;
        this.awards = awards != null ? awards : new ArrayList<>();
    }

    public void addAward(Award award) {
        awards.add(award);
        award.setEmployee(this); // keep both sides in sync

        int current = (dundieAwards == null ? 0 : dundieAwards);
        dundieAwards = current + 1;
    }
}