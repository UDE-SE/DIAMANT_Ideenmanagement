package de.unidue.se.diamant.backend.backendservice.service.permissions;

import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.Idea;
import de.unidue.se.diamant.backend.backendservice.service.time.TimeService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Log
@Service
public class ChallengePermissionService {

    private TimeService timeService;

    @Autowired
    public ChallengePermissionService(TimeService timeService) {
        this.timeService = timeService;
    }


    /**
     * Prüft, ob der angegebene Nutzer Anhänge zu der Challenge hinzufügen darf
     * @param challenge Challenge
     * @param currentUserMail Nutzer
     * @param errorBuffer Speicher für mögliche Fehler
     * @return Ergebnis der Prüfung
     */
    protected boolean checkIsAllowedToUploadAttachmentToChallenge(Challenge challenge, String currentUserMail, StringBuilder errorBuffer) {
        if (! checkIsCaller(challenge, currentUserMail, new StringBuilder())) {
            String caller = challenge.getCaller().getEmail();
            errorBuffer.append(String.format("Nutzer '%s' hat versucht Anhänge für die Challenge '%s' hinzuzufügen, diese gehört jedoch '%s'", currentUserMail, challenge.getId(), caller));
            return false;
        }
        return true;
    }

    /**
     * Prüft, ob ein gegebener Nutzer die Rechte hat, eine bestimmte Challenge anzuzeigen.
     * Gutachter dürfen "Ihre" Challenges unabhängig von Fristen/Einladestatus/Sichtbarkeiten immer sehen
     * @param challengeDetails Challenge, die überprüft werden soll
     * @param currentUserMail Nutzer, der Versucht die Challenge anzuschauen
     * @param errorBuffer StringBuilder, in den Fehler geschrieben werden.
     */
    protected boolean checkIsAllowedToViewChallenge(Challenge challengeDetails, String currentUserMail, StringBuilder errorBuffer) {
        // Gutachter dürfen "Ihre" Challenges unabhängig von Fristen/Einladestatus/Sichtbarkeiten immer sehen
        if(checkIsReferee(challengeDetails, currentUserMail, errorBuffer)){
            return true;
        }
        boolean deadlineCheckPositive = checkIfDeadLinesAllowViewing(challengeDetails, timeService.getTime(), errorBuffer);
        boolean visibilityCheckPositive = checkIfVisibilitiesAllowViewing(challengeDetails, currentUserMail, errorBuffer);
        return deadlineCheckPositive && visibilityCheckPositive;
    }

    /**
     * Prüft, ob der aktuelle Nutzer die angegebene Challenge gemäß ihrer Sichtbarkeit sehen darf
     * @param challenge Challenge, die geprüft wird
     * @param currentUserMail aktueller Nutzer
     * @param errorBuffer Buffer für Fehlermeldungen
     * @return Indikator, ob die Bedingungen erfüllt isnd.
     */
    protected boolean checkIfVisibilitiesAllowViewing(Challenge challenge, String currentUserMail, StringBuilder errorBuffer) {
        if(currentUserMail == null){
            currentUserMail = "";
        }
        switch (challenge.getVisibility()) {
            case OPEN:
                return true;
            case INTERNAL:
                String companyOfCaller = challenge.getCaller().getEmail().substring(challenge.getCaller().getEmail().indexOf("@") + 1);
                String companyOfCurrentUser = currentUserMail.substring(currentUserMail.indexOf("@") + 1);
                if(! companyOfCaller.equalsIgnoreCase(companyOfCurrentUser)){
                    errorBuffer.append(String.format("Der Nutzer %s gehört zur Firma %s, möchte aber auf eine Challenge mit der Sichtbarkeit INTERN der Firma %s zugreifen. Das ist verboten!", currentUserMail, companyOfCurrentUser, companyOfCaller));
                    return false;
                }
                return true;
            case INVITE:
                String finalCurrentUserMail = currentUserMail;
                boolean isInvited = challenge.getInvitedUsers().stream().anyMatch((person) -> person.getEmail().equals(finalCurrentUserMail.toLowerCase()));
                if(! isInvited){
                    errorBuffer.append(String.format("Der Nutzer %s versucht auf eine Challenge zuzugreifen, auf die nur eingeladene Nutzer Zugriff haben, ist selbst aber nicht eingeladen!", currentUserMail));
                }
                return isInvited;
            default:
                throw new IllegalStateException("Unexpected value: " + challenge.getVisibility());
        }
    }

