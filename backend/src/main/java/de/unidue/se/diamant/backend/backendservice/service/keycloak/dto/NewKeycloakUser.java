package de.unidue.se.diamant.backend.backendservice.service.keycloak.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.Collections;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class NewKeycloakUser {

    private String email;
    private String firstName;
    private String lastName;

    public String getEmail(){
        return email.toLowerCase();
    }

    public String getUsername(){
        return email.toLowerCase();
    }

    public Boolean getEnabled() {return true;}
}
