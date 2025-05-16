package com.epilogo.epilogo.repository;

import com.epilogo.epilogo.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(Role.RoleName roleName);
    Boolean existsByRoleName(Role.RoleName roleName);
}