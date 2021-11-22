package de.unidue.se.diamant.backend.backendservice.service.permissions;

import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.*;
import de.unidue.se.diamant.backend.backendservice.service.time.TimeService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Log
public class IdeaPermissionsService {

    private ChallengePermissionService challengePermissionService;
    private TimeService timeService;

    @Autowired
    public IdeaPermissionsService(ChallengePermissionService challengePermissionService, TimeService timeService) {
        this.challengePermissionService = challengePermissionService;
        this.timeService = timeService;
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
    protected boolean checkIsAllowedToUploadAttachmentToIdea(Challenge challenge, Idea idea, String businessAttachmentId, String currentUserMail, StringBuilder errorBuffer) {
        if(! checkIsAllowedToEditIdea(challenge, idea, currentUserMail, timeService.getTime(), errorBuffer)){
            return false;
        }

        for (CanvasElement canvasElement : idea.getCanvasElements()) {
            if(canvasElement.getId().equals(businessAttachmentId)){
                if(CanvasElementType.TEXT.equals(canvasElement.getType())){
                    String error = String.format("Nutzer '%s' hat versucht einen Anhang (ID %s) für die Idea '%s' hinzuzufügen, der Eintrag ist jedoch vom Type Text", currentUserMail, businessAttachmentId, idea.getId());
                    errorBuffer.append(error);
                    return false;
                }
                return true;
            }
        }
        String error = String.format("Nutzer '%s' hat versucht einen Anhang mit der ID %s für die Idea '%s' hinzuzufügen, diese besitzt aber kein passendes Canvas-Element", currentUserMail, businessAttachmentId, idea.getId());
        errorBuffer.append(error);
        return false;
    }

    /**
     * Prüft, ob der eigeloggte Nutzer die Rechte hat, den Status der Idee auf den neuen Wert zu setzen
     */
    protected boolean checkIsAllowedToUpdateIdeaState(Challenge challenge, Idea idea, IdeaState newState, String currentUserMail, StringBuilder errorBuffer) {
        if(! checkIfIdeaBelongsToChallenge(challenge, idea, errorBuffer)){
            return false;
        }
        switch (newState) {
            case SUBMITTED:
                return checkIsAllowedToSubmitIdea(challenge, idea, currentUserMail, timeService.getTime(), errorBuffer);
            case READY_FOR_VOTE:
                return checkIsAllowedToNominateIdeaForVoting(challenge, idea, currentUserMail, timeService.getTime(), errorBuffer);
            case DRAFT:
                errorBuffer.append(String.format("Es wird versucht die Idee %s in den Zustand %s zu setzen. Dies ist nicht möglich!", idea.getId(), IdeaState.DRAFT.toString()));
                return false;
            default:
                throw new IllegalStateException("Unexpected value: " + newState);
        }
    }

    protected boolean checkIsAllowedToSubmitIdea(Challenge challenge, Idea idea, String currentUserMail, Date now, StringBuilder errorBuffer){
        if(! IdeaState.DRAFT.equals(idea.getIdeaState())){
            String error = String.format("Die Idee %s kann nicht für die Gutachter freigegeben (eingereicht) werden, weil sie sich momentan im Status %s befindet!",
                    idea.getId(), idea.getIdeaState().toString());
            errorBuffer.append(error);
            return false;
        }
        return checkIsAllowedToEditIdea(challenge, idea, currentUserMail, now, errorBuffer);
    }

    /**
     * Prüft, ob der angegebene Nutzer die angegebene Idee für das Voting nominieren darf. Falls nicht wird ein Fehlergrund in den errorBuffer geschrieben.
     * @param challenge Challenge zu der die Idee gehört!
     * @param idea Idee, die nominiert werden soll
     * @param currentUser der aktuelle Nutzer (hoffentlich ein Gutachter!)
     * @param now Aktuelle Zeit (für Tests..)
     * @param errorBuffer Buffer für die Fehlermeldungen..
     * @return Ergebnis der Prüfung
     */
    protected boolean checkIsAllowedToNominateIdeaForVoting(Challenge challenge, Idea idea, String currentUser, Date now, StringBuilder errorBuffer){
        if(! checkIfIdeaBelongsToChallenge(challenge, idea, errorBuffer)){
            return false;
        }
        if(! now.before(challenge.getVotingStart())) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            String error = String.format("Die Idee %s kann nicht für das Voting nominiert werden. Dies ist nur bis zum %s möglich (heute ist der %s)",
                    idea.getId(), simpleDateFormat.format(challenge.getVotingStart()), simpleDateFormat.format(now));
            errorBuffer.append(error);
            return false;
        }
        if(challenge.getReferees().stream().noneMatch((p) -> p.getEmail().equals(currentUser.toLowerCase()))){
            String error = String.format("Die Idee %s kann nicht durch %s für das Voting nominiert werden. Dies ist nur für Gutachter möglich!",
                    idea.getId(), currentUser.toLowerCase());
            errorBuffer.append(error);
            return false;
        }
        if(! IdeaState.SUBMITTED.equals(idea.getIdeaState())){
            String error = String.format("Die Idee %s kann nicht durch %s für das Voting nominiert werden, weil sie sich momentan im Status %s befindet!",
                    idea.getId(), currentUser.toLowerCase(), idea.getIdeaState().toString());
            errorBuffer.append(error);
            return false;
        }
        return true;
    }

