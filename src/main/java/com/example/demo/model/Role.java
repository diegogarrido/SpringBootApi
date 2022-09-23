package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

public enum Role {
    SUPER_ADMIN("ROLE_SUPER_ADMIN"), ADMIN("ROLE_ADMIN"), USER("ROLE_USER");

    @Getter
    @Setter
    private String name;

    Role(String name) {
        this.name = name;
    }
}
