package de.unidue.se.diamant.backend.backendservice.service.permissions;

import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.Idea;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.IdeaState;
import de.unidue.se.diamant.backend.backendservice.service.time.TimeService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log
public class PermissionServiceFacade {

    private ChallengePermissionService challengePermissionService;
    private IdeaPermissionsService ideaPermissionsService;
    private VotingPermissionService votingPermissionService;
    private TimeService timeService;

    @Autowired
    public PermissionServiceFacade(ChallengePermissionService challengePermissionService, IdeaPermissionsService ideaPermissionsService, VotingPermissionService votingPermissionService, TimeService timeService) {
        this.challengePermissionService = challengePermissionService;
        this.ideaPermissionsService = ideaPermissionsService;
        this.votingPermissionService = votingPermissionService;
        this.timeService = timeService;
    }

    /**
     * Prüft, ob ein gegebener Nutzer die Rechte hat, eine bestimmte Challenge anzuzeigen.
     * Gutachter dürfen "Ihre" Challenges unabhängig von Fristen/Einladestatus/Sichtbarkeiten immer sehen
     * @param challengeDetails Challenge, die überprüft werden soll
     * @param currentUserMail Nutzer, der Versucht die Challenge anzuschauen
     * @param errorBuffer StringBuilder, in den Fehler geschrieben werden.
     */
    public boolean checkIsAllowedToViewChallenge(Challenge challengeDetails, String currentUserMail, StringBuilder errorBuffer) {
        return challengePermissionService.checkIsAllowedToViewChallenge(challengeDetails, currentUserMail, errorBuffer);
    }

    /**
     * Prüft, ob der angegebene Nutzer Anhänge zu der Challenge hinzufügen darf
     * @param challenge Challenge
     * @param currentUserMail Nutzer
     * @param errorBuffer Speicher für mögliche Fehler
     * @return Ergebnis der Prüfung
     */
    public boolean checkIsAllowedToUploadAttachmentToChallenge(Challenge challenge, String currentUserMail, StringBuilder errorBuffer) {
        return challengePermissionService.checkIsAllowedToUploadAttachmentToChallenge(challenge, currentUserMail, errorBuffer);
    }

    /**
     * Prüft, ob das Ansehen einer Idea erlaubt ist
     */
    public boolean checkIsAllowedToViewIdea(Challenge challenge, Idea idea, String currentUser, StringBuilder errorBuffer){
        return ideaPermissionsService.checkIsAllowedToViewIdea(challenge, idea, currentUser, timeService.getTime(), errorBuffer);
    }

    /**
     * Prüft, ob der aktuelle Nutzer berechtigt ist, einen Anhang mit der gegebenen ID zu einer Idee hinzuzufügen
     * @param challenge Challenge zu der die Idee gehört (bzw. gehören müsste)
     * @param idea Idee, überprüft wird
     * @param businessAttachmentId ID des potentiellen Anhangs
     * @param currentUserMail Mail-Adresse des hochlandenden Nutzers
     * @param errorBuffer Speicher für mögliche Fehler
     * @return Ergebnis der Prüfung
     */
    public boolean checkIsAllowedToUploadAttachmentToIdea(Challenge challenge, Idea idea, String businessAttachmentId, String currentUserMail, StringBuilder errorBuffer) {
        return ideaPermissionsService.checkIsAllowedToUploadAttachmentToIdea(challenge, idea, businessAttachmentId, currentUserMail, errorBuffer);
    }

    /**
     * Prüft, ob der eigeloggte Nutzer die Rechte hat, den Status der Idee auf den neuen Wert zu setzen
     */
    public boolean checkIsAllowedToUpdateIdeaState(Challenge challenge, Idea idea, IdeaState newState, String currentUserMail, StringBuilder errorBuffer) {
        return ideaPermissionsService.checkIsAllowedToUpdateIdeaState(challenge, idea, newState, currentUserMail, errorBuffer);
    }

    /**
     * Prüft, ob das Bearbeiten der Idee erlaubt ist.
     * @param challenge Challenge, zu der die Idee gehört (wird *nicht* geprüft!)
     * @param idea Idee
     * @param currentUser aktueller Nutzer
     * @param errorBuffer Potentielle Fehlerursachen
     */
    public boolean checkIsAllowedToEditIdea(Challenge challenge, Idea idea, String currentUser, StringBuilder errorBuffer) {
        return ideaPermissionsService.checkIsAllowedToEditIdea(challenge, idea, currentUser, timeService.getTime(), errorBuffer);
    }