    /**
     * Prüft, ob eine Idee zu der angegebenen Challenge gehört
     * @param challenge Challenge zu der die Idee potentiell gehört
     * @param idea Idee, die geprüft werden soll
     * @param errorBuffer Speicher für mögliche Fehler
     * @return Ergebnis der Prüfung
     */
    protected boolean checkIfIdeaBelongsToChallenge(Challenge challenge, Idea idea, StringBuilder errorBuffer){
        if(! idea.getChallengeId().equals(challenge.getId())) {
            String error = String.format("Die Idee mit der Id %s gehört nicht zur Challenge %s!", idea.getId(), challenge.getId());
            errorBuffer.append(error);
            return false;
        }
        return true;
    }

    protected boolean checkIsAllowedToViewIdea(Challenge challenge, Idea idea, String currentUser, Date now, StringBuilder errorBuffer){
        if(! checkIfIdeaBelongsToChallenge(challenge, idea, errorBuffer)){
            return false;
        }
        if(! challengePermissionService.checkIsAllowedToViewChallenge(challenge, currentUser, errorBuffer)){
            return false;
        }
        // Die eigenen Ideen darf man immer anschauen
        if(checkIsTeamMember(idea, currentUser, errorBuffer)){
            return true;
        }
        // Fremde Ideen darf man nur sehen, wenn Idea submitted ist und man Gutachter ist
        if(challengePermissionService.checkIsReferee(challenge, currentUser, errorBuffer)) {
            switch (idea.getIdeaState()) {
                case DRAFT:
                    errorBuffer.append("Gutachter dürfen keine Ideen sehen, die im Zustand DRAFT sind!");
                    return false;
                case SUBMITTED:
                case READY_FOR_VOTE:
                    return true;
                default:
                    throw new IllegalStateException("Unexpected value: " + idea.getIdeaState());
            }
        }
        if(IdeaState.READY_FOR_VOTE.equals(idea.getIdeaState())){
            if(! challenge.getVotingStart().before(now)){
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                String error = String.format("Fremde Ideen können nur angesehen werden, wenn die Voting-Phase begonnen hat. Dies geschieht am %s; heute ist der %s", simpleDateFormat.format(challenge.getVotingStart()), simpleDateFormat.format(now));
                errorBuffer.append(error);
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    protected boolean checkIsTeamMember(Idea idea, String currentUser, StringBuilder errorBuffer){
        boolean result = idea.getTeamMember().contains(TeamMember.fromPerson(Person.builder().email(currentUser.toLowerCase()).build()));
        if(! result){
            errorBuffer.append(String.format("Der Nutzer %s ist kein Teammitglied der Idee %s", currentUser, idea.getId()));
        }
        return result;
    }

    protected boolean checkIsAllowedToEditIdea(Challenge challenge, Idea idea, String currentUser, Date now, StringBuilder errorBuffer) {
        if(! checkIfIdeaBelongsToChallenge(challenge, idea, errorBuffer)){
            return false;
        }
        if(! challengePermissionService.checkIsAllowedToViewChallenge(challenge, currentUser, errorBuffer)){
            return false;
        }
        if(! checkIsTeamMember(idea, currentUser, errorBuffer)){
            return false;
        }
        boolean challengeIsInInitialSubmitPhase = challenge.getSubmissionStart().before(now) && challenge.getReviewStart().after(now);
        boolean challengeIsInRefactoringPhase = challenge.getRefactoringStart() != null && (challenge.getRefactoringStart().before(now) && challenge.getVotingStart().after(now));
        if(! (challengeIsInInitialSubmitPhase || challengeIsInRefactoringPhase)){
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            errorBuffer.append(String.format("Idee %s kann nicht bearbeitet werden, weil die Challenge sich nicht initialen Einreichungsphase oder der Überarbeitungsphase befindet. " +
                    "Diese gingen vom %s bis %s, bzw. %s bis %s; heute ist der %s!", idea.getId(),
                    sdf.format(challenge.getSubmissionStart()), sdf.format(challenge.getReviewStart()),
                    challenge.getRefactoringStart() != null ? sdf.format(challenge.getRefactoringStart()) : "null", sdf.format(challenge.getVotingStart()),
                    sdf.format(now)));
            return false;
        }
        return true;
    }

    protected boolean checkIsAllowedToViewTeamChat(Challenge challenge, Idea idea, String currentUser, Date currentDate, StringBuilder errorBuffer) {
        if(! checkIsAllowedToViewIdea(challenge, idea, currentUser, currentDate, errorBuffer)){
            return false;
        }
        return checkIsTeamMember(idea, currentUser, errorBuffer);
    }

    protected boolean checkIsAllowedToViewRefereesChat(Challenge challenge, Idea idea, String currentUser, Date currentDate, StringBuilder errorBuffer) {
        if(! checkIsAllowedToViewIdea(challenge, idea, currentUser, currentDate, errorBuffer)){
            return false;
        }
        return challengePermissionService.checkIsReferee(challenge, currentUser, errorBuffer);
    }
}
