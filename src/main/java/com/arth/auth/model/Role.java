package com.arth.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Set;

@Entity
@Table(name = "roles",schema = "auth")
@ToString(exclude = {"users"})
//@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; // e.g., ROLE_USER, ROLE_ADMIN

    @JsonIgnore
    @ManyToMany(mappedBy = "roles")
    private Set<User> users;   // Users having this role


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}
