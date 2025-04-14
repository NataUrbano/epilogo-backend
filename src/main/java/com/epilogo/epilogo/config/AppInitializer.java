package com.epilogo.epilogo.config;

import com.epilogo.epilogo.service.RoleService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppInitializer {

    private final RoleService roleService;

    @Autowired
    public AppInitializer(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostConstruct
    public void init() {
        roleService.createRolesIfNotExists();
    }
}

