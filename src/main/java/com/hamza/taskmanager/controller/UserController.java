package com.hamza.taskmanager.controller;

import com.hamza.taskmanager.dto.common.ApiSuccessResponse;
import com.hamza.taskmanager.dto.user.UserCreateRequest;
import com.hamza.taskmanager.dto.user.UserResponse;
import com.hamza.taskmanager.dto.user.UserUpdateRequest;
import com.hamza.taskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request){
        return ResponseEntity.ok(ApiSuccessResponse.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .message("User created successfully")
                .data(userService.createUser(request))
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<List<UserResponse>>> getAllUsers(){
        return ResponseEntity.ok(ApiSuccessResponse.<List<UserResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Users fetched successfully")
                .data(userService.getAllUsers())
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<UserResponse>> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(ApiSuccessResponse.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .message("User fetched successfully")
                .data(userService.getUserById(id))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<UserResponse>> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request){
        return ResponseEntity.ok(ApiSuccessResponse.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .message("User updated successfully")
                .data(userService.updateUser(id,request))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiSuccessResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("User deleted successfully")
                .build());
    }
}
