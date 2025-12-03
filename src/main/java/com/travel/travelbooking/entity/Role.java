package com.travel.travelbooking.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString(exclude = "users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor        // THÊM DÒNG NÀY → CHO new Role()
@AllArgsConstructor       // (TÙY CHỌN)
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String name;

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    private Set<User> users;

    // Constructor cho OAuth2
    public Role(String name) {
        this.name = name;
    }
}