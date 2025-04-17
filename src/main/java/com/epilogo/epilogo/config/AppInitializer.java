package com.epilogo.epilogo.config;

import com.epilogo.epilogo.model.Role;
import com.epilogo.epilogo.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppInitializer {

    private final RoleRepository roleRepository;

    @Autowired
    public AppInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void init() {
        if (!roleRepository.existsByRolName("ADMIN")) {
            Role role = new Role();
            role.setRolName("ADMIN");
            roleRepository.save(role);
        }
        if (!roleRepository.existsByRolName("USER")) {
            Role role = new Role();
            role.setRolName("USER");
            roleRepository.save(role);
        }
    }
}


