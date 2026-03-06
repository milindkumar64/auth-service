package com.arth.auth.dto;

import com.arth.auth.model.Role;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RoleDto {

    private List<String> role;

    public RoleDto(Set<Role> roles) {
     this.role = roles.stream().map(role -> role.getName().toString()).collect(Collectors.toList());
    }

    public List<String> getRole() {
        return role;
    }

    public void setRole(List<String> role) {
        this.role = role;
    }

}
