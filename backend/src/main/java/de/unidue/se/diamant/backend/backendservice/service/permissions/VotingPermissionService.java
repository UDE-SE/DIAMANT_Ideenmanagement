package de.unidue.se.diamant.backend.backendservice.service.permissions;

import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.Idea;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.IdeaState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class VotingPermissionService {

    private ChallengePermissionService challengePermissionService;
    private IdeaPermissionsService ideaPermissionsService;

    @Autowired
    public VotingPermissionService(ChallengePermissionService challengePermissionService, IdeaPermissionsService ideaPermissionsService) {
        this.challengePermissionService = challengePermissionService;
        this.ideaPermissionsService = ideaPermissionsService;
    }

    /**
     * Prüft, ob abgestimmt werden darf
     */
    protected boolean checkIsAllowedToVote(Challenge challenge, Idea idea, String currentUserMail, Date now, StringBuilder errorBuffer){
        if(! challengePermissionService.checkIsAllowedToViewChallenge(challenge, currentUserMail, errorBuffer)) {
            return false;
        }
        if(! ideaPermissionsService.checkIfIdeaBelongsToChallenge(challenge, idea, errorBuffer)){
            return false;
        }
        if(! IdeaState.READY_FOR_VOTE.equals(idea.getIdeaState())){
            String error = String.format("Für die Idee %s kann nicht abgestimmt werden, weil sie nicht für das Voting nominiert wurde (sie ist im Zustand %s)", idea.getId(), idea.getIdeaState().toString());
            errorBuffer.append(error);
            return false;
        }
        if(! (now.after(challenge.getVotingStart()) && now.before(challenge.getImplementationStart()) )){
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            String error = String.format("Für die Idee %s kann nicht abgestimmt werden, weil dies nur zwischen dem %s und %s möglich ist. Heute ist jedoch %s", idea.getId(), format.format(challenge.getVotingStart()), format.format(challenge.getImplementationStart()), format.format(now));
            errorBuffer.append(error);
            return false;
        }
        return true;
    }

    public boolean checkIsAllowedToViewVotes(Challenge challenge, Date now, StringBuilder errorBuffer) {
        if(! challenge.getImplementationStart().before(now)){
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            errorBuffer.append(String.format("Fremnde Votings können erst nach Ende der Votingphase eingesehen werden. Diese Endet am %s heute ist der %s", format.format(challenge.getImplementationStart()), format.format(now)));
            return false;
        }
        return true;
    }
}
