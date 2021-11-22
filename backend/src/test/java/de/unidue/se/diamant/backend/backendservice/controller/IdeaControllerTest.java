package de.unidue.se.diamant.backend.backendservice.controller;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import de.unidue.se.diamant.backend.backendservice.dto.idea.*;
import de.unidue.se.diamant.backend.backendservice.dto.idea.update.*;
import de.unidue.se.diamant.backend.backendservice.service.AttachmentService;
import de.unidue.se.diamant.backend.backendservice.service.challenge.ChallengeService;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Visibility;
import de.unidue.se.diamant.backend.backendservice.service.chat.domain.ChatEntry;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import de.unidue.se.diamant.backend.backendservice.service.idea.IdeaService;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.*;
import de.unidue.se.diamant.backend.backendservice.service.invitation.InvitationInformation;
import de.unidue.se.diamant.backend.backendservice.service.invitation.JWTService;
import de.unidue.se.diamant.backend.backendservice.service.keycloak.KeycloakUserService;
import de.unidue.se.diamant.backend.backendservice.service.keycloak.KeycloakUtils;
import de.unidue.se.diamant.backend.backendservice.service.permissions.DenyAccessInterceptor;
import de.unidue.se.diamant.backend.backendservice.service.permissions.PermissionServiceFacade;
import de.unidue.se.diamant.backend.backendservice.service.vote.VoteService;
import de.unidue.se.diamant.backend.backendservice.service.vote.dto.AverageAndOwnVoteForIdea;
import org.assertj.core.api.Assertions;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;

import java.util.*;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "app.server.base.url=https://diamant.se-tools.de")
public class IdeaControllerTest extends RestDocTest {

    @MockBean
    private IdeaService ideaService;

    @MockBean
    private ChallengeService challengeService;

    @MockBean
    private AttachmentService attachmentService;

    @MockBean
    private DenyAccessInterceptor denyAccessInterceptor;

    @MockBean
    private VoteService voteService;

    @MockBean
    private PermissionServiceFacade permissionServiceFacade;

    @MockBean
    private KeycloakUserService keycloakUserService;

    @MockBean
    private JWTService jwtService;

    private final String userMail = "caller@uni-due.de";

    @Test
    @WithMockUser(username = userMail, roles = {"user"})
    public void saveIdea() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        Challenge challenge = Challenge.builder().id(challengeId).build();
        String ideaId = "5dc40f47d270c23cccc43b8a";
        NewIdeaDTO postArguments = new NewIdeaDTO(
                "Heja BVB Team",
                "Kurzbeschreibung",
                Arrays.asList(
                        CanvasElement.builder().id("1").position(new Position(10, 20, 2, 3)).type(CanvasElementType.TEXT).content("Lorem Ipsum").build(),
                        CanvasElement.builder().id("2").position(new Position(20, 10, 2, 3)).type(CanvasElementType.IMAGE).content("Nicht gefunde").build()
                ),
                null
        );

        ArgumentCaptor<Idea> saveCaptor = ArgumentCaptor.forClass(Idea.class);

