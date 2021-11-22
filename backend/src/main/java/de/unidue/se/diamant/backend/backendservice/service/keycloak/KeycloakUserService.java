package de.unidue.se.diamant.backend.backendservice.service.keycloak;

import java.net.URISyntaxException;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.unidue.se.diamant.backend.backendservice.service.keycloak.dto.KeycloakUser;
import de.unidue.se.diamant.backend.backendservice.service.keycloak.dto.NewKeycloakUser;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
@Log
public class KeycloakUserService {

	private RestTemplate restTemplate;
	private final String currentRealmURL;


	@Autowired
	public KeycloakUserService(@Qualifier("customKeyCloakRestTemplate") RestTemplate restTemplate,
							   @Value("${keycloak.auth-server-url}") String keycloakServerUrl,
							   @Value("${keycloak.realm}") String keyCloakRealm) {
		this.restTemplate = restTemplate;
		this.currentRealmURL = keycloakServerUrl + "/admin/realms/" + keyCloakRealm;
	}

	public List<KeycloakUser> getAllUsers(String search) throws URISyntaxException {
		URIBuilder uriBuilder = new URIBuilder(currentRealmURL + "/users");
		if(! StringUtils.isEmpty(search)){
			uriBuilder.addParameter("search", search);
		}
		uriBuilder.addParameter("max", "10000");

		return Arrays.asList(Optional.ofNullable(restTemplate.getForEntity(uriBuilder.build(), KeycloakUser[].class)
				.getBody()).orElse(new KeycloakUser[0]));
	}

	@SneakyThrows
	public boolean checkIfUserExists(String email) {
		URIBuilder uriBuilder = new URIBuilder(currentRealmURL + "/users");
		uriBuilder.addParameter("email", email);
		return Optional.ofNullable(restTemplate.getForEntity(uriBuilder.build(), KeycloakUser[].class).getBody()).orElse(new KeycloakUser[0]).length > 0;
	}


}
