package com.epilogo.epilogo.repository;

import com.epilogo.epilogo.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Optional;

@Repository
@Tag(name = "Role Repository", description = "Repositorio para operaciones con roles")
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(Role.RoleName roleName);
    Boolean existsByRoleName(Role.RoleName roleName);
}