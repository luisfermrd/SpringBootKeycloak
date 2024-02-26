package com.api.rest.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

@Value
@AllArgsConstructor
@Builder
public class UserDTO {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private Set<String> roles;
}
