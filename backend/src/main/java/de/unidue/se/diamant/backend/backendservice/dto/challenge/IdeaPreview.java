package de.unidue.se.diamant.backend.backendservice.dto.challenge;

import de.unidue.se.diamant.backend.backendservice.dto.idea.IdeaPermissions;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.Idea;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.IdeaState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdeaPreview {
    /**
     * Id der Idee
     */
    private String id;
    /**
     * Kurzbeschreibung
     */
    private String shortDescription;
    /**
     * Teamname
     */
    private String teamName;

    /**
     * Aktueller Status der Idee
     */
    private IdeaState state;

    /**
     * Rechte des aktuellen Nutzers
     */
    private List<IdeaPermissions> currentUserPermissions;

    /**
     * Eigener Abstimmungswert.
     */
    private Integer ownVoting;

    /**
     * Durchschnittlicher Abstimmwert.
     */
    private Double averageVoting;

    /**
     * Siegerplatzierung
     */
    private int winningPlace;

    public static IdeaPreview fromIdea(Idea idea) {
        return new IdeaPreview(idea.getId(), idea.getShortDescription(), idea.getTeamName(), idea.getIdeaState(), Collections.emptyList(), null, null, idea.getWinningPlace() == null ? 0 : idea.getWinningPlace());
    }
}
