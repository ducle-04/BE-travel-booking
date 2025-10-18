package com.travel.travelbooking.Entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Entity
@Getter
@Setter
@ToString(exclude = "users") // tránh in ra users → vòng lặp
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // chỉ dùng các field include
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String name;

    // Quan hệ ngược với User
    @ManyToMany(mappedBy = "roles")
    @JsonIgnore // tránh vòng lặp khi convert sang JSON
    private Set<User> users;
}
