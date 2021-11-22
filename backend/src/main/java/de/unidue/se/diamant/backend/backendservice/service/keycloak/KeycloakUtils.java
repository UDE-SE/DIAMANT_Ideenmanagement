package de.unidue.se.diamant.backend.backendservice.service.keycloak;

import de.unidue.se.diamant.backend.backendservice.dto.ResourceNotFoundException;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import de.unidue.se.diamant.backend.backendservice.service.keycloak.dto.KeycloakUser;
import lombok.extern.java.Log;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.logging.Level;

@Log
public class KeycloakUtils {

    public static Person getPersonWithoutClassCastException(Authentication authentication){
        KeycloakUser currentUser = KeycloakUtils.getUserDetailsClassCastExceptionSafe(authentication);
        return Person.builder().email(currentUser.getEmail()).name(String.format("%s %s", currentUser.getFirstName(), currentUser.getLastName())).build();
    }

    public static KeycloakUser getUserDetailsClassCastExceptionSafe(Authentication authentication){
        try{
            return KeycloakUtils.getUserDetails(authentication);
        } catch (ClassCastException ce){
            return KeycloakUser.builder().firstName(authentication.getName()).lastName(authentication.getName()).email(authentication.getName()).build();
        }
    }

    public static KeycloakUser getUserDetails(Principal principal) {
        KeycloakSecurityContext session = ((KeycloakPrincipal)(((KeycloakAuthenticationToken)principal).getPrincipal())).getKeycloakSecurityContext();
        AccessToken accessToken = session.getToken();
        return KeycloakUser.builder().email(accessToken.getEmail()).firstName(accessToken.getGivenName()).lastName(accessToken.getFamilyName()).build();
    }

    public static String getUser(Authentication authentication) {
        return getUserOrDefault(authentication, null);
    }

    public static String getUserOrDefault(Authentication authentication){
        return getUserOrDefault(authentication, "Unbekannt@example.com");
    }

    /**
     * Gibt den aktuellen Nutzer (seine E-Mail-Adresse) zurück.
     *
     * @return E-Mail des aktuellen Nutzers
     */
    private static String getUserOrDefault(Authentication authentication, String defaultName) {
        String result = authentication != null ? authentication.getName() : null;
        if(StringUtils.isEmpty(result) && defaultName != null){
            result = defaultName;
        }
        if (StringUtils.isEmpty(result)) {
            String error = "Kein eingeloggter Nutzer gefunden";
            log.log(Level.WARNING, error);
            throw new ResourceNotFoundException(error);
        }
        return result;
    }

}
