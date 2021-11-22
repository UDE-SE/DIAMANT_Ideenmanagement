package de.unidue.se.diamant.backend.backendservice.service.invitation;

import io.jsonwebtoken.*;
import lombok.extern.java.Log;
import org.keycloak.representations.JsonWebToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@Service
@Log
public class JWTService {

    @Value("${app.invitation.sign.secret}")
    private String signSecret;


    public String generateInvitationToken(InvitationInformation invitationInformation){
        return Jwts.builder()
                .setClaims(invitationInformation.asClaims())
                .setExpiration(invitationInformation.getExpiryDate())
                .signWith(SignatureAlgorithm.HS512, signSecret)
                .compact();
    }

    /**
     * Validiert den token und gibt (bei gültigem Token) die Informationen zurück
     */
    public InvitationInformation getInvitationInformationFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(signSecret)
                    .parseClaimsJws(token)
                    .getBody();
            return InvitationInformation.fromClaims(claims);
        } catch (SignatureException ex) {
            log.log(Level.WARNING, "Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.log(Level.WARNING,"Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.log(Level.WARNING,"Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.log(Level.WARNING,"Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.log(Level.WARNING,"JWT claims string is empty.");
        }
        throw new IllegalArgumentException("Der Token ist nicht gültig!");
    }

}
