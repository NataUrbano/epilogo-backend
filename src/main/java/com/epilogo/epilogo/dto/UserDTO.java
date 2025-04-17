package com.epilogo.epilogo.dto;

import com.epilogo.epilogo.model.Role;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserDTO {
    private Long userId;
    private String name;
    private String email;
    private LocalDateTime registerDate;
    private Set<Role> roles;
}
