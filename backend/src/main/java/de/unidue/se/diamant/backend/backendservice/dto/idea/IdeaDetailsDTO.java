package de.unidue.se.diamant.backend.backendservice.dto.idea;

import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Visibility;
import de.unidue.se.diamant.backend.backendservice.service.chat.domain.ChatEntry;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.Idea;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.IdeaState;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.TeamMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdeaDetailsDTO {
    /**
     * ID der Idee
     */
    String id;

    /**
     * Name des Teams das die Idee entwickelt
     */
    private String teamName;

    /**
     * Kurzbeschreibung der Idee. Wird auf der Übersichtsseite der Challenge angezeigt!
     */
    private String shortDescription;

    /**
     * Liste der Elemente des Canvas. Die IDs der Elemente dürfen nicht doppelt vorkommen!
     */
    private List<CanvasElementDetailDTO> canvasElements;

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
     * Teammitglieder
     */
    private Set<TeamMember> teamMember;

    @Builder.Default
    private Set<ChatEntryDTO> teamChat = new HashSet<>();

    @Builder.Default
    private Set<ChatEntryDTO> refereesChat = new HashSet<>();

    @Builder.Default
    private Set<ChatEntryDTO> teamAndRefereesChat = new HashSet<>();

    /**
     * Siegerplatzierung
     */
    private int winningPlace;

    /**
     * Einladungslink für Teammitglieder
     */
    private String invitationLinkTeammember;

    private Visibility challengeVisibility;

    public static IdeaDetailsDTO from(Idea idea){
        return new IdeaDetailsDTO(
                idea.getId(),
                idea.getTeamName(),
                idea.getShortDescription(),
                idea.getCanvasElements().stream().map(CanvasElementDetailDTO::new).collect(Collectors.toList()),
                idea.getIdeaState(),
                Collections.emptyList(),
                null,
                null,
                idea.getTeamMember(),
                idea.getTeamChat().stream().map(ChatEntryDTO::fromChatEntry).collect(Collectors.toSet()),
                idea.getRefereesChat().stream().map(ChatEntryDTO::fromChatEntry).collect(Collectors.toSet()),
                idea.getTeamAndRefereesChat().stream().map(ChatEntryDTO::fromChatEntry).collect(Collectors.toSet()),
                idea.getWinningPlace() == null ? 0 : idea.getWinningPlace(),
                null,
                null
        );
    }
}
