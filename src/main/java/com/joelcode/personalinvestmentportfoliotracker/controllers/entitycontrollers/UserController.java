package com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Get all users (admin only ideally)
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(){
        List<UserDTO> user = userService.getAllUsers();
        return ResponseEntity.ok(user);
    }

    // Get a user
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id){
        UserDTO user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Update a user profile
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable UUID id,
            @RequestBody UserUpdateRequest updatedUser
    ) {
        UserDTO user = userService.updateUser(id, updatedUser);

        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Get all accounts from specific user
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserAccounts(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getAllAccountsForUser(id));
    }


}
