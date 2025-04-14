package com.epilogo.epilogo.service;

import com.epilogo.epilogo.model.User;
import com.epilogo.epilogo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findByUserId(int userId) {
        return userRepository.findById(userId);
    }

    public User updateUser(User user) {
        if (user.getUserId() == null) {
            throw new IllegalArgumentException("El ID del usuario es requerido");
        }
        return userRepository.save(user);
    }

    public void deleteUser(int userId) {
        userRepository.deleteById(userId);
    }
}

