package de.unidue.se.diamant.backend.backendservice.service.invitation;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Builder;
import lombok.Data;
import org.apache.http.client.utils.DateUtils;

import java.util.Calendar;
import java.util.Date;

@Data
@Builder
public class InvitationInformation {

    private enum TokenFields {
        TYPE, CHALLENGE_ID, IDEA_ID, CREATOR, ADDITIONAL_INFORMATION
    }

    private InvitationType type;
    private String challengeId;
    private String ideaId;
    private String creator;
    private String additionalInformation;
    private Date expiryDate;

    public Claims asClaims() {
        Claims result = Jwts.claims();
        result.put(TokenFields.TYPE.name(), type.name());
        result.put(TokenFields.CHALLENGE_ID.name(), challengeId);
        result.put(TokenFields.IDEA_ID.name(), ideaId);
        result.put(TokenFields.CREATOR.name(), creator);
        result.put(TokenFields.ADDITIONAL_INFORMATION.name(), additionalInformation);
        return result;
    }

    public static InvitationInformation fromClaims(Claims claims){
        return InvitationInformation.builder()
                .type(InvitationType.valueOf(claims.get(TokenFields.TYPE.name(), String.class)))
                .challengeId(claims.get(TokenFields.CHALLENGE_ID.name(), String.class))
                .ideaId(claims.get(TokenFields.IDEA_ID.name(), String.class))
                .creator(claims.get(TokenFields.CREATOR.name(), String.class))
                .additionalInformation(claims.get(TokenFields.ADDITIONAL_INFORMATION.name(), String.class))
                .build();
    }

    public static Date calculateExpiryDate(Date now, int durationInHours){
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.HOUR_OF_DAY, durationInHours);
        return cal.getTime();
    }


}
