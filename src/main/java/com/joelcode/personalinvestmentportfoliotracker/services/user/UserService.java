package com.joelcode.personalinvestmentportfoliotracker.services.user;

import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserDTO createUser(UserDTO userDTO);

    UserDTO getUserById(UUID userId);

    List<UserDTO> getAllUsers();

    UserDTO updateUser(UUID userId, UserUpdateRequest request);

    void deleteUser(UUID userId);
}