    /**
     * Prüft, ob die zeitlichen Voraussetzungen erfüllt sind, sodass eine Challenge angezeigt werden kann
     * @param challenge Zu prüfende Challenge
     * @param currentDate Aktuelles Datum (als Parameter, damit es in Tests gemockt werden kann)
     * @param errorBuffer Buffer für Fehlermeldungen
     * @return Indikator, ob die Bedingungen erfüllt isnd.
     */
    protected boolean checkIfDeadLinesAllowViewing(Challenge challenge, Date currentDate, StringBuilder errorBuffer) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        if(challenge.getSubmissionStart().after(currentDate)){
            errorBuffer.append(String.format("Die Einreichphase beginnt erst am %s, es wird aber versucht heute (am %s) auf die Challenge zuzugreifen!", formatter.format(challenge.getSubmissionStart()), formatter.format(currentDate)));
            return false;
        }
        if(challenge.getChallengeEnd().before(currentDate)){
            errorBuffer.append(String.format("Die Challenge hat am %s geendet, es wird aber versucht heute (am %s) auf die Challenge zuzugreifen!", formatter.format(challenge.getChallengeEnd()), formatter.format(currentDate)));
            return false;
        }
        return true;
    }

    protected boolean checkIsReferee(Challenge challenge, String currentUser, StringBuilder errorBuffer) {
        boolean result = challenge.getReferees().contains(Person.builder().email(currentUser.toLowerCase()).build());
        if(! result){
            errorBuffer.append(String.format("Der Nutzer %s ist kein Gutachter der Challenge %s", currentUser, challenge.getId()));
        }
        return result;
    }

    protected boolean checkIsCaller(Challenge challenge, String currentUser, StringBuilder errorBuffer) {
        boolean result = challenge.getCaller().equals(Person.builder().email(currentUser.toLowerCase()).build());
        if(! result){
            errorBuffer.append(String.format("Der Nutzer %s ist nicht der Ersteller der Challenge %s", currentUser, challenge.getId()));
        }
        return result;
    }

    public boolean checkHasEditPermission(Challenge challengeDetails, String currentUserMail, StringBuilder errorBuffer) {
        return checkIsCaller(challengeDetails, currentUserMail, errorBuffer);
    }

    public boolean checkIsAllowedToNominateAsWinner(Challenge challenge, String currentUser, StringBuilder errorBuffer) {
        return checkIsCaller(challenge, currentUser, errorBuffer) && checkDeadlinesAllowWinnerNomination(challenge, timeService.getTime(), errorBuffer);

    }

    protected boolean checkDeadlinesAllowWinnerNomination(Challenge challenge, Date currentDate, StringBuilder errorBuffer){
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        if(challenge.getImplementationStart().after(currentDate)){
            errorBuffer.append(String.format("Die VotingPhase wurde noch nicht beendet. Nominierungen als Gewinner sind erst ab dem %s möglich. Heute ist der %s", formatter.format(challenge.getImplementationStart()), formatter.format(currentDate)));
            return false;
        }
        if(challenge.getChallengeEnd().before(currentDate)){
            errorBuffer.append(String.format("Die Challenge hat am %s geendet, es wird aber versucht heute (am %s) auf die Challenge zuzugreifen!", formatter.format(challenge.getChallengeEnd()), formatter.format(currentDate)));
            return false;
        }
        return true;
    }
}
