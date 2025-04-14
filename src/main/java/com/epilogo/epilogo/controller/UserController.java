package com.epilogo.epilogo.controller;

import com.epilogo.epilogo.dto.UserDTO;
import com.epilogo.epilogo.model.User;
import com.epilogo.epilogo.service.UserService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;

    @Autowired
    public UserController(UserService userService, ModelMapper modelMapper) {
        this.userService = userService;
        this.modelMapper = modelMapper;
    }


    @GetMapping
    public ResponseEntity<List<UserDTO>> findAll() {
        List<User> users = userService.findAll();
        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<UserDTO> userDTOs = users.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findByUserId(@PathVariable int userId) {
        return userService.findByUserId(userId)
                .map(user -> ResponseEntity.ok(modelMapper.map(user, UserDTO.class)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/actualizar")
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody UserDTO userDTO) {
        User user = modelMapper.map(userDTO, User.class);
        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(modelMapper.map(updatedUser, UserDTO.class));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable int userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
}

