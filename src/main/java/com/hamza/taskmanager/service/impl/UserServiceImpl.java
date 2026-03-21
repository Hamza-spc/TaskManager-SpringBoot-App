package com.hamza.taskmanager.service.impl;

import com.hamza.taskmanager.dto.user.UserCreateRequest;
import com.hamza.taskmanager.dto.user.UserResponse;
import com.hamza.taskmanager.entity.User;
import com.hamza.taskmanager.repository.UserRepository;
import com.hamza.taskmanager.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse createUser(UserCreateRequest request){
        User user = userRepository.save(
                User.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .password(request.getPassword())
                        .build()
        );
        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
