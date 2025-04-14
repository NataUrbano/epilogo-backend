package com.epilogo.epilogo.dto;

import com.epilogo.epilogo.model.Role;
import lombok.Data;

import java.util.Set;

@Data
public class UserDTO {
    private Long userId;
    private String userName;
    private String email;
    private Set<Role> roles;
}
