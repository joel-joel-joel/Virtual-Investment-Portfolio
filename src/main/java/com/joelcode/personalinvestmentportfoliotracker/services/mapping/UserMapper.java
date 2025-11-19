package com.joelcode.personalinvestmentportfoliotracker.services.mapping;

import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    // Convert user creation request DTO to entity
    public static User toEntity(UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        return user;
    }

    // Update user entity from update request DTO
    public static void updateEntity(User user, UserUpdateRequest request) {
        if (request.getUsername() != null) {user.setUsername(request.getUsername());}
        if (request.getEmail() != null) {user.setEmail(request.getEmail());}
        if (request.getPassword() != null) {user.setPassword(request.getPassword());}
    }

    // Convert user entity to user response DTO
    public static UserDTO toDTO(User user) {
        if (user == null) return null;
        return new UserDTO(user.getUserId(),
                user.getUsername(),
                user.getEmail());
    }
}
