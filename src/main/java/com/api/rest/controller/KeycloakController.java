package com.api.rest.controller;

import com.api.rest.controller.dto.UserDTO;
import com.api.rest.service.IKeycloakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/keycloack/user")
@PreAuthorize("hasRole('admin_client_role')")
public class KeycloakController {

    @Autowired
    private IKeycloakService keycloakService;

    @GetMapping("/search")
    public ResponseEntity<?> findAllUser() {
        return ResponseEntity.ok(keycloakService.findAllUsers());
    }

    @GetMapping("/search/{username}")
    public ResponseEntity<?> findUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(keycloakService.searchUserByUsername(username));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUsername(@RequestBody UserDTO userDTO) throws URISyntaxException {
        String response = keycloakService.createUser(userDTO);
        return ResponseEntity.created(new URI("/keycloack/user/create")).body(response);
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUsername(@PathVariable String userId, @RequestBody UserDTO userDTO) {
        keycloakService.updateUser(userId,userDTO);
        return ResponseEntity.ok("User updated successfully!!");
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUsername(@PathVariable String userId) throws URISyntaxException {
        keycloakService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

}
