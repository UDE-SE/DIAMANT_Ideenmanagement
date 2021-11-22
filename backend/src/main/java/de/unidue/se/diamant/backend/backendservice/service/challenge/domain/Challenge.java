package de.unidue.se.diamant.backend.backendservice.service.challenge.domain;

import de.unidue.se.diamant.backend.backendservice.dto.challenge.NewChallengeDTO;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@Document
public class Challenge {
    @Id
    private String id;
    /**
     * Titel der Challenge
     */
    @NotBlank
    private String title;
    /**
     * Kurzbeschreibung. Maximal 100 Zeichen
     */
    @NotBlank
    private String shortDescription;
    /**
     * Ausführliche Beschreibung
     */
    @NotBlank
    private String description;

    /**
     * Start der Einreichungsfrist
     */
    @NotNull
    private Date submissionStart;

    /**
     * Start der Begutachtungszeit (geleichzeitig Ende der Einreichungszeit)
     */
    @NotNull
    private Date reviewStart;

    /**
     * Start der Überarbeitungszeit (geleichzeitig Ende der Begutachtungszeit). Optional
     */
    private Date refactoringStart;

    /**
     * Start der Voting-Phase (geleichzeitig Ende (optionalen) der Überarbeitungszeit / bzw. Einreichungszeit).
     */
    private Date votingStart;

    /**
     * Start der Umsetzungsphase / Siegerverkündung (geleichzeitig Ende Votingzeit).
     */
    @NotNull
    private Date implementationStart;

    /**
     * Ende der Umsetzungsphase und der Challenge.
     */
    @NotNull
    private Date challengeEnd;

    /**
     * Sichtbarkeit der Challenge
     */
    @NotNull
    private Visibility visibility;

    /**
     * Preise für die Sieger der Challenge
     */
    @NotBlank
    private String awards;

    /**
     * Ersteller der Challenge. Identifiziert über die E-Mail-Adresse.
     */
    @NotBlank
    private Person caller;

    /**
     * Liste der Gutachter. Identifiziert über die E-Mail-Adresse.
     */
    @Builder.Default
    private Set<Person> referees = new HashSet<>();

    /**
     * Zusätzliche, explizit eingeladene Teilnehmer.
     * Identifiziert über die E-Mail-Adresse.
     */
    @Builder.Default
    private Set<Person> invitedUsers = new HashSet<>();

    public Challenge(@NotBlank String title, @NotBlank String shortDescription, @NotBlank String description, @NotNull Date submissionStart, @NotNull Date reviewStart, Date refactoringStart, @NotNull Date implementationStart, @NotNull Date challengeEnd, @NotNull Visibility visibility, @NotBlank String awards, @NotNull @Valid Person caller, Set<Person> referees, Set<Person> invitedUsers) {
        this.title = title;
        this.shortDescription = shortDescription;
        this.description = description;
        this.submissionStart = submissionStart;
        this.reviewStart = reviewStart;
        this.refactoringStart = refactoringStart;
        this.implementationStart = implementationStart;
        this.challengeEnd = challengeEnd;
        this.visibility = visibility;
        this.awards = awards;
        this.caller = caller;
        this.referees = referees;
        this.invitedUsers = invitedUsers;
    }

    private static Date getDateTimeIfAvailable(Long milliseconds){
        if(milliseconds != null){
            return new Date(milliseconds);
        }
        return null;
    }

    public static Challenge fromNewChallengeDTO(NewChallengeDTO dto) {
        return new Challenge(
                null,
                dto.getGeneralInformation().getTitle(),
                dto.getGeneralInformation().getShortDescription(),
                dto.getDescription(),
                getDateTimeIfAvailable(dto.getMileStones().getSubmissionStart()),
                getDateTimeIfAvailable(dto.getMileStones().getReviewStart()),
                getDateTimeIfAvailable(dto.getMileStones().getRefactoringStart()),
                getDateTimeIfAvailable(dto.getMileStones().getVotingStart()),
                getDateTimeIfAvailable(dto.getMileStones().getImplementationStart()),
                getDateTimeIfAvailable(dto.getMileStones().getChallengeEnd()),
                dto.getGeneralInformation().getVisibility(),
                dto.getAwards(),
                null,
                dto.getReferees(),
                dto.getInvitedUsers()
        );
    }
}
