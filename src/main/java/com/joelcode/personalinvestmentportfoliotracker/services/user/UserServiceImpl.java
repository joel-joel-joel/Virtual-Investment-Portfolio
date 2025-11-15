package com.joelcode.personalinvestmentportfoliotracker.services.user;

import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.repositories.UserRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService{

    // Define key fields
    private final UserRepository userRepository;
    private final UserValidationService userValidationService;


    // Constructor
    public UserServiceImpl(UserRepository userRepository, UserValidationService userValidationService) {
        this.userRepository = userRepository;
        this.userValidationService = userValidationService;
    }

    // Interface functions

    // Create a new user and show its essential information
    @Override
    public UserDTO createUser(UserCreateRequest request) {

        // Check if user exists
        userValidationService.validateEmailUnique(request.getEmail());

        // Map request to entity (process creation request to an entity)
        User user = UserMapper.toEntity(request);

        // Save user to db
        user = userRepository.save(user);

        // Map entity to dto (show information of the user)
        return UserMapper.toDTO(user);
    }

    // Find a user by their ID
    @Override
    public UserDTO getUserById(UUID userId) {

        // Check if user ID is in the repository
        User user = userValidationService.validateUserExists(userId);

        // Map entity to dto (show information of the user)
        return UserMapper.toDTO(user);
    }

    // Generate a list of all the users inclusive of their information
    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Update user entity by given userId
    @Override
    public UserDTO updateUser(UUID userId, UserUpdateRequest request) {

        // Check if user exists
        User user = userValidationService.validateUserExists(userId);

        // Update fields from request
        UserMapper.updateEntity(user, request);

        user = userRepository.save(user);

        // Map entity to dto
        return UserMapper.toDTO(user);
    }

    // Delete user by userId
    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with id " + userId + " not found"));

        userRepository.delete(user);
    }

}