        Mockito.when(challengeService.getChallengeDetails(Mockito.eq(challengeId))).thenReturn(challenge);
        Mockito.when(ideaService.createIdea(Mockito.any(NewIdeaDTO.class), Mockito.anyString(), Mockito.any(Person.class))).thenCallRealMethod();
        Mockito.when(ideaService.saveIdea(saveCaptor.capture())).thenReturn(ideaId);
        Mockito.when(keycloakUserService.checkIfUserExists(userMail)).thenReturn(true);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(userMail), Mockito.any(StringBuilder.class))).thenReturn(true);

        this.mockMvc.perform(
                post("/api/challenge/" + challengeId + "/idea")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(postArguments))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(ideaId));

        Mockito.verify(ideaService).saveIdea(saveCaptor.capture());
        Assertions.assertThat(saveCaptor.getValue().getTeamName()).isEqualTo(postArguments.getTeamName());
        Assertions.assertThat(saveCaptor.getValue().getCanvasElements().get(0)).isEqualTo(postArguments.getCanvasElements().get(0));
        Assertions.assertThat(saveCaptor.getValue().getCanvasElements().get(1)).isEqualTo(postArguments.getCanvasElements().get(1));
    }

    @Test
    @WithMockUser(username = userMail, roles = {"caller"})
    public void saveIdeaWithCallerAccount() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        Challenge challenge = Challenge.builder().id(challengeId).build();
        String ideaId = "5dc40f47d270c23cccc43b8a";
        NewIdeaDTO postArguments = new NewIdeaDTO(
                "Heja BVB Team",
                "Kurzbeschreibung",
                Arrays.asList(
                        CanvasElement.builder().id("1").position(new Position(10, 20, 2, 3)).type(CanvasElementType.TEXT).content("Lorem Ipsum").build(),
                        CanvasElement.builder().id("2").position(new Position(20, 10, 2, 3)).type(CanvasElementType.IMAGE).content("Nicht gefunde").build()
                ),
                new HashSet<>()
        );
        postArguments.getTeamMember().add(Person.builder().name("tm1").email("tm1@example.com").build());
        postArguments.getTeamMember().add(Person.builder().name("tm2").email("tm2@example.com").build());
        ArgumentCaptor<Idea> saveCaptor = ArgumentCaptor.forClass(Idea.class);

        Mockito.when(challengeService.getChallengeDetails(Mockito.eq(challengeId))).thenReturn(challenge);
        Mockito.when(ideaService.createIdea(Mockito.any(NewIdeaDTO.class), Mockito.anyString(), Mockito.any(Person.class))).thenCallRealMethod();
        Mockito.when(ideaService.saveIdea(saveCaptor.capture())).thenReturn(ideaId);
        Mockito.when(keycloakUserService.checkIfUserExists(userMail)).thenReturn(true);
        Mockito.when(keycloakUserService.checkIfUserExists("tm1@example.com")).thenReturn(true);
        Mockito.when(keycloakUserService.checkIfUserExists("tm2@example.com")).thenReturn(true);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(userMail), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq("tm1@example.com"), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq("tm2@example.com"), Mockito.any(StringBuilder.class))).thenReturn(true);


        this.mockMvc.perform(
                post("/api/challenge/" + challengeId + "/idea")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(postArguments))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(ideaId));

        Mockito.verify(ideaService).saveIdea(saveCaptor.capture());
        Assertions.assertThat(saveCaptor.getValue().getTeamName()).isEqualTo(postArguments.getTeamName());
        Assertions.assertThat(saveCaptor.getValue().getCanvasElements().get(0)).isEqualTo(postArguments.getCanvasElements().get(0));
        Assertions.assertThat(saveCaptor.getValue().getCanvasElements().get(1)).isEqualTo(postArguments.getCanvasElements().get(1));
        Mockito.verify(keycloakUserService, Mockito.times(1)).checkIfUserExists(userMail);
        Mockito.verify(keycloakUserService, Mockito.times(1)).checkIfUserExists("tm1@example.com");
        Mockito.verify(keycloakUserService, Mockito.times(1)).checkIfUserExists("tm2@example.com");
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(userMail), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq("tm1@example.com"), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq("tm2@example.com"), Mockito.any(StringBuilder.class));
    }

    @Test
    @WithMockUser(username = userMail, roles = {"user"})
    public void updateStatusFieldToSubmitted() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";


        UpdateIdeaFieldDTO updateContent = new UpdateIdeaFieldDTO(
                UpdateIdeaFieldDTO.Field.STATE,
                IdeaState.SUBMITTED
        );

        Mockito.doNothing().when(denyAccessInterceptor).interceptUpdateIdeaState(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.eq(IdeaState.SUBMITTED), Mockito.any(Authentication.class));
        Mockito.doNothing().when(ideaService).updateState(Mockito.eq(ideaId), Mockito.eq(IdeaState.SUBMITTED));

        this.mockMvc.perform(
                put("/api/challenge/" + challengeId + "/idea/" + ideaId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateContent))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(ideaId));

        Mockito.verify(ideaService).updateState(Mockito.eq(ideaId), Mockito.eq(IdeaState.SUBMITTED));
        Mockito.verify(denyAccessInterceptor).interceptUpdateIdeaState(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.eq(IdeaState.SUBMITTED), Mockito.any(Authentication.class));
    }

    @Test
    @WithMockUser(username = userMail, roles = {"user"})
    public void updateStatusFieldToReadyForVote() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";


        UpdateIdeaFieldDTO updateContent = new UpdateIdeaFieldDTO(
                UpdateIdeaFieldDTO.Field.STATE,
                IdeaState.READY_FOR_VOTE
        );

        Mockito.doNothing().when(denyAccessInterceptor).interceptUpdateIdeaState(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.eq(IdeaState.READY_FOR_VOTE), Mockito.any(Authentication.class));
        Mockito.doNothing().when(ideaService).updateState(Mockito.eq(ideaId), Mockito.eq(IdeaState.READY_FOR_VOTE));

        this.mockMvc.perform(
                put("/api/challenge/" + challengeId + "/idea/" + ideaId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateContent))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(ideaId));

        Mockito.verify(ideaService).updateState(Mockito.eq(ideaId), Mockito.eq(IdeaState.READY_FOR_VOTE));
        Mockito.verify(denyAccessInterceptor).interceptUpdateIdeaState(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.eq(IdeaState.READY_FOR_VOTE), Mockito.any(Authentication.class));
    }

    @Test
    @WithMockUser(username = userMail, roles = {"user"})
    public void updateStatusFieldShouldFailForRandomValue() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";


        UpdateIdeaFieldDTO updateContent = new UpdateIdeaFieldDTO(
                UpdateIdeaFieldDTO.Field.STATE,
                "komischer anderer Status"
        );

        Mockito.doNothing().when(denyAccessInterceptor).interceptUpdateIdeaState(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.eq(IdeaState.READY_FOR_VOTE), Mockito.any(Authentication.class));

        this.mockMvc.perform(
                put("/api/challenge/" + challengeId + "/idea/" + ideaId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateContent))
        )
                .andDo(print())
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(ideaService, denyAccessInterceptor);
    }

    @Test
    @WithMockUser(username = userMail, roles = {"user"})
    public void getIdeaDetails() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";
        String teamName = "hejaBVBTeam";
        String shortDescription = "Kurzbeschreeeeibung";

        Person challengeCaller = Person.builder().email("someOtherCaller@example.com").build();
        Challenge challenge = Challenge.builder().caller(challengeCaller).visibility(Visibility.INTERNAL).build();
        Mockito.when(challengeService.getChallengeDetails(Mockito.eq(challengeId))).thenReturn(challenge);
        Mockito.when(jwtService.generateInvitationToken(Mockito.any(InvitationInformation.class))).thenReturn("token1");

        CanvasElement textElement = CanvasElement.builder()
                .id("1")
                .type(CanvasElementType.TEXT)
                .position(Position.builder().x(5).y(10).width(2).height(3).build())
                .content("Lorem Ipsum etc :)")
                .build();
        CanvasElement imageElement = CanvasElement.builder()
                .id("2")
                .type(CanvasElementType.IMAGE)
                .position(Position.builder().x(10).y(5).width(4).height(20).build())
                .content("Das hier müsste überschriebne werden...")
                .build();
        CanvasElement etcElement = CanvasElement.builder()
                .id("3")
                .type(CanvasElementType.ATTACHMENT)
                .position(Position.builder().x(3).y(7).width(4).height(20).build())
                .content("Das hier müsste auch überschriebne werden...")
                .build();

        Idea idea = new Idea(
                ideaId,
                challengeId,
                Person.builder().email(userMail).build(),
                teamName,
                shortDescription,
                Arrays.asList(
                        textElement, imageElement, etcElement
                ),
                IdeaState.DRAFT,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                0
        );
        idea.getTeamMember().add(TeamMember.fromPerson(Person.builder().name("Max Mustermann").email(userMail).build()));

        AverageAndOwnVoteForIdea vote = AverageAndOwnVoteForIdea.builder().ideaId(ideaId).averageVote(13.33).ownVote(3).build();

        Mockito.when(ideaService.findById(Mockito.eq(ideaId))).thenReturn(idea);
        Mockito.when(ideaService.getPermissions(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail))).thenReturn(Arrays.asList(IdeaPermissions.EDIT, IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE));
        Mockito.when(voteService.getAvgAndOwnVoteForIdea(Mockito.eq(ideaId), Mockito.eq(userMail))).thenReturn(vote);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewVotes(Mockito.eq(challenge), Mockito.any(StringBuilder.class))).thenReturn(true);

        String attachmentId1 = "5c6c25fe3accaf414c792f49";
        String fileName1 = "Datei 1";
        String attachmentBusinessId1 = "2";
        String contentType1 = "image/png";
        String attachmentId2 = "5c617f7c0f4a6437b4f769fc";
        String fileName2 = "Datei 2";
        String attachmentBusinessId2 = "3";
        String contentType2 = "application/pdf";
        GridFSFindIterable attachments = createAttachmentMocks(challengeId, ideaId,
                attachmentId1, attachmentBusinessId1, fileName1, contentType1,
                attachmentId2, attachmentBusinessId2, fileName2, contentType2);
        Mockito.when(attachmentService.findIdeaAttachmentsByIdeaId(Mockito.eq(ideaId))).thenReturn(attachments);


        IdeaDetailsDTO expected = IdeaDetailsDTO.from(idea);
        CanvasElementDetailDTO attachment1 = new CanvasElementDetailDTO(CanvasElement.builder()
                .id("1")
                .type(CanvasElementType.TEXT)
                .position(Position.builder().x(5).y(10).width(2).height(3).build())
                .content("Lorem Ipsum etc :)")
                .build());

        CanvasElementDetailDTO attachment2 = new CanvasElementDetailDTO(CanvasElement.builder()
                .id("2")
                .type(CanvasElementType.IMAGE)
                .position(Position.builder().x(10).y(5).width(4).height(20).build())
                .content(String.format("https://diamant.se-tools.de/api/challenge/%s/idea/%s/attachment/%s", challengeId, ideaId, attachmentId1))
                .build());
        attachment2.setFileName(fileName1);
        CanvasElementDetailDTO attachment3 = new CanvasElementDetailDTO(CanvasElement.builder()
                .id("3")
                .type(CanvasElementType.ATTACHMENT)
                .position(Position.builder().x(3).y(7).width(4).height(20).build())
                .content(String.format("https://diamant.se-tools.de/api/challenge/%s/idea/%s/attachment/%s", challengeId, ideaId, attachmentId2))
                .build());
        attachment3.setFileName(fileName2);
        expected.setCanvasElements(Arrays.asList(
            attachment1, attachment2, attachment3
        ));
        expected.setCurrentUserPermissions(Arrays.asList(IdeaPermissions.EDIT, IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE));
        expected.setOwnVoting(vote.getOwnVote());
        expected.setAverageVoting(vote.getAverageVote());
        expected.getTeamMember().add(TeamMember.fromPerson(Person.builder().name("Max Mustermann").email(userMail).build()));
        expected.setTeamChat(null);
        expected.setRefereesChat(null);
        expected.setTeamAndRefereesChat(null);
        expected.setInvitationLinkTeammember("https://diamant.se-tools.de/invitation/token1");
        expected.setChallengeVisibility(Visibility.INTERNAL);

        this.mockMvc.perform(
                get("/api/challenge/" + challengeId + "/idea/" + ideaId)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)));
        Mockito.verify(denyAccessInterceptor, Mockito.only()).interceptViewIdea(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.any(Authentication.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewVotes(Mockito.eq(challenge), Mockito.any(StringBuilder.class));
    }

    @Test
    @WithMockUser(username = userMail, roles = {"user"})
    public void getIdeaDetailsWithTeamChatAndTeamAndReferees() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";
        String teamName = "hejaBVBTeam";
        String shortDescription = "Kurzbeschreeeeibung";

        Person challengeCaller = Person.builder().email("someOtherCaller@example.com").build();
        Challenge challenge = Challenge.builder().caller(challengeCaller).build();
        Mockito.when(challengeService.getChallengeDetails(Mockito.eq(challengeId))).thenReturn(challenge);
        Mockito.when(jwtService.generateInvitationToken(Mockito.any(InvitationInformation.class))).thenReturn("token1");

        CanvasElement textElement = CanvasElement.builder()
                .id("1")
                .type(CanvasElementType.TEXT)
                .position(Position.builder().x(5).y(10).width(2).height(3).build())
                .content("Lorem Ipsum etc :)")
                .build();

        Idea idea = new Idea(
                ideaId,
                challengeId,
                Person.builder().email(userMail).build(),
                teamName,
                shortDescription,
                Collections.singletonList(textElement),
                IdeaState.DRAFT,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                0
        );
        idea.getTeamMember().add(TeamMember.fromPerson(Person.builder().name("Max Mustermann").email(userMail).build()));
        idea.getTeamChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Meier").email(userMail).build()).text("TeamChatEintrag").build());
        idea.getTeamAndRefereesChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Meier1").email(userMail).build()).text("GutachterUndTeamEintrag 1").build());
        idea.getTeamAndRefereesChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Meier2").email("andere@Mail.de").build()).text("GutachterUndTeamEintrag 2").build());
        idea.getRefereesChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Meier2").email("andere@Mail.de").build()).text("NurGutachterEintrag 2").build());

        AverageAndOwnVoteForIdea vote = AverageAndOwnVoteForIdea.builder().ideaId(ideaId).averageVote(13.33).ownVote(3).build();

        String attachmentId1 = "5c6c25fe3accaf414c792f49";
        String fileName1 = "Datei 1";
        String attachmentBusinessId1 = "2";
        String contentType1 = "image/png";
        String attachmentId2 = "5c617f7c0f4a6437b4f769fc";
        String fileName2 = "Datei 2";
        String attachmentBusinessId2 = "3";
        String contentType2 = "application/pdf";
        GridFSFindIterable attachments = createAttachmentMocks(challengeId, ideaId,
                attachmentId1, attachmentBusinessId1, fileName1, contentType1,
                attachmentId2, attachmentBusinessId2, fileName2, contentType2);
        Mockito.when(attachmentService.findIdeaAttachmentsByIdeaId(Mockito.eq(ideaId))).thenReturn(attachments);

        Mockito.when(ideaService.findById(Mockito.eq(ideaId))).thenReturn(idea);
        Mockito.when(ideaService.getPermissions(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail))).thenReturn(Arrays.asList(IdeaPermissions.EDIT, IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE));
        Mockito.when(voteService.getAvgAndOwnVoteForIdea(Mockito.eq(ideaId), Mockito.eq(userMail))).thenReturn(vote);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewTeamChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewTeamAndRefereeChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewRefereeChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class))).thenReturn(false);

        IdeaDetailsDTO expected = IdeaDetailsDTO.from(idea);
        CanvasElementDetailDTO attachment1 = new CanvasElementDetailDTO(CanvasElement.builder()
                .id("1")
                .type(CanvasElementType.TEXT)
                .position(Position.builder().x(5).y(10).width(2).height(3).build())
                .content("Lorem Ipsum etc :)")
                .build());

        expected.setCanvasElements(Collections.singletonList(attachment1));
        expected.setCurrentUserPermissions(Arrays.asList(IdeaPermissions.EDIT, IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE));
        expected.getTeamMember().add(TeamMember.fromPerson(Person.builder().name("Max Mustermann").email(userMail).build()));
        expected.setOwnVoting(vote.getOwnVote());
        expected.setRefereesChat(null);
        expected.getTeamChat().add(ChatEntryDTO.fromChatEntry(idea.getTeamChat().iterator().next()));
        Iterator<ChatEntry> teamAndRefChatIterator = idea.getTeamAndRefereesChat().iterator();
        expected.getTeamAndRefereesChat().add(ChatEntryDTO.fromChatEntry(teamAndRefChatIterator.next()));
        expected.getTeamAndRefereesChat().add(ChatEntryDTO.fromChatEntry(teamAndRefChatIterator.next()));
        expected.setInvitationLinkTeammember("https://diamant.se-tools.de/invitation/token1");

        this.mockMvc.perform(
                get("/api/challenge/" + challengeId + "/idea/" + ideaId)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)));
        Mockito.verify(denyAccessInterceptor, Mockito.only()).interceptViewIdea(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.any(Authentication.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewVotes(Mockito.eq(challenge), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewTeamChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewTeamAndRefereeChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewRefereeChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class));
    }

    @Test
    public void getIdeaDetailsNotLoggedInFromIdeaWithChatMessages() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";
        String teamName = "hejaBVBTeam";
        String shortDescription = "Kurzbeschreeeeibung";

        Person challengeCaller = Person.builder().email("someOtherCaller@example.com").build();
        Challenge challenge = Challenge.builder().caller(challengeCaller).build();
        Mockito.when(challengeService.getChallengeDetails(Mockito.eq(challengeId))).thenReturn(challenge);
        Mockito.when(jwtService.generateInvitationToken(Mockito.any(InvitationInformation.class))).thenReturn("token1");

        Idea idea = new Idea(
                ideaId,
                challengeId,
                Person.builder().email(userMail).build(),
                teamName,
                shortDescription,
                Collections.emptyList(),
                IdeaState.READY_FOR_VOTE,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                0
        );
        idea.getTeamChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Meier").email(userMail).build()).text("TeamChatEintrag").build());
        idea.getTeamAndRefereesChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Meier1").email(userMail).build()).text("GutachterUndTeamEintrag 1").build());
        idea.getTeamAndRefereesChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Meier2").email("andere@Mail.de").build()).text("GutachterUndTeamEintrag 2").build());
        idea.getRefereesChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Meier2").email("andere@Mail.de").build()).text("NurGutachterEintrag 2").build());


        AverageAndOwnVoteForIdea vote = AverageAndOwnVoteForIdea.builder().ideaId(ideaId).averageVote(13.33).ownVote(3).build();

        Mockito.when(ideaService.findById(Mockito.eq(ideaId))).thenReturn(idea);
        Mockito.when(ideaService.getPermissions(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail))).thenReturn(Arrays.asList(IdeaPermissions.EDIT, IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE));
        Mockito.when(voteService.getAvgAndOwnVoteForIdea(Mockito.eq(ideaId), Mockito.eq(KeycloakUtils.getUserOrDefault(null)))).thenReturn(vote);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewVotes(Mockito.eq(challenge), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewTeamChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(KeycloakUtils.getUserOrDefault(null)), Mockito.any(StringBuilder.class))).thenReturn(false);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewTeamAndRefereeChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(KeycloakUtils.getUserOrDefault(null)), Mockito.any(StringBuilder.class))).thenReturn(false);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewRefereeChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(KeycloakUtils.getUserOrDefault(null)), Mockito.any(StringBuilder.class))).thenReturn(false);

        String attachmentId1 = "5c6c25fe3accaf414c792f49";
        String fileName1 = "Datei 1";
        String attachmentBusinessId1 = "2";
        String contentType1 = "image/png";
        String attachmentId2 = "5c617f7c0f4a6437b4f769fc";
        String fileName2 = "Datei 2";
        String attachmentBusinessId2 = "3";
        String contentType2 = "application/pdf";
        GridFSFindIterable attachments = createAttachmentMocks(challengeId, ideaId,
                attachmentId1, attachmentBusinessId1, fileName1, contentType1,
                attachmentId2, attachmentBusinessId2, fileName2, contentType2);
        Mockito.when(attachmentService.findIdeaAttachmentsByIdeaId(Mockito.eq(ideaId))).thenReturn(attachments);


        IdeaDetailsDTO expected = IdeaDetailsDTO.from(idea);
        expected.setCurrentUserPermissions(Collections.emptyList());
        expected.setOwnVoting(vote.getOwnVote());
        expected.setAverageVoting(vote.getAverageVote());
        expected.setTeamChat(null);
        expected.setRefereesChat(null);
        expected.setTeamAndRefereesChat(null);
        expected.setInvitationLinkTeammember(null);

        this.mockMvc.perform(
                get("/api/challenge/" + challengeId + "/idea/" + ideaId)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)));
        Mockito.verify(denyAccessInterceptor, Mockito.only()).interceptViewIdea(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.isNull());
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewVotes(Mockito.eq(challenge), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewVotes(Mockito.eq(challenge), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewTeamChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(KeycloakUtils.getUserOrDefault(null)), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewTeamAndRefereeChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(KeycloakUtils.getUserOrDefault(null)), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewRefereeChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(KeycloakUtils.getUserOrDefault(null)), Mockito.any(StringBuilder.class));
    }

    @Test
    @WithMockUser(username = userMail, roles = {"user"})
    public void getIdeaDetailsWithRefereesChatAndTeamAndReferees() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";
        String teamName = "hejaBVBTeam";
        String shortDescription = "Kurzbeschreeeeibung";

        Person challengeCaller = Person.builder().email("someOtherCaller@example.com").build();
        Challenge challenge = Challenge.builder().caller(challengeCaller).build();
        Mockito.when(challengeService.getChallengeDetails(Mockito.eq(challengeId))).thenReturn(challenge);
        Mockito.when(jwtService.generateInvitationToken(Mockito.any(InvitationInformation.class))).thenReturn("token1");

        CanvasElement textElement = CanvasElement.builder()
                .id("1")
                .type(CanvasElementType.TEXT)
                .position(Position.builder().x(5).y(10).width(2).height(3).build())
                .content("Lorem Ipsum etc :)")
                .build();

        Idea idea = new Idea(
                ideaId,
                challengeId,
                Person.builder().email(userMail).build(),
                teamName,
                shortDescription,
                Collections.singletonList(textElement),
                IdeaState.DRAFT,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                0
        );
        idea.getTeamMember().add(TeamMember.fromPerson(Person.builder().name("Max Mustermann").email(userMail).build()));
        idea.getTeamChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Meier").email(userMail).build()).text("TeamChatEintrag").build());
        idea.getTeamAndRefereesChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Meier1").email(userMail).build()).text("GutachterUndTeamEintrag 1").build());
        idea.getTeamAndRefereesChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Meier2").email("andere@Mail.de").build()).text("GutachterUndTeamEintrag 2").build());
        idea.getRefereesChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Meier2").email("andere@Mail.de").build()).text("NurGutachterEintrag 2").build());

        AverageAndOwnVoteForIdea vote = AverageAndOwnVoteForIdea.builder().ideaId(ideaId).averageVote(13.33).ownVote(3).build();
        String attachmentId1 = "5c6c25fe3accaf414c792f49";
        String fileName1 = "Datei 1";
        String attachmentBusinessId1 = "2";
        String contentType1 = "image/png";
        String attachmentId2 = "5c617f7c0f4a6437b4f769fc";
        String fileName2 = "Datei 2";
        String attachmentBusinessId2 = "3";
        String contentType2 = "application/pdf";
        GridFSFindIterable attachments = createAttachmentMocks(challengeId, ideaId,
                attachmentId1, attachmentBusinessId1, fileName1, contentType1,
                attachmentId2, attachmentBusinessId2, fileName2, contentType2);
        Mockito.when(attachmentService.findIdeaAttachmentsByIdeaId(Mockito.eq(ideaId))).thenReturn(attachments);
        Mockito.when(ideaService.findById(Mockito.eq(ideaId))).thenReturn(idea);
        Mockito.when(ideaService.getPermissions(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail))).thenReturn(Arrays.asList(IdeaPermissions.EDIT, IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE));
        Mockito.when(voteService.getAvgAndOwnVoteForIdea(Mockito.eq(ideaId), Mockito.eq(userMail))).thenReturn(vote);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewTeamChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class))).thenReturn(false);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewTeamAndRefereeChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewRefereeChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class))).thenReturn(true);

        IdeaDetailsDTO expected = IdeaDetailsDTO.from(idea);
        CanvasElementDetailDTO attachment1 = new CanvasElementDetailDTO(CanvasElement.builder()
                .id("1")
                .type(CanvasElementType.TEXT)
                .position(Position.builder().x(5).y(10).width(2).height(3).build())
                .content("Lorem Ipsum etc :)")
                .build());

        expected.setCanvasElements(Collections.singletonList(attachment1));
        expected.setCurrentUserPermissions(Arrays.asList(IdeaPermissions.EDIT, IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE));
        expected.getTeamMember().add(TeamMember.fromPerson(Person.builder().name("Max Mustermann").email(userMail).build()));
        expected.setTeamChat(null);
        expected.getRefereesChat().add(ChatEntryDTO.fromChatEntry(idea.getRefereesChat().iterator().next()));
        Iterator<ChatEntry> teamAndRefChatIterator = idea.getTeamAndRefereesChat().iterator();
        expected.getTeamAndRefereesChat().add(ChatEntryDTO.fromChatEntry(teamAndRefChatIterator.next()));
        expected.getTeamAndRefereesChat().add(ChatEntryDTO.fromChatEntry(teamAndRefChatIterator.next()));
        expected.setOwnVoting(vote.getOwnVote());
        expected.setInvitationLinkTeammember("https://diamant.se-tools.de/invitation/token1");

        this.mockMvc.perform(
                get("/api/challenge/" + challengeId + "/idea/" + ideaId)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)));
        Mockito.verify(denyAccessInterceptor, Mockito.only()).interceptViewIdea(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.any(Authentication.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewVotes(Mockito.eq(challenge), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewTeamChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewTeamAndRefereeChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewRefereeChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class));
    }

    @Test
    @WithMockUser(username = userMail, roles = {"user"})
    public void getIdeaDetailsAlwaysShowsTeamMemberIfUserIsTeamMemberHimself() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";
        String teamName = "hejaBVBTeam";
        String shortDescription = "Kurzbeschreeeeibung";

        Person challengeCaller = Person.builder().email("someOtherCaller@example.com").build();
        Challenge challenge = Challenge.builder().caller(challengeCaller).build();
        Mockito.when(challengeService.getChallengeDetails(Mockito.eq(challengeId))).thenReturn(challenge);
        Mockito.when(jwtService.generateInvitationToken(Mockito.any(InvitationInformation.class))).thenReturn("token1");

        CanvasElement textElement = CanvasElement.builder()
                .id("1")
                .type(CanvasElementType.TEXT)
                .position(Position.builder().x(5).y(10).width(2).height(3).build())
                .content("Lorem Ipsum etc :)")
                .build();

        Idea idea = new Idea(
                ideaId,
                challengeId,
                Person.builder().email(userMail).build(),
                teamName,
                shortDescription,
                Collections.singletonList(textElement),
                IdeaState.DRAFT,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                0
        );
        idea.getTeamMember().add(TeamMember.fromPerson(Person.builder().name("Max Mustermann").email(userMail).build()));
        idea.getTeamMember().add(new TeamMember("Paula Paulason", "Test@Mail.de", true));

        AverageAndOwnVoteForIdea vote = AverageAndOwnVoteForIdea.builder().ideaId(ideaId).averageVote(13.33).ownVote(3).build();
        String attachmentId1 = "5c6c25fe3accaf414c792f49";
        String fileName1 = "Datei 1";
        String attachmentBusinessId1 = "2";
        String contentType1 = "image/png";
        String attachmentId2 = "5c617f7c0f4a6437b4f769fc";
        String fileName2 = "Datei 2";
        String attachmentBusinessId2 = "3";
        String contentType2 = "application/pdf";
        GridFSFindIterable attachments = createAttachmentMocks(challengeId, ideaId,
                attachmentId1, attachmentBusinessId1, fileName1, contentType1,
                attachmentId2, attachmentBusinessId2, fileName2, contentType2);
        Mockito.when(attachmentService.findIdeaAttachmentsByIdeaId(Mockito.eq(ideaId))).thenReturn(attachments);
        Mockito.when(ideaService.findById(Mockito.eq(ideaId))).thenReturn(idea);
        Mockito.when(ideaService.getPermissions(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail))).thenReturn(Arrays.asList(IdeaPermissions.EDIT, IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE));
        Mockito.when(voteService.getAvgAndOwnVoteForIdea(Mockito.eq(ideaId), Mockito.eq(userMail))).thenReturn(vote);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewTeamChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class))).thenReturn(false);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewTeamAndRefereeChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewRefereeChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewAllTeamMember(Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class))).thenReturn(true);

        IdeaDetailsDTO expected = IdeaDetailsDTO.from(idea);
        CanvasElementDetailDTO attachment1 = new CanvasElementDetailDTO(CanvasElement.builder()
                .id("1")
                .type(CanvasElementType.TEXT)
                .position(Position.builder().x(5).y(10).width(2).height(3).build())
                .content("Lorem Ipsum etc :)")
                .build());

        expected.setCanvasElements(Collections.singletonList(attachment1));
        expected.setCurrentUserPermissions(Arrays.asList(IdeaPermissions.EDIT, IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE));
        expected.getTeamMember().add(TeamMember.fromPerson(Person.builder().name("Max Mustermann").email(userMail).build()));
        expected.getTeamMember().add(new TeamMember("Paula Paulason", "Test@Mail.de", true));
        expected.setTeamChat(null);
        expected.setOwnVoting(vote.getOwnVote());
        expected.setInvitationLinkTeammember("https://diamant.se-tools.de/invitation/token1");

        this.mockMvc.perform(
                get("/api/challenge/" + challengeId + "/idea/" + ideaId)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)));
        Mockito.verify(denyAccessInterceptor, Mockito.only()).interceptViewIdea(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.any(Authentication.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewVotes(Mockito.eq(challenge), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewTeamChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewTeamAndRefereeChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewRefereeChat(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewAllTeamMember(Mockito.eq(idea), Mockito.eq(userMail), Mockito.any(StringBuilder.class));
    }

    @Test
    public void getIdeaDetailsWithoutUser() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";
        String teamName = "hejaBVBTeam";
        String shortDescription = "Kurzbeschreeeeibung";

        Person challengeCaller = Person.builder().email("someOtherCaller@example.com").build();
        Challenge challenge = Challenge.builder().caller(challengeCaller).build();
        challenge.getReferees().add(challengeCaller);
        Mockito.when(challengeService.getChallengeDetails(Mockito.eq(challengeId))).thenReturn(challenge);

        Idea idea = new Idea(
                ideaId,
                challengeId,
                Person.builder().email(userMail).build(),
                teamName,
                shortDescription,
                Collections.emptyList(),
                IdeaState.READY_FOR_VOTE,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                0
        );
        idea.getTeamMember().add(TeamMember.fromPerson(idea.getCreator()));
        idea.getTeamMember().add(new TeamMember("Christopher Christopher", "Test@Mail.de", true));

        AverageAndOwnVoteForIdea vote = AverageAndOwnVoteForIdea.builder().ideaId(ideaId).averageVote(13.33).ownVote(3).build();

        Mockito.when(ideaService.findById(Mockito.eq(ideaId))).thenReturn(idea);
        Mockito.when(voteService.getAvgAndOwnVoteForIdea(Mockito.eq(ideaId), Mockito.eq(KeycloakUtils.getUserOrDefault(null)))).thenReturn(vote);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewVotes(Mockito.eq(challenge), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewAllTeamMember(Mockito.eq(idea), Mockito.eq(KeycloakUtils.getUserOrDefault(null)), Mockito.any(StringBuilder.class))).thenReturn(false);

        String attachmentId1 = "5c6c25fe3accaf414c792f49";
        String fileName1 = "Datei 1";
        String attachmentBusinessId1 = "2";
        String contentType1 = "image/png";
        String attachmentId2 = "5c617f7c0f4a6437b4f769fc";
        String fileName2 = "Datei 2";
        String attachmentBusinessId2 = "3";
        String contentType2 = "application/pdf";
        GridFSFindIterable attachments = createAttachmentMocks(challengeId, ideaId,
                attachmentId1, attachmentBusinessId1, fileName1, contentType1,
                attachmentId2, attachmentBusinessId2, fileName2, contentType2);
        Mockito.when(attachmentService.findIdeaAttachmentsByIdeaId(Mockito.eq(ideaId))).thenReturn(attachments);


        IdeaDetailsDTO expected = IdeaDetailsDTO.from(idea);
        expected.setCurrentUserPermissions(Collections.emptyList());
        expected.setOwnVoting(vote.getOwnVote());
        expected.setAverageVoting(vote.getAverageVote());
        expected.setTeamChat(null);
        expected.setRefereesChat(null);
        expected.setTeamAndRefereesChat(null);
        expected.getTeamMember().add(TeamMember.fromPerson(idea.getCreator()));
        expected.getTeamMember().add(new TeamMember(TeamMember.INCOGNITO_USER_NAME, "", true));

        this.mockMvc.perform(
                get("/api/challenge/" + challengeId + "/idea/" + ideaId)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)));
        Mockito.verify(denyAccessInterceptor, Mockito.only()).interceptViewIdea(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.isNull());
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewVotes(Mockito.eq(challenge), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewAllTeamMember(Mockito.eq(idea), Mockito.eq(KeycloakUtils.getUserOrDefault(null)), Mockito.any(StringBuilder.class));
    }

    @Test
    @WithMockUser(username = userMail, roles = {"user"})
    public void saveVote() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";

        Voting postArguments = new Voting(4);

        this.mockMvc.perform(
                post("/api/challenge/" + challengeId + "/idea/" + ideaId + "/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(postArguments)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(ideaId));
        Mockito.verify(denyAccessInterceptor, Mockito.times(1)).interceptVote(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.any(Authentication.class));
        Mockito.verify(voteService, Mockito.times(1)).saveVote(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.eq(userMail), Mockito.eq(postArguments.getVote()));
    }

    @Test
    @WithMockUser(username = userMail, roles = {"user"})
    public void updateGeneralInformation() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";


        GeneralInformationDTO newInformation = new GeneralInformationDTO(
                "New TeamName",
                "Beschreibung.. :)"
        );
        UpdateIdeaFieldDTO updateContent = new UpdateIdeaFieldDTO(
                UpdateIdeaFieldDTO.Field.GENERAL_INFORMATION,
                newInformation
        );

        Mockito.doNothing().when(denyAccessInterceptor).interceptEditIdea(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.any(Authentication.class));
        Mockito.doNothing().when(ideaService).updateGeneralInformation(Mockito.eq(ideaId), Mockito.eq(newInformation));

        this.mockMvc.perform(
                put("/api/challenge/" + challengeId + "/idea/" + ideaId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateContent))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(ideaId));

        Mockito.verify(ideaService).updateGeneralInformation(Mockito.eq(ideaId), Mockito.eq(newInformation));
        Mockito.verify(denyAccessInterceptor).interceptEditIdea(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.any(Authentication.class));
    }

    @Test
    @WithMockUser(username = userMail, roles = {"user"})
    public void updateCanvasElements() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";


        UpdateCanvasElementDTO newInformation = new UpdateCanvasElementDTO(
                Collections.singletonList(CanvasElement.builder().type(CanvasElementType.TEXT).id("2").position(Position.builder().build()).content("Text").build())
        );
        UpdateIdeaFieldDTO updateContent = new UpdateIdeaFieldDTO(
                UpdateIdeaFieldDTO.Field.CANVAS,
                newInformation
        );

        Mockito.doNothing().when(denyAccessInterceptor).interceptEditIdea(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.any(Authentication.class));
        Mockito.doNothing().when(ideaService).updateCanvasElements(Mockito.eq(ideaId), Mockito.eq(newInformation));

        this.mockMvc.perform(
                put("/api/challenge/" + challengeId + "/idea/" + ideaId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateContent))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(ideaId));

        Mockito.verify(ideaService).updateCanvasElements(Mockito.eq(ideaId), Mockito.eq(newInformation));
        Mockito.verify(denyAccessInterceptor).interceptEditIdea(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.any(Authentication.class));
    }

    @Test
    @WithMockUser(username = userMail, roles = {"user"})
    public void updateTeamMember() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";


        UpdateIdeaTeamDTO newInformation = new UpdateIdeaTeamDTO(
                Arrays.asList(
                    TeamMember.fromPerson(Person.builder().email("test@exmaple.com").name("neuesMitglied").build(), false),
                    TeamMember.fromPerson(Person.builder().email("test2@exmaple.com").name("neuesMitglied2").build(), false),
                    new TeamMember("hiddenPerson name", "hidden@person.example.com", true)
                )
        );
        UpdateIdeaFieldDTO updateContent = new UpdateIdeaFieldDTO(
                UpdateIdeaFieldDTO.Field.TEAM_MEMBER,
                newInformation
        );
        List<TeamMember> expectedToBeSaved = Arrays.asList(
                TeamMember.fromPerson(Person.builder().email("test@exmaple.com").name("neuesMitglied").build(), false),
                TeamMember.fromPerson(Person.builder().email("test2@exmaple.com").name("neuesMitglied2").build(), false),
                new TeamMember("hiddenPerson name", "hidden@person.example.com", true),
                TeamMember.fromPerson(Person.builder().email(userMail).name("caller@uni-due.de caller@uni-due.de").build())
        );
        Challenge c = Challenge.builder().id(challengeId).build();
        Mockito.when(challengeService.getChallengeDetails(Mockito.eq(challengeId))).thenReturn(c);
        Mockito.doNothing().when(denyAccessInterceptor).interceptEditIdea(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.any(Authentication.class));
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewChallenge(Mockito.eq(c),Mockito.eq("test@exmaple.com"), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewChallenge(Mockito.eq(c),Mockito.eq("test2@exmaple.com"), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewChallenge(Mockito.eq(c),Mockito.eq("hidden@person.example.com"), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionServiceFacade.checkIsAllowedToViewChallenge(Mockito.eq(c),Mockito.eq(userMail), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(keycloakUserService.checkIfUserExists(Mockito.eq("test@exmaple.com"))).thenReturn(true);
        Mockito.when(keycloakUserService.checkIfUserExists(Mockito.eq("test2@exmaple.com"))).thenReturn(true);
        Mockito.when(keycloakUserService.checkIfUserExists(Mockito.eq("hidden@person.example.com"))).thenReturn(true);
        Mockito.when(keycloakUserService.checkIfUserExists(Mockito.eq(userMail))).thenReturn(true);
        Mockito.doNothing().when(ideaService).updateTeamMember(Mockito.eq(ideaId), Mockito.eq(expectedToBeSaved));

        this.mockMvc.perform(
                put("/api/challenge/" + challengeId + "/idea/" + ideaId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateContent))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(ideaId));

        Mockito.verify(ideaService).updateTeamMember(Mockito.eq(ideaId), Mockito.eq(expectedToBeSaved));
        Mockito.verify(denyAccessInterceptor).interceptEditIdea(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.any(Authentication.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(c),Mockito.eq("test@exmaple.com"), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(c),Mockito.eq("test2@exmaple.com"), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionServiceFacade, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(c),Mockito.eq(userMail), Mockito.any(StringBuilder.class));
         Mockito.verify(keycloakUserService, Mockito.times(1)).checkIfUserExists(Mockito.eq("test@exmaple.com"));
         Mockito.verify(keycloakUserService, Mockito.times(1)).checkIfUserExists(Mockito.eq("test2@exmaple.com"));
         Mockito.verify(keycloakUserService, Mockito.times(1)).checkIfUserExists(Mockito.eq("hidden@person.example.com"));
         Mockito.verify(keycloakUserService, Mockito.times(1)).checkIfUserExists(Mockito.eq(userMail));
    }

    @Test
    @WithMockUser(username = userMail, roles = {"user"})
    public void updateWinningPlaceShouldFailIfNotCaller() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";

        UpdateIdeaFieldDTO updateContent = new UpdateIdeaFieldDTO(
                UpdateIdeaFieldDTO.Field.WINNING_PLACE,
                UpdateWinningPlace.WinningPlace.WINNER_1
        );

        Mockito.doThrow(new AccessDeniedException("nope..")).when(denyAccessInterceptor).interceptNominateAsWinner(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.doNothing().when(ideaService).updateWinningPlace(Mockito.eq(ideaId), Mockito.eq(UpdateWinningPlace.WinningPlace.WINNER_1));

        this.mockMvc.perform(
                put("/api/challenge/" + challengeId + "/idea/" + ideaId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateContent))
        )
                .andDo(print())
                .andExpect(status().isForbidden());

        Mockito.verify(ideaService, Mockito.never()).updateWinningPlace(Mockito.eq(ideaId), Mockito.eq(UpdateWinningPlace.WinningPlace.WINNER_1));
        Mockito.verify(denyAccessInterceptor).interceptNominateAsWinner(Mockito.eq(challengeId), Mockito.any(Authentication.class));
    }

    @Test
    @WithMockUser(username = userMail, roles = {"user"})
    public void updateWinningAsCaller() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";

        UpdateIdeaFieldDTO updateContent = new UpdateIdeaFieldDTO(
                UpdateIdeaFieldDTO.Field.WINNING_PLACE,
                UpdateWinningPlace.WinningPlace.WINNER_1
        );

        Mockito.doNothing().when(denyAccessInterceptor).interceptNominateAsWinner(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.doNothing().when(ideaService).updateWinningPlace(Mockito.eq(ideaId), Mockito.eq(UpdateWinningPlace.WinningPlace.WINNER_1));

        this.mockMvc.perform(
                put("/api/challenge/" + challengeId + "/idea/" + ideaId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateContent))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(ideaId));

        Mockito.verify(ideaService, Mockito.times(1)).updateWinningPlace(Mockito.eq(ideaId), Mockito.eq(UpdateWinningPlace.WinningPlace.WINNER_1));
        Mockito.verify(denyAccessInterceptor).interceptNominateAsWinner(Mockito.eq(challengeId), Mockito.any(Authentication.class));
    }


    private GridFSFindIterable createAttachmentMocks(String challengeId, String ideaId,
                                                     String attachmentId1, String attachmentBusinessId1, String fileName1, String contentType1,
                                                     String attachmentId2, String attachmentBusinessId2, String fileName2, String contentType2) {
        GridFSFindIterable result = Mockito.mock(GridFSFindIterable.class);
        MongoCursor<GridFSFile> cursor = Mockito.mock(MongoCursor.class);
        Mockito.when(cursor.hasNext()).thenReturn(true, true, false);

        Mockito.when(cursor.next())
                .thenReturn(new GridFSFile(new BsonObjectId(new ObjectId(attachmentId1)), fileName1, 0, 0, new Date(), "hash", createMetaDataDocument(challengeId, ideaId, attachmentBusinessId1, fileName1, contentType1)))
                .thenReturn(new GridFSFile(new BsonObjectId(new ObjectId(attachmentId2)), fileName2, 0, 0, new Date(), "hash", createMetaDataDocument(challengeId, ideaId, attachmentBusinessId2, fileName2, contentType2)))
                .thenReturn(null);

        Mockito.when(result.iterator()).thenReturn(cursor);
        return result;
    }

    private Document createMetaDataDocument(String challengeId, String ideaId, String attachmentBusinessId, String fileName, String contentType) {
        Document metaData = new Document();
        metaData.put(AttachmentService.META_DATA_FIELD_CHALLENGE_ID, challengeId);
        metaData.put(AttachmentService.META_DATA_FIELD_IDEA_ID, ideaId);
        metaData.put(AttachmentService.META_DATA_FIELD_ATTACHMENT_BUSINESS_ID, attachmentBusinessId);
        metaData.put(AttachmentService.META_DATA_FIELD_FILE_NAME, fileName);
        metaData.put(AttachmentService.META_DATA_FIELD_CONTENT_TYPE, contentType);
        metaData.put(AttachmentService.META_DATA_FIELD_TYPE, AttachmentService.AttachmentType.IDEA_ATTACHMENT.toString());
        return metaData;
    }
}
