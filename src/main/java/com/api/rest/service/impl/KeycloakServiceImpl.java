package com.api.rest.service.impl;

import com.api.rest.controller.dto.UserDTO;
import com.api.rest.service.IKeycloakService;
import com.api.rest.util.KeycloakProvider;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class KeycloakServiceImpl implements IKeycloakService {

    /**
     * Metodo para listar todos los metodos de Keycloak
     * @return List<UserRepresentation>
     */
    @Override
    public List<UserRepresentation> findAllUsers() {
        return KeycloakProvider.getRealmResource()
                .users()
                .list();
    }


    /**
     * Metodos para buscar un usuario por username
     * @param username
     * @return List<UserRepresentation>
     */
    @Override
    public List<UserRepresentation> searchUserByUsername(String username) {
        return KeycloakProvider.getRealmResource()
                .users()
                .searchByUsername(username, true);
    }


    /**
     * Metodo para crear un usuario nuevo en Keycloak
     * @param userDTO
     * @return Mensaje
     */
    @Override
    public String createUser(@NotNull UserDTO userDTO) {

        int status = 0;
        UsersResource userResource = KeycloakProvider.getUserResource();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.getUsername());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setEmailVerified(true);
        user.setEnabled(true);

        Response response = userResource.create(user);
        status = response.getStatus();

        if (status == 201){
            String path = response.getLocation().getPath();
            String userId = path.substring(path.lastIndexOf("/") + 1);

            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setTemporary(false);
            credentialRepresentation.setType(OAuth2Constants.PASSWORD);
            credentialRepresentation.setValue(userDTO.getPassword());

            userResource.get(userId).resetPassword(credentialRepresentation);

            RealmResource realmResource = KeycloakProvider.getRealmResource();

            List<RoleRepresentation> roleRepresentations = null;

            if (userDTO.getRoles() == null || userDTO.getRoles().isEmpty()) {
                roleRepresentations = List.of(realmResource.roles().get("user").toRepresentation());
            }else{
                roleRepresentations = realmResource.roles()
                        .list()
                        .stream()
                        .filter(role -> userDTO.getRoles()
                                .stream()
                                .anyMatch(roleName -> roleName.equalsIgnoreCase(role.getName())))
                                .toList();
            }

            realmResource.users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .add(roleRepresentations);

            return "User created successfully!!";
        } else if (status == 409) {
            log.error("User exist already!!");
            return "User exist already!!";
        }else{
            log.error("Error creating user, please contact with the administrator!!");
            return "Error creating user, please contact with the administrator!!";
        }
    }


    /**
     * Metodo para borrar un usuario en Keycloak
     * @param userId
     */
    @Override
    public void deleteUser(String userId) {
        KeycloakProvider.getUserResource().get(userId).remove();
    }


    /**
     * Metodo para actualizar un usuario en Keycloak
     * @param userId
     * @param userDTO
     */
    @Override
    public void updateUser(String userId, @NotNull UserDTO userDTO) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(OAuth2Constants.PASSWORD);
        credentialRepresentation.setValue(userDTO.getPassword());

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.getUsername());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setCredentials(Collections.singletonList(credentialRepresentation));

        UserResource userResource = KeycloakProvider.getUserResource().get(userId);

        userResource.update(user);
    }
}