    /**
     * Prüft, ob der angegebene Nutzer die angegebene Idee für das Voting nominieren darf. Falls nicht wird ein Fehlergrund in den errorBuffer geschrieben.
     * @param challenge Challenge zu der die Idee gehört (wird *nicht* geprüft)!
     * @param idea Idee, die nominiert werden soll
     * @param currentUser der aktuelle Nutzer (hoffentlich ein Gutachter!)
     * @param errorBuffer Buffer für die Fehlermeldungen..
     * @return Ergebnis der Prüfung
     */
    public boolean checkIsAllowedToNominateIdeaForVoting(Challenge challenge, Idea idea, String currentUser, StringBuilder errorBuffer){
        return ideaPermissionsService.checkIsAllowedToNominateIdeaForVoting(challenge, idea, currentUser, timeService.getTime(), errorBuffer);
    }

    /**
     * Prüft, ob abgestimmt werden darf
     */
    public boolean checkIsAllowedToVote(Challenge challenge, Idea idea, String currentUserMail, StringBuilder errorBuffer){
        return votingPermissionService.checkIsAllowedToVote(challenge, idea, currentUserMail, timeService.getTime(), errorBuffer);
    }

    /**
     * Prüft, ob man die "fremden" Votings für Ideen sehen darf
     */
    public boolean checkIsAllowedToViewVotes(Challenge challenge, StringBuilder errorBuffer) {
        return votingPermissionService.checkIsAllowedToViewVotes(challenge, timeService.getTime(), errorBuffer);
    }

    /**
     * Prüft, ob der aktuelle Nutzer den TeamChat sehen (und Nachrichten hinzufügen) darf
     */
    public boolean checkIsAllowedToViewTeamChat(Challenge challenge, Idea idea, String currentUser, StringBuilder errorBuffer) {
       return ideaPermissionsService.checkIsAllowedToViewTeamChat(challenge, idea, currentUser, timeService.getTime(), errorBuffer);
    }

    /**
     * Prüft, ob der aktuelle Nutzer den GutachterChat sehen (und Nachrichten hinzufügen) darf
     */
    public boolean checkIsAllowedToViewRefereeChat(Challenge challenge, Idea idea, String currentUser, StringBuilder errorBuffer) {
        return ideaPermissionsService.checkIsAllowedToViewRefereesChat(challenge, idea, currentUser, timeService.getTime(), errorBuffer);
    }

    /**
     * Prüft, ob der aktuelle Nutzer den TeamUndGutachterChat sehen (und Nachrichten hinzufügen) darf
     */
    public boolean checkIsAllowedToViewTeamAndRefereeChat(Challenge challenge, Idea idea, String currentUser, StringBuilder errorBuffer) {
        return ideaPermissionsService.checkIsAllowedToViewTeamChat(challenge, idea, currentUser, timeService.getTime(), errorBuffer) ||
                ideaPermissionsService.checkIsAllowedToViewRefereesChat(challenge, idea, currentUser, timeService.getTime(), errorBuffer);
    }

    /**
     * Prüft, ob jemand die Rechte hat, alle Teammitglieder (unabhängig von ihrem Sichtbarkeitsstatus) zu sehen
     */
    public boolean checkIsAllowedToViewAllTeamMember(Idea ideaDetails, String currentUser, StringBuilder errorBuffer) {
        return ideaPermissionsService.checkIsTeamMember(ideaDetails, currentUser, errorBuffer);
    }

    public boolean checkHasEditPermission(Challenge challengeDetails, String currentUserMail, StringBuilder errorBuffer) {
        return challengePermissionService.checkHasEditPermission(challengeDetails, currentUserMail, errorBuffer);
    }

    public boolean checkIsCaller(Challenge challenge, String currentUserMail, StringBuilder errorBuffer){
        return challengePermissionService.checkIsCaller(challenge, currentUserMail, errorBuffer);
    }

    public boolean checkIsAllowedToNominateAsWinner(Challenge challenge, String currentUser, StringBuilder stringBuilder) {
        return challengePermissionService.checkIsAllowedToNominateAsWinner(challenge, currentUser, stringBuilder);
    }
}
