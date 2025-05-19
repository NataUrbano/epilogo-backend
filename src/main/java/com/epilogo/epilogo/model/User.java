package com.epilogo.epilogo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
@Schema(name = "User", description = "Entidad que representa un usuario del sistema")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @Schema(description = "Identificador único del usuario", example = "123", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;

    @Column(name = "user_name", nullable = false, length = 150)
    @Size(min = 3, max = 150)
    @Schema(description = "Nombre de usuario", example = "juanperez", required = true)
    private String userName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    @Email
    @Schema(description = "Correo electrónico del usuario", example = "usuario@correo.com", required = true, format = "email")
    private String email;

    @Column(name = "password", nullable = false)
    @JsonIgnore
    @Schema(description = "Contraseña del usuario (no expuesta en la documentación)", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @Column(name = "register_date")
    @CreationTimestamp
    @Schema(description = "Fecha de registro del usuario", example = "2024-05-18T16:50:00", accessMode = Schema.AccessMode.READ_ONLY, format = "date-time")
    private LocalDateTime registerDate;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Schema(description = "Roles asignados al usuario")
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-reservations")
    @Schema(description = "Reservas realizadas por el usuario")
    private List<Reservation> reservations = new ArrayList<>();

    @Transient
    @Schema(description = "URL de la imagen del usuario", example = "https://example.com/images/user123.png", accessMode = Schema.AccessMode.READ_ONLY)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Schema(description = "Indica si el usuario está activo", example = "true")
    private Boolean isActive = true;

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    @PrePersist
    @PreUpdate
    public void prepareData() {
        this.email = this.email != null ? this.email.toLowerCase().trim() : null;
        this.userName = this.userName != null ? this.userName.trim() : null;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", registerDate=" + registerDate +
                '}';
    }
}
