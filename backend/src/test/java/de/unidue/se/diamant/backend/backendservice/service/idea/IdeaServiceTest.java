package de.unidue.se.diamant.backend.backendservice.service.idea;

import de.unidue.se.diamant.backend.backendservice.dto.idea.IdeaPermissions;
import de.unidue.se.diamant.backend.backendservice.dto.idea.NewIdeaDTO;
import de.unidue.se.diamant.backend.backendservice.dto.idea.update.UpdateCanvasElementDTO;
import de.unidue.se.diamant.backend.backendservice.service.AttachmentService;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.*;
import de.unidue.se.diamant.backend.backendservice.service.permissions.PermissionServiceFacade;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;

import javax.management.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class IdeaServiceTest {

    @Test
    public void saveIdea() {
        IdeaRepository ideaRepository = Mockito.mock(IdeaRepository.class);
        IdeaService sut = new IdeaService(ideaRepository, null, null, null);

        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";
        String currentUser = "usER@example.com";

        Idea idea = Idea.builder().challengeId("someStrangeChallengeId").build();
        Idea ideaWithId = Idea.builder().id(ideaId).build();

        ArgumentCaptor<Idea> saveIdeaCaptor = ArgumentCaptor.forClass(Idea.class);

        Mockito.when(ideaRepository.save(saveIdeaCaptor.capture())).thenReturn(ideaWithId);

        String savedId = sut.saveIdea(idea);

        Assertions.assertThat(savedId).isEqualTo(ideaId);
        Mockito.verify(ideaRepository, Mockito.only()).save(saveIdeaCaptor.capture());
    }

    @Test
    public void createIdea(){
        IdeaService sut = new IdeaService(null, null, null, null);

        NewIdeaDTO ideaDTO = new NewIdeaDTO();
        ideaDTO.setShortDescription("kurze KurzBeschreibung");
        ideaDTO.setCanvasElements(new ArrayList<>());
        ideaDTO.getCanvasElements().add(CanvasElement.builder().id("1").type(CanvasElementType.TEXT).content("Content").build());
        ideaDTO.getCanvasElements().add(CanvasElement.builder().id("2").type(CanvasElementType.ATTACHMENT).content("Content").build());
        ideaDTO.setTeamName("HeyJaBVB");
        ideaDTO.getTeamMember().add(Person.builder().email("a@Example.com").build());
        ideaDTO.getTeamMember().add(Person.builder().email("B@example.com").build());

        Idea createIdea = sut.createIdea(ideaDTO, "challId", Person.builder().email("creatORRR@example.com").build());

        Assertions.assertThat(createIdea.getIdeaState()).isEqualTo(IdeaState.DRAFT);
        Assertions.assertThat(createIdea.getChallengeId()).isEqualTo("challId");
        Assertions.assertThat(createIdea.getCreator().getEmail()).isEqualTo("creatORRR@example.com".toLowerCase());
        Assertions.assertThat(createIdea.getTeamName()).isEqualTo("HeyJaBVB");
        Assertions.assertThat(createIdea.getShortDescription()).isEqualTo("kurze KurzBeschreibung");
        Assertions.assertThat(createIdea.getCanvasElements()).containsExactlyInAnyOrder(CanvasElement.builder().id("1").type(CanvasElementType.TEXT).content("Content").build(), CanvasElement.builder().id("2").type(CanvasElementType.ATTACHMENT).content("Content").build());
        Assertions.assertThat(createIdea.getTeamMember()).containsExactlyInAnyOrder(TeamMember.fromPerson(Person.builder().email("a@example.com").build()), TeamMember.fromPerson(Person.builder().email("b@example.com").build()), TeamMember.fromPerson(Person.builder().email("creatORRR@example.com").build()));
    }

    @Test
    public void getPermissions() {
        PermissionServiceFacade permissionService = Mockito.mock(PermissionServiceFacade.class);
        IdeaService sut = new IdeaService(null, null, permissionService, null);

        Challenge challenge = Challenge.builder().build();
        Idea idea = Idea.builder().build();
        String currentUser = "user@example.com";

        Mockito.when(permissionService.checkIsAllowedToVote(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(false);
        Mockito.when(permissionService.checkIsAllowedToNominateIdeaForVoting(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(false);

        List<IdeaPermissions> permissions = sut.getPermissions(challenge, idea, currentUser);
        Assertions.assertThat(permissions).isEmpty();
    }

    @Test
    public void getPermissionsVotePermission() {
        PermissionServiceFacade permissionService = Mockito.mock(PermissionServiceFacade.class);
        IdeaService sut = new IdeaService(null, null, permissionService, null);

        Challenge challenge = Challenge.builder().build();
        Idea idea = Idea.builder().build();
        String currentUser = "user@example.com";

        Mockito.when(permissionService.checkIsAllowedToVote(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionService.checkIsAllowedToNominateIdeaForVoting(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionService.checkIsAllowedToEditIdea(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(true);

        List<IdeaPermissions> permissions = sut.getPermissions(challenge, idea, currentUser);
        Assertions.assertThat(permissions).containsExactlyInAnyOrder(IdeaPermissions.VOTE);
    }

    @Test
    public void getPermissionsNominatePermission() {
        PermissionServiceFacade permissionService = Mockito.mock(PermissionServiceFacade.class);
        IdeaService sut = new IdeaService(null, null, permissionService, null);

        Challenge challenge = Challenge.builder().build();
        Idea idea = Idea.builder().build();
        String currentUser = "user@example.com";

        Mockito.when(permissionService.checkIsAllowedToVote(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(false);
        Mockito.when(permissionService.checkIsAllowedToNominateIdeaForVoting(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionService.checkIsAllowedToEditIdea(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(false);

        List<IdeaPermissions> permissions = sut.getPermissions(challenge, idea, currentUser);
        Assertions.assertThat(permissions).containsExactly(IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE);
    }

    @Test
    public void getPermissionsEditAndNominatePermission() {
        PermissionServiceFacade permissionService = Mockito.mock(PermissionServiceFacade.class);
        IdeaService sut = new IdeaService(null, null, permissionService, null);

        Challenge challenge = Challenge.builder().build();
        Idea idea = Idea.builder().build();
        String currentUser = "user@example.com";

        Mockito.when(permissionService.checkIsAllowedToVote(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(false);
        Mockito.when(permissionService.checkIsAllowedToNominateIdeaForVoting(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionService.checkIsAllowedToEditIdea(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(true);

        List<IdeaPermissions> permissions = sut.getPermissions(challenge, idea, currentUser);
        Assertions.assertThat(permissions).containsExactly(IdeaPermissions.EDIT, IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE);
    }

    @Test
    public void updateCanvas() {
        MongoTemplate mongoTemplate = Mockito.mock(MongoTemplate.class);
        AttachmentService attachmentService = Mockito.mock(AttachmentService.class);
        IdeaService sut = new IdeaService(null, mongoTemplate, null, attachmentService);

        String ideaId = "5dc40f47d270c23cccc43b8a";
        UpdateCanvasElementDTO updateCanvasElementDTO = new UpdateCanvasElementDTO(
                Arrays.asList(
                        CanvasElement.builder().id("2").build(),
                        CanvasElement.builder().id("43").build()
                )
        );
        Mockito.doNothing().when(attachmentService).deleteAllUnreferencedAttachments(Mockito.eq(ideaId), Mockito.eq(Arrays.asList("2", "43")));

        sut.updateCanvasElements(ideaId, updateCanvasElementDTO);
        Mockito.verify(attachmentService, Mockito.only()).deleteAllUnreferencedAttachments(Mockito.eq(ideaId), Mockito.eq(Arrays.asList("2", "43")));
    }

}
