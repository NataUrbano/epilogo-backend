package com.epilogo.epilogo.controller;

import com.epilogo.epilogo.dto.UserDTO;
import com.epilogo.epilogo.exceptions.ForbiddenException;
import com.epilogo.epilogo.exceptions.InvalidDataException;
import com.epilogo.epilogo.exceptions.UnauthorizedException;
import com.epilogo.epilogo.exceptions.UserNotFoundException;
import com.epilogo.epilogo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> findAll() {
        List<UserDTO> userDTOs = userService.findAllDTOs();
        if (userDTOs.isEmpty()) {
            throw new UserNotFoundException("No users found");
        }
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findByUserId(@PathVariable("id") Long userId) {
        return userService.findDTOById(userId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    @PutMapping("/actualizar")
    public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO userDTO) {
        if (userDTO.getUserId() == null || userDTO.getName() == null) {
            throw new InvalidDataException("Invalid user data");
        }
        UserDTO updatedUserDTO = userService.updateUserDTO(userDTO);
        return ResponseEntity.ok(updatedUserDTO);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable("id") Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/restricted")
    public ResponseEntity<String> restrictedEndpoint() {
        throw new UnauthorizedException("You are not authorized to access this resource");
    }

    @GetMapping("/forbidden")
    public ResponseEntity<String> forbiddenEndpoint() {
        throw new ForbiddenException("Access to this resource is forbidden");
    }
}
