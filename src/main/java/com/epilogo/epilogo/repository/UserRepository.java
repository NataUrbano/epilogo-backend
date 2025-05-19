package com.epilogo.epilogo.repository;

import com.epilogo.epilogo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;

@Repository
@Tag(name = "User Repository", description = "Repositorio para operaciones de base de datos relacionadas con usuarios")
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByUserName(String userName);

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.userId = :userId")
    Optional<User> findByIdWithRoles(Long userId);

    @Query("SELECT u FROM User u JOIN FETCH u.roles")
    List<User> findAllWithRoles();

    @Query(value = "SELECT u.* FROM users u " +
            "JOIN reservations r ON u.user_id = r.user_id " +
            "WHERE r.status = 'ACTIVE' " +
            "GROUP BY u.user_id " +
            "ORDER BY COUNT(r.reservation_id) DESC LIMIT 10", nativeQuery = true)
    List<User> findTopUsersByActiveReservations();
}