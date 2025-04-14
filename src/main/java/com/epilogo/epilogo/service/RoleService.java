package com.epilogo.epilogo.service;

import com.epilogo.epilogo.model.Role;
import com.epilogo.epilogo.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public void createRolesIfNotExists() {
        createRoleIfNotExists("ADMIN");
        createRoleIfNotExists("USER");
        createRoleIfNotExists("GUEST");
    }

    private void createRoleIfNotExists(String rolName) {
        if (!roleRepository.existsByRolName(rolName)) {
            Role role = new Role();
            role.setRolName(rolName);
            roleRepository.save(role);
        }
    }
}
