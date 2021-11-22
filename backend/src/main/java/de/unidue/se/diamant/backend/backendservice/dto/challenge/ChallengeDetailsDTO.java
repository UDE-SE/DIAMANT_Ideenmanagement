package de.unidue.se.diamant.backend.backendservice.dto.challenge;

import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Visibility;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeDetailsDTO {

    /**
     * Id der Challenge
     */
    private String id;

    /**
     * Titel der Challenge. Maximal 50 Zeichen.
     */
    private String title;
    /**
     * Kurzbeschreibung. Maximal 100 Zeichen
     */
    private String shortDescription;
    /**
     * Ausführliche Beschreibung
     */
    private String description;

    /**
     * Start der Einreichungsfrist
     * Angabe in Millisekunden seit Start der Epoche:
     * <code>1970-01-01T00:00:00Z + timeStamp</code>
     */
    private Long submissionStart;

    /**
     * Start der Begutachtungszeit (geleichzeitig Ende der Einreichungszeit)
     * Angabe in Millisekunden seit Start der Epoche:
     * <code>1970-01-01T00:00:00Z + timeStamp</code>
     */
    private Long reviewStart;

    /**
     * Start der Überarbeitungszeit (geleichzeitig Ende der Begutachtungszeit). Optional
     * Angabe in Millisekunden seit Start der Epoche:
     * <code>1970-01-01T00:00:00Z + timeStamp</code>
     */
    private Long refactoringStart;

    /**
     * Start der Voting-Phase (geleichzeitig Ende (optionalen) der Überarbeitungszeit / bzw. Einreichungszeit).
     * Angabe in Millisekunden seit Start der Epoche:
     * <code>1970-01-01T00:00:00Z + timeStamp</code>
     */
    @NotNull
    private Long votingStart;

    /**
     * Start der Umsetzungsphase / Siegerverkündung (geleichzeitig Ende Voting-Phase).
     * Angabe in Millisekunden seit Start der Epoche:
     * <code>1970-01-01T00:00:00Z + timeStamp</code>
     */
    private Long implementationStart;

    /**
     * Ende der Umsetzungsphase und der Challenge.
     */
    private Long challengeEnd;

    /**
     * Sichtbarkeit der Challenge
     */
    private Visibility visibility;

    /**
     * Preise für die Sieger der Challenge
     */
    private String awards;

    /**
     * News zu der Challenge
     */
    private List<NewsItemDTO> news = new ArrayList<>();

    /**
     * Liste der verfügbaren Anhänge
     */
    private List<ChallengeAttachmentDTO> attachments = new ArrayList<>();

    /**
     * Eingereichte Ideen
     */
    private List<IdeaPreview> ideas;

    /**
     * Gutachter der Challenge
     */
    private Set<Person> referees;

    /**
     * Eingeladene Teilnehmer
     */
    private Set<Person> invitedUsers;

    /**
     * Gibt an, ob der aktuelle Nutzer die Erlaubnis hat, die Challenge zu bearbeiten
     */
    private boolean editPermission;

    /**
     * Einladungslink für Gutachter
     */
    private String invitationLinkReferee;

    /**
     * Einladungslink für Teilnehmer (wenn durch die Sichtbarekeit notwendig)
     */
    private String invitationLinkInvitedUsers;


    public static ChallengeDetailsDTO from(Challenge challenge){
        return new ChallengeDetailsDTO(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getShortDescription(),
                challenge.getDescription(),
                challenge.getSubmissionStart() != null ? challenge.getSubmissionStart().getTime() : null,
                challenge.getReviewStart() != null ? challenge.getReviewStart().getTime() : null,
                challenge.getRefactoringStart() != null ? challenge.getRefactoringStart().getTime() : null,
                challenge.getVotingStart() != null ? challenge.getVotingStart().getTime() : null,
                challenge.getImplementationStart() != null ? challenge.getImplementationStart().getTime() : null,
                challenge.getChallengeEnd() != null ? challenge.getChallengeEnd().getTime() : null,
                challenge.getVisibility(),
                challenge.getAwards(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                challenge.getReferees(),
                challenge.getInvitedUsers(),
                false,
                null,
                null
        );
    }
}
