package de.unidue.se.diamant.backend.backendservice.dto.challenge;

import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ChallengePreviewDTO {
    /**
     * Id
     */
    private String id;
    /**
     * Titel
     */
    private String title;
    /**
     * Kurzbeschreibung
     */
    private String shortDescription;
    /**
     * Sichtbarkeit der Challenge
     */
    private Visibility visibility;
    /**
     * Zeitpunkt, ab dem die Challenge sichtbar/freigeschaltet ist.
     * Angabe in Millisekunden seit Start der Epoche:
     * <code>1970-01-01T00:00:00Z + timeStamp</code>
     */
    private Long submissionStart;

    public static ChallengePreviewDTO fromChallenge(Challenge challenge){
        return new ChallengePreviewDTO(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getShortDescription(),
                challenge.getVisibility(),
                challenge.getSubmissionStart().getTime()
        );
    }
}
