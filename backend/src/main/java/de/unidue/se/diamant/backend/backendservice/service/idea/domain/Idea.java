package de.unidue.se.diamant.backend.backendservice.service.idea.domain;

import de.unidue.se.diamant.backend.backendservice.dto.idea.NewIdeaDTO;
import de.unidue.se.diamant.backend.backendservice.service.chat.domain.ChatEntry;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class Idea {

    @Id
    private String id;

    /**
     * ID der challenge zu der die Idee gehört
     */
    private String challengeId;

    /**
     * Ersteller der Idee
     */
    private Person creator;

    /**
     * Name dea Teams
     */
    private String teamName;

    /**
     * Kurzbeschreibung der Idee
     */
    private String shortDescription;

    /**
     * Elemente des Canvas
     */
    @Builder.Default
    private List<CanvasElement> canvasElements = new ArrayList<>();

    /**
     * Status der Idee
     */
    private IdeaState ideaState;

    /**
     * Teammitglieder
     */
    @Builder.Default
    private Set<TeamMember> teamMember = new HashSet<>();

    @Builder.Default
    private Set<ChatEntry> teamChat = new HashSet<>();

    @Builder.Default
    private Set<ChatEntry> refereesChat = new HashSet<>();

    @Builder.Default
    private Set<ChatEntry> teamAndRefereesChat = new HashSet<>();

    private Integer winningPlace;

    public static Idea fromIdeaDTO(NewIdeaDTO dto) {
        return new Idea(
                null,
                null,
                null,
                dto.getTeamName(),
                dto.getShortDescription(),
                dto.getCanvasElements(),
                IdeaState.DRAFT,
                dto.getTeamMember() != null ? dto.getTeamMember().stream().map(TeamMember::fromPerson).collect(Collectors.toSet()) : new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                0
        );
    }
}
