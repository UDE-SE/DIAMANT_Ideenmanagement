package de.unidue.se.diamant.backend.backendservice.controller;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import de.unidue.se.diamant.backend.backendservice.dto.challenge.*;
import de.unidue.se.diamant.backend.backendservice.dto.challenge.parts.GeneralInformation;
import de.unidue.se.diamant.backend.backendservice.dto.challenge.parts.MileStones;
import de.unidue.se.diamant.backend.backendservice.dto.challenge.update.UpdateAttachments;
import de.unidue.se.diamant.backend.backendservice.dto.challenge.update.UpdateIdeaFieldDTO;
import de.unidue.se.diamant.backend.backendservice.dto.challenge.update.UpdatePersons;
import de.unidue.se.diamant.backend.backendservice.dto.idea.IdeaPermissions;
import de.unidue.se.diamant.backend.backendservice.service.AttachmentService;
import de.unidue.se.diamant.backend.backendservice.service.invitation.InvitationInformation;
import de.unidue.se.diamant.backend.backendservice.service.invitation.JWTService;
import de.unidue.se.diamant.backend.backendservice.service.news.NewsService;
import de.unidue.se.diamant.backend.backendservice.service.challenge.ChallengeService;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Visibility;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import de.unidue.se.diamant.backend.backendservice.service.idea.IdeaService;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.Idea;
import de.unidue.se.diamant.backend.backendservice.service.keycloak.KeycloakUserService;
import de.unidue.se.diamant.backend.backendservice.service.keycloak.KeycloakUtils;
import de.unidue.se.diamant.backend.backendservice.service.news.domain.NewsItem;
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
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "app.server.base.url=https://diamant.se-tools.de")
public class ChallengeControllerTest extends RestDocTest {

    @MockBean
    private ChallengeService challengeService;

    @MockBean
    private AttachmentService attachmentService;

    @MockBean
    private IdeaService ideaService;

    @MockBean
    private VoteService voteService;

    @MockBean
    private NewsService newsService;

    @MockBean
    private PermissionServiceFacade permissionService;

    @MockBean
    private DenyAccessInterceptor denyAccessInterceptor;

    @MockBean
    private KeycloakUserService keycloakUserService;

    @MockBean
    private JWTService jwtService;

    private static Date parseDate(String date) throws ParseException {
        return (new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")).parse(date);
    }


    @Test
    public void getChallengePreview() throws Exception {
        List<Challenge> challengeList = new LinkedList<>();
        challengeList.add(Challenge.builder().id("Id1").title("Title1").shortDescription("Kurzbeschreibung").visibility(Visibility.INTERNAL).submissionStart(parseDate("03.09.2019 10:10:10")).build());
        challengeList.add(Challenge.builder().id("Id2").title("2").shortDescription("Kurzbeschreibung").visibility(Visibility.INTERNAL).submissionStart(parseDate("03.09.2019 10:10:10")).build());
        challengeList.add(Challenge.builder().id("Id3").title("3").shortDescription("Kurzbes3chreibung").visibility(Visibility.INTERNAL).submissionStart(parseDate("03.09.2019 10:10:10")).build());
        Mockito.when(challengeService.getChallengePreview()).thenReturn(challengeList);
        Mockito.when(permissionService.checkIsAllowedToViewChallenge(Mockito.any(Challenge.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(true, true, false);

        this.mockMvc.perform(
                get("/api/challenge/preview")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(
                        challengeList.stream()
                                .filter(challenge -> !challenge.getTitle().equals("3")) // Hierdurch wird auch geschaut, ob die prüfungen des PermissionServices berücksichtigt werden (das letzte Element sollte nicht zurückgegeben werden)
                                .map(ChallengePreviewDTO::fromChallenge).collect(Collectors.toList()))));

    }

    @Test
    @WithMockUser(username = "callEr@uni-due.de", roles = {"caller"})
    public void saveChallenge() throws Exception {
        String postArg = "{" +
                "\"title\": \"Titel\"," +
                "\"shortDescription\" : \"Kurzbeschreibung\"," +
                "\"description\" : \"Lange ausführliche Beschreibung\"," +
                "\"visibility\" : \"INTERNAL\"," +
                "\"submissionStart\" : \""+parseDate("17.08.2019 10:10:00").getTime() +"\","+
                "\"reviewStart\" : \""+parseDate("18.08.2019 11:10:00").getTime() +"\","+
                "\"refactoringStart\" : null,"+
                "\"votingStart\" : \""+parseDate("19.08.2019 12:10:00").getTime() +"\","+
                "\"implementationStart\" : \""+parseDate("20.08.2019 10:11:00").getTime() +"\","+
                "\"challengeEnd\" : \""+parseDate("21.08.2019 10:12:00").getTime() +"\","+
                "\"awards\" : \"Der Gewinner bekommt: *Trommelwirbel* nichts\","+
                "\"referees\": [" +
                "{\"name\": \"Erster Gutachter\", \"email\" : \"Gutachter1@uni-due.de\"}," +
                "{\"name\": \"Zweiter gutachter\", \"email\" : \"Gutachter2@uni-due.de\"}" +
                "]," +
                "\"invitedUsers\": [" +
                "{\"name\": \"eingeladener Nutzer 1\", \"email\" : \"1@example.com\"}" +
                "]"+
                "}";

        ArgumentCaptor<Challenge> saveCaptor = ArgumentCaptor.forClass(Challenge.class);


        String saved_challenge_id = "5c6c25fe3accaf414c792f49";
        Person currentUserPerson = Person.builder().name("callEr@uni-due.de callEr@uni-due.de").email("callEr@uni-due.de").build();
        Mockito.when(challengeService.saveChallenge(saveCaptor.capture(), Mockito.eq(currentUserPerson))).thenReturn(saved_challenge_id);
        Mockito.when(keycloakUserService.checkIfUserExists("Gutachter1@uni-due.de".toLowerCase())).thenReturn(true);
        Mockito.when(keycloakUserService.checkIfUserExists("Gutachter2@uni-due.de".toLowerCase())).thenReturn(true);

        this.mockMvc.perform(
                post("/api/challenge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postArg)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(saved_challenge_id));

        Mockito.verify(challengeService).saveChallenge(saveCaptor.capture(), Mockito.eq(currentUserPerson));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Assertions.assertThat(simpleDateFormat.format(saveCaptor.getValue().getSubmissionStart())).isEqualTo("17.08.2019 10:10:00");
        Assertions.assertThat(simpleDateFormat.format(saveCaptor.getValue().getReviewStart())).isEqualTo("18.08.2019 11:10:00");
        Assertions.assertThat(saveCaptor.getValue().getRefactoringStart()).isNull();
        Assertions.assertThat(simpleDateFormat.format(saveCaptor.getValue().getVotingStart())).isEqualTo("19.08.2019 12:10:00");
        Assertions.assertThat(simpleDateFormat.format(saveCaptor.getValue().getImplementationStart())).isEqualTo("20.08.2019 10:11:00");
        Assertions.assertThat(simpleDateFormat.format(saveCaptor.getValue().getChallengeEnd())).isEqualTo("21.08.2019 10:12:00");
        Assertions.assertThat(saveCaptor.getValue().getInvitedUsers()).isNull();
        Mockito.verify(keycloakUserService, Mockito.times(1)).checkIfUserExists("Gutachter1@uni-due.de".toLowerCase());
        Mockito.verify(keycloakUserService, Mockito.times(1)).checkIfUserExists("Gutachter2@uni-due.de".toLowerCase());
        Mockito.verify(keycloakUserService, Mockito.never()).checkIfUserExists("1@example.com".toLowerCase());
    }

    @Test
    @WithMockUser(username = "callEr@uni-due.de", roles = {"caller"})
    public void saveChallengeWithNullMileStone() throws Exception {
        String postArg = "{" +
                "\"title\": \"Titel\"," +
                "\"shortDescription\" : \"Kurzbeschreibung\"," +
                "\"description\" : \"Lange ausführliche Beschreibung\"," +
                "\"visibility\" : \"INTERNAL\"," +
                "\"submissionStart\" : \""+parseDate("17.08.2019 10:10:00").getTime() +"\","+
                "\"reviewStart\" : \""+parseDate("18.08.2019 11:10:00").getTime() +"\","+
                "\"refactoringStart\" : null,"+
                "\"votingStart\" : null,"+
                "\"implementationStart\" : \""+parseDate("20.08.2019 10:11:00").getTime() +"\","+
                "\"challengeEnd\" : \""+parseDate("21.08.2019 10:12:00").getTime() +"\","+
                "\"awards\" : \"Der Gewinner bekommt: *Trommelwirbel* nichts\","+
                "\"referees\": [" +
                "{\"name\": \"Erster Gutachter\", \"email\" : \"Gutachter1@uni-due.de\"}," +
                "{\"name\": \"Zweiter gutachter\", \"email\" : \"Gutachter2@uni-due.de\"}" +
                "]," +
                "\"invitedUsers\": [" +
                "{\"name\": \"eingeladener Nutzer 1\", \"email\" : \"1@example.com\"}" +
                "]"+
                "}";

        ArgumentCaptor<Challenge> saveCaptor = ArgumentCaptor.forClass(Challenge.class);


        String saved_challenge_id = "5c6c25fe3accaf414c792f49";
        Person currentUserPerson = Person.builder().name("callEr@uni-due.de callEr@uni-due.de").email("callEr@uni-due.de").build();
        Mockito.when(challengeService.saveChallenge(saveCaptor.capture(), Mockito.eq(currentUserPerson))).thenReturn(saved_challenge_id);
        Mockito.when(keycloakUserService.checkIfUserExists("Gutachter1@uni-due.de".toLowerCase())).thenReturn(true);
        Mockito.when(keycloakUserService.checkIfUserExists("Gutachter2@uni-due.de".toLowerCase())).thenReturn(true);

        this.mockMvc.perform(
                post("/api/challenge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postArg)
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "callEr@uni-due.de", roles = {"caller"})
    public void saveChallengeWithInvitedUsers() throws Exception {
        String postArg = "{" +
                "\"title\": \"Titel\"," +
                "\"shortDescription\" : \"Kurzbeschreibung\"," +
                "\"description\" : \"Lange ausführliche Beschreibung\"," +
                "\"visibility\" : \"INVITE\"," +
                "\"submissionStart\" : \""+parseDate("17.08.2019 10:10:00").getTime() +"\","+
                "\"reviewStart\" : \""+parseDate("18.08.2019 11:10:00").getTime() +"\","+
                "\"refactoringStart\" : null,"+
                "\"votingStart\" : \""+parseDate("19.08.2019 12:10:00").getTime() +"\","+
                "\"implementationStart\" : \""+parseDate("20.08.2019 10:11:00").getTime() +"\","+
                "\"challengeEnd\" : \""+parseDate("21.08.2019 10:12:00").getTime() +"\","+
                "\"awards\" : \"Der Gewinner bekommt: *Trommelwirbel* nichts\","+
                "\"referees\": [" +
                    "{\"name\": \"Erster Gutachter\", \"email\" : \"Gutachter1@uni-due.de\"}," +
                    "{\"name\": \"Zweiter gutachter\", \"email\" : \"Gutachter2@uni-due.de\"}" +
                "]," +
                "\"invitedUsers\": [" +
                    "{\"name\": \"eingeladener Nutzer 1\", \"email\" : \"1@example.com\"}" +
                "]"+
            "}";

        ArgumentCaptor<Challenge> saveCaptor = ArgumentCaptor.forClass(Challenge.class);


        String saved_challenge_id = "5c6c25fe3accaf414c792f49";
        Person currentUserPerson = Person.builder().name("callEr@uni-due.de callEr@uni-due.de").email("callEr@uni-due.de").build();
        Mockito.when(challengeService.saveChallenge(saveCaptor.capture(), Mockito.eq(currentUserPerson))).thenReturn(saved_challenge_id);
        Mockito.when(keycloakUserService.checkIfUserExists("Gutachter1@uni-due.de".toLowerCase())).thenReturn(true);
        Mockito.when(keycloakUserService.checkIfUserExists("Gutachter2@uni-due.de".toLowerCase())).thenReturn(true);
        Mockito.when(keycloakUserService.checkIfUserExists("1@example.com".toLowerCase())).thenReturn(true);

        this.mockMvc.perform(
                post("/api/challenge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postArg)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(saved_challenge_id));

        Mockito.verify(challengeService).saveChallenge(saveCaptor.capture(), Mockito.eq(currentUserPerson));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Assertions.assertThat(simpleDateFormat.format(saveCaptor.getValue().getSubmissionStart())).isEqualTo("17.08.2019 10:10:00");
        Assertions.assertThat(simpleDateFormat.format(saveCaptor.getValue().getReviewStart())).isEqualTo("18.08.2019 11:10:00");
        Assertions.assertThat(saveCaptor.getValue().getRefactoringStart()).isNull();
        Assertions.assertThat(simpleDateFormat.format(saveCaptor.getValue().getVotingStart())).isEqualTo("19.08.2019 12:10:00");
        Assertions.assertThat(simpleDateFormat.format(saveCaptor.getValue().getImplementationStart())).isEqualTo("20.08.2019 10:11:00");
        Assertions.assertThat(simpleDateFormat.format(saveCaptor.getValue().getChallengeEnd())).isEqualTo("21.08.2019 10:12:00");
        Assertions.assertThat(saveCaptor.getValue().getInvitedUsers()).contains(Person.builder().name("eingeladener Nutzer 1").email("1@example.com").build());
        Mockito.verify(keycloakUserService, Mockito.times(1)).checkIfUserExists("Gutachter1@uni-due.de".toLowerCase());
        Mockito.verify(keycloakUserService, Mockito.times(1)).checkIfUserExists("Gutachter2@uni-due.de".toLowerCase());
        Mockito.verify(keycloakUserService, Mockito.times(1)).checkIfUserExists("1@example.com".toLowerCase());
    }

    @Test
    @WithMockUser(username = "caller2@uni-due.de", roles = {"caller"})
    public void getChallengeDetails() throws Exception {
        String challengeId = "5c6c25fe3accaf414c792f49";
        Challenge challenge = new Challenge();
        challenge.setId(challengeId);
        challenge.setShortDescription("Kurzbeschreibung");
        challenge.setDescription("Lange Beschreibung");
        challenge.setSubmissionStart(parseDate("05.09.2019 10:10:00"));
        challenge.setReviewStart(parseDate("06.09.2019 10:10:00"));
        challenge.setRefactoringStart(parseDate("07.09.2019 10:10:00"));
        challenge.setVotingStart(parseDate("08.09.2019 10:10:00"));
        challenge.setImplementationStart(parseDate("04.08.2019 10:10:00"));
        challenge.setChallengeEnd(parseDate("09.09.2019 10:10:00"));
        challenge.setVisibility(Visibility.OPEN);
        challenge.setAwards("Preise.. :) Cooles Zeug..");
        challenge.setCaller(Person.builder().email("callEr@uni-due.de").build());
        challenge.getReferees().add(Person.builder().name("Caller Callerson").email("caller@unidue.de").build());

        List<NewsItem> newsItems = Arrays.asList(
                NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getChallengeEnd()).content("Ende").build(),
                NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getImplementationStart()).content("Siegerbestimmung").build(),
                NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getRefactoringStart()).content("Überarbeitungsphase").build(),
                NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getVotingStart()).content("Abstimmphase").build(),
                NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getReviewStart()).content("Einreichungsfrist").build(),
                NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getSubmissionStart()).content("Start").build());

        Mockito.when(challengeService.getChallengeDetails(Mockito.eq(challengeId))).thenReturn(challenge);
        String attachmentId1 = "5c6c25fe3accaf414c792f49";
        String attachmentName1 = "Datei 1";
        String attachmentId2 = "5c617f7c0f4a6437b4f769fc";
        String attachmentName2 = "Datei 2";
        GridFSFindIterable attachments = findAttachmentMock(attachmentId1, attachmentName1, attachmentId2, attachmentName2);
        Mockito.when(attachmentService.getChallengeAttachmentsByChallengeId(Mockito.eq(challengeId))).thenReturn(attachments);

        Idea firstIdea = Idea.builder().id("First Idea").creator(Person.builder().email("caller2@uni-due.de").build()).shortDescription("KurzBeschreibung Idee 1").teamName("Heja BVB").build();
        Idea secondIdea = Idea.builder().id("Zweite Idea").creator(Person.builder().email("user2").build()).shortDescription("KurzBeschreibung Idee 2").teamName("Heja BVB2").build();
        Idea thirdIdea = Idea.builder().id("Dritte Idea").creator(Person.builder().email("user3").build()).shortDescription("KurzBeschreibung Idee 3").teamName("Heja BVB3").build();
        Mockito.when(ideaService.findByChallengeId(Mockito.eq(challengeId))).thenReturn(
                Arrays.asList(
                        firstIdea,
                        secondIdea,
                        thirdIdea
                )
        );
        String currentUser = "caller2@uni-due.de";

        Mockito.when(ideaService.getPermissions(Mockito.eq(challenge), Mockito.eq(firstIdea), Mockito.eq("caller2@uni-due.de"))).thenReturn(Arrays.asList(IdeaPermissions.EDIT, IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE));
        Mockito.when(ideaService.getPermissions(Mockito.eq(challenge), Mockito.eq(secondIdea), Mockito.eq("caller2@uni-due.de"))).thenReturn(Collections.singletonList(IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE));
        Mockito.when(newsService.getNews(Mockito.eq(challenge))).thenReturn(newsItems);
        Mockito.when(permissionService.checkIsAllowedToViewIdea(Mockito.eq(challenge), Mockito.eq(firstIdea), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionService.checkIsAllowedToViewIdea(Mockito.eq(challenge), Mockito.eq(secondIdea), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionService.checkIsAllowedToViewIdea(Mockito.eq(challenge), Mockito.eq(thirdIdea), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(false);
        Mockito.when(permissionService.checkIsAllowedToViewVotes(Mockito.eq(challenge), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionService.checkHasEditPermission(Mockito.eq(challenge), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(jwtService.generateInvitationToken(Mockito.any(InvitationInformation.class))).thenReturn("token1", "token2");

        List<AverageAndOwnVoteForIdea> votings = Arrays.asList(
                AverageAndOwnVoteForIdea.builder().ideaId(firstIdea.getId()).ownVote(1).averageVote(5D).build(),
                AverageAndOwnVoteForIdea.builder().ideaId(secondIdea.getId()).ownVote(0).averageVote(13D).build()
        );
        Mockito.when(voteService.getAvgAndOwnVoteForIdeaIds(Mockito.eq(Arrays.asList(firstIdea.getId(), secondIdea.getId())), Mockito.eq("caller2@uni-due.de"))).thenReturn(votings);

        ChallengeDetailsDTO expected = ChallengeDetailsDTO.from(challenge);
        expected.getNews().add(NewsItemDTO.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getChallengeEnd().getTime()).content("Ende").build());
        expected.getNews().add(NewsItemDTO.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getImplementationStart().getTime()).content("Siegerbestimmung").build());
        expected.getNews().add(NewsItemDTO.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getRefactoringStart().getTime()).content("Überarbeitungsphase").build());
        expected.getNews().add(NewsItemDTO.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getVotingStart().getTime()).content("Abstimmphase").build());
        expected.getNews().add(NewsItemDTO.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getReviewStart().getTime()).content("Einreichungsfrist").build());
        expected.getNews().add(NewsItemDTO.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getSubmissionStart().getTime()).content("Start").build());
        expected.getAttachments().add(
                ChallengeAttachmentDTO.builder()
                        .name(attachmentName1)
                        .type("image/png")
                        .url(String.format("https://diamant.se-tools.de/api/challenge/%s/attachment/%s", challengeId, attachmentId1))
                        .build()
        );
        expected.getAttachments().add(
                ChallengeAttachmentDTO.builder()
                        .name(attachmentName2)
                        .type("image/png")
                        .url(String.format("https://diamant.se-tools.de/api/challenge/%s/attachment/%s", challengeId, attachmentId2))
                        .build()
        );
        expected.getIdeas().addAll(
                Arrays.asList(
                        IdeaPreview.builder().id("First Idea").shortDescription("KurzBeschreibung Idee 1").teamName("Heja BVB").build(),
                        IdeaPreview.builder().id("Zweite Idea").shortDescription("KurzBeschreibung Idee 2").teamName("Heja BVB2").build()
                )
        );
        expected.getIdeas().get(0).setCurrentUserPermissions(Arrays.asList(IdeaPermissions.EDIT, IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE));
        expected.getIdeas().get(1).setCurrentUserPermissions(Collections.singletonList(IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE));
        expected.getIdeas().get(0).setOwnVoting(votings.get(0).getOwnVote());
        expected.getIdeas().get(0).setAverageVoting(votings.get(0).getAverageVote());
        expected.getIdeas().get(1).setOwnVoting(votings.get(1).getOwnVote());
        expected.getIdeas().get(1).setAverageVoting(votings.get(1).getAverageVote());
        expected.getReferees().add(Person.builder().name("Caller Callerson").email("caller@unidue.de").build());
        expected.setEditPermission(true);
        expected.setInvitationLinkReferee("https://diamant.se-tools.de/invitation/token1");
        expected.setInvitationLinkInvitedUsers(null);

        this.mockMvc.perform(
                get("/api/challenge/" + challengeId)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)));
        Mockito.verify(denyAccessInterceptor, Mockito.times(1)).interceptViewChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.verify(denyAccessInterceptor, Mockito.times(1)).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.verify(permissionService, Mockito.times(1)).checkIsAllowedToViewVotes(Mockito.eq(challenge), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionService, Mockito.times(1)).checkHasEditPermission(Mockito.eq(challenge), Mockito.eq(currentUser), Mockito.any(StringBuilder.class));
    }

    @Test
    @WithMockUser(username = "caller2@uni-due.de", roles = {"caller"})
    public void getChallengeDetailsForInviteVisibility() throws Exception {
        String challengeId = "5c6c25fe3accaf414c792f49";
        Challenge challenge = new Challenge();
        challenge.setId(challengeId);
        challenge.setShortDescription("Kurzbeschreibung");
        challenge.setDescription("Lange Beschreibung");
        challenge.setSubmissionStart(parseDate("05.09.2019 10:10:00"));
        challenge.setReviewStart(parseDate("06.09.2019 10:10:00"));
        challenge.setRefactoringStart(parseDate("07.09.2019 10:10:00"));
        challenge.setVotingStart(parseDate("08.09.2019 10:10:00"));
        challenge.setImplementationStart(parseDate("04.08.2019 10:10:00"));
        challenge.setChallengeEnd(parseDate("09.09.2019 10:10:00"));
        challenge.setVisibility(Visibility.INVITE);
        challenge.setAwards("Preise.. :) Cooles Zeug..");
        challenge.setCaller(Person.builder().email("callEr@uni-due.de").build());
        challenge.getReferees().add(Person.builder().name("Caller Callerson").email("caller@unidue.de").build());

        List<NewsItem> newsItems = Arrays.asList(
                NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getChallengeEnd()).content("Ende").build(),
                NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getImplementationStart()).content("Siegerbestimmung").build(),
                NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getRefactoringStart()).content("Überarbeitungsphase").build(),
                NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getVotingStart()).content("Abstimmphase").build(),
                NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getReviewStart()).content("Einreichungsfrist").build(),
                NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getSubmissionStart()).content("Start").build());

        Mockito.when(challengeService.getChallengeDetails(Mockito.eq(challengeId))).thenReturn(challenge);
        String attachmentId1 = "5c6c25fe3accaf414c792f49";
        String attachmentName1 = "Datei 1";
        String attachmentId2 = "5c617f7c0f4a6437b4f769fc";
        String attachmentName2 = "Datei 2";
        GridFSFindIterable attachments = findAttachmentMock(attachmentId1, attachmentName1, attachmentId2, attachmentName2);
        Mockito.when(attachmentService.getChallengeAttachmentsByChallengeId(Mockito.eq(challengeId))).thenReturn(attachments);

        Idea firstIdea = Idea.builder().id("First Idea").creator(Person.builder().email("caller2@uni-due.de").build()).shortDescription("KurzBeschreibung Idee 1").teamName("Heja BVB").build();
        Idea secondIdea = Idea.builder().id("Zweite Idea").creator(Person.builder().email("user2").build()).shortDescription("KurzBeschreibung Idee 2").teamName("Heja BVB2").build();
        Idea thirdIdea = Idea.builder().id("Dritte Idea").creator(Person.builder().email("user3").build()).shortDescription("KurzBeschreibung Idee 3").teamName("Heja BVB3").build();
        Mockito.when(ideaService.findByChallengeId(Mockito.eq(challengeId))).thenReturn(
                Arrays.asList(
                        firstIdea,
                        secondIdea,
                        thirdIdea
                )
        );
        String currentUser = "caller2@uni-due.de";

        Mockito.when(ideaService.getPermissions(Mockito.eq(challenge), Mockito.eq(firstIdea), Mockito.eq("caller2@uni-due.de"))).thenReturn(Arrays.asList(IdeaPermissions.EDIT, IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE));
        Mockito.when(ideaService.getPermissions(Mockito.eq(challenge), Mockito.eq(secondIdea), Mockito.eq("caller2@uni-due.de"))).thenReturn(Collections.singletonList(IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE));
        Mockito.when(newsService.getNews(Mockito.eq(challenge))).thenReturn(newsItems);
        Mockito.when(permissionService.checkIsAllowedToViewIdea(Mockito.eq(challenge), Mockito.eq(firstIdea), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionService.checkIsAllowedToViewIdea(Mockito.eq(challenge), Mockito.eq(secondIdea), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionService.checkIsAllowedToViewIdea(Mockito.eq(challenge), Mockito.eq(thirdIdea), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(false);
        Mockito.when(permissionService.checkIsAllowedToViewVotes(Mockito.eq(challenge), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(permissionService.checkHasEditPermission(Mockito.eq(challenge), Mockito.eq(currentUser), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(jwtService.generateInvitationToken(Mockito.any(InvitationInformation.class))).thenReturn("token1", "token2");

        List<AverageAndOwnVoteForIdea> votings = Arrays.asList(
                AverageAndOwnVoteForIdea.builder().ideaId(firstIdea.getId()).ownVote(1).averageVote(5D).build(),
                AverageAndOwnVoteForIdea.builder().ideaId(secondIdea.getId()).ownVote(0).averageVote(13D).build()
        );
        Mockito.when(voteService.getAvgAndOwnVoteForIdeaIds(Mockito.eq(Arrays.asList(firstIdea.getId(), secondIdea.getId())), Mockito.eq("caller2@uni-due.de"))).thenReturn(votings);

        ChallengeDetailsDTO expected = ChallengeDetailsDTO.from(challenge);
        expected.getNews().add(NewsItemDTO.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getChallengeEnd().getTime()).content("Ende").build());
        expected.getNews().add(NewsItemDTO.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getImplementationStart().getTime()).content("Siegerbestimmung").build());
        expected.getNews().add(NewsItemDTO.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getRefactoringStart().getTime()).content("Überarbeitungsphase").build());
        expected.getNews().add(NewsItemDTO.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getVotingStart().getTime()).content("Abstimmphase").build());
        expected.getNews().add(NewsItemDTO.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getReviewStart().getTime()).content("Einreichungsfrist").build());
        expected.getNews().add(NewsItemDTO.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getSubmissionStart().getTime()).content("Start").build());
        expected.getAttachments().add(
                ChallengeAttachmentDTO.builder()
                        .name(attachmentName1)
                        .type("image/png")
                        .url(String.format("https://diamant.se-tools.de/api/challenge/%s/attachment/%s", challengeId, attachmentId1))
                        .build()
        );
        expected.getAttachments().add(
                ChallengeAttachmentDTO.builder()
                        .name(attachmentName2)
                        .type("image/png")
                        .url(String.format("https://diamant.se-tools.de/api/challenge/%s/attachment/%s", challengeId, attachmentId2))
                        .build()
        );
        expected.getIdeas().addAll(
                Arrays.asList(
                        IdeaPreview.builder().id("First Idea").shortDescription("KurzBeschreibung Idee 1").teamName("Heja BVB").build(),
                        IdeaPreview.builder().id("Zweite Idea").shortDescription("KurzBeschreibung Idee 2").teamName("Heja BVB2").build()
                )
        );
        expected.getIdeas().get(0).setCurrentUserPermissions(Arrays.asList(IdeaPermissions.EDIT, IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE));
        expected.getIdeas().get(1).setCurrentUserPermissions(Collections.singletonList(IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE));
        expected.getIdeas().get(0).setOwnVoting(votings.get(0).getOwnVote());
        expected.getIdeas().get(0).setAverageVoting(votings.get(0).getAverageVote());
        expected.getIdeas().get(1).setOwnVoting(votings.get(1).getOwnVote());
        expected.getIdeas().get(1).setAverageVoting(votings.get(1).getAverageVote());
        expected.getReferees().add(Person.builder().name("Caller Callerson").email("caller@unidue.de").build());
        expected.setEditPermission(true);
        expected.setInvitationLinkReferee("https://diamant.se-tools.de/invitation/token2");
        expected.setInvitationLinkInvitedUsers("https://diamant.se-tools.de/invitation/token1");

        this.mockMvc.perform(
                get("/api/challenge/" + challengeId)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)));
        Mockito.verify(denyAccessInterceptor, Mockito.times(1)).interceptViewChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.verify(denyAccessInterceptor, Mockito.times(1)).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.verify(permissionService, Mockito.times(1)).checkIsAllowedToViewVotes(Mockito.eq(challenge), Mockito.any(StringBuilder.class));
        Mockito.verify(permissionService, Mockito.times(1)).checkHasEditPermission(Mockito.eq(challenge), Mockito.eq(currentUser), Mockito.any(StringBuilder.class));
    }

    @Test
    public void getChallengeDetailsWithoutUser() throws Exception {
        String challengeId = "5c6c25fe3accaf414c792f49";
        Challenge challenge = new Challenge();
        challenge.setId(challengeId);
        challenge.setShortDescription("Kurzbeschreibung");
        challenge.setDescription("Lange Beschreibung");
        challenge.setSubmissionStart(parseDate("05.09.2019 10:10:00"));
        challenge.setReviewStart(parseDate("06.09.2019 10:10:00"));
        challenge.setRefactoringStart(parseDate("07.09.2019 10:10:00"));
        challenge.setVotingStart(parseDate("08.09.2019 10:10:00"));
        challenge.setImplementationStart(parseDate("04.08.2019 10:10:00"));
        challenge.setChallengeEnd(parseDate("09.09.2019 10:10:00"));
        challenge.setVisibility(Visibility.INVITE);
        challenge.setAwards("Preise.. :) Cooles Zeug..");
        challenge.setCaller(Person.builder().email("caller").build());

        List<NewsItem> newsItems = Arrays.asList(
                NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getChallengeEnd()).content("Ende").build(),
                NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getImplementationStart()).content("Siegerbestimmung").build(),
                NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getRefactoringStart()).content("Überarbeitungsphase").build(),
                NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getVotingStart()).content("Abstimmphase").build(),
                NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getReviewStart()).content("Einreichungsfrist").build(),
                NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getSubmissionStart()).content("Start").build()
        );

        Mockito.when(challengeService.getChallengeDetails(Mockito.eq(challengeId))).thenReturn(challenge);
        Mockito.when(voteService.getAvgAndOwnVoteForChallenge(Mockito.eq(challengeId), Mockito.eq("caller2@uni-due.de"))).thenReturn(Collections.emptyList());
        Mockito.when(newsService.getNews(Mockito.eq(challenge))).thenReturn(newsItems);
        Mockito.when(permissionService.checkHasEditPermission(Mockito.eq(challenge), Mockito.eq(KeycloakUtils.getUserOrDefault(null)), Mockito.any(StringBuilder.class))).thenReturn(false);


        String attachmentId1 = "5c6c25fe3accaf414c792f49";
        String attachmentName1 = "Datei 1";
        String attachmentId2 = "5c617f7c0f4a6437b4f769fc";
        String attachmentName2 = "Datei 2";
        GridFSFindIterable attachments = findAttachmentMock(attachmentId1, attachmentName1, attachmentId2, attachmentName2);
        Mockito.when(attachmentService.getChallengeAttachmentsByChallengeId(Mockito.eq(challengeId))).thenReturn(attachments);

        ChallengeDetailsDTO expected = ChallengeDetailsDTO.from(challenge);
        expected.getNews().add(NewsItemDTO.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getChallengeEnd().getTime()).content("Ende").build());
        expected.getNews().add(NewsItemDTO.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getImplementationStart().getTime()).content("Siegerbestimmung").build());
        expected.getNews().add(NewsItemDTO.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getRefactoringStart().getTime()).content("Überarbeitungsphase").build());
        expected.getNews().add(NewsItemDTO.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getVotingStart().getTime()).content("Abstimmphase").build());
        expected.getNews().add(NewsItemDTO.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getReviewStart().getTime()).content("Einreichungsfrist").build());
        expected.getNews().add(NewsItemDTO.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getSubmissionStart().getTime()).content("Start").build());
        expected.getAttachments().add(
                ChallengeAttachmentDTO.builder()
                        .name(attachmentName1)
                        .type("image/png")
                        .url(String.format("https://diamant.se-tools.de/api/challenge/%s/attachment/%s", challengeId, attachmentId1))
                        .build()
        );
        expected.getAttachments().add(
                ChallengeAttachmentDTO.builder()
                        .name(attachmentName2)
                        .type("image/png")
                        .url(String.format("https://diamant.se-tools.de/api/challenge/%s/attachment/%s", challengeId, attachmentId2))
                        .build()
        );
        expected.setEditPermission(false);
        expected.setInvitationLinkReferee(null);
        expected.setInvitationLinkInvitedUsers(null);

        this.mockMvc.perform(
                get("/api/challenge/" + challengeId)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)));
        Mockito.verify(denyAccessInterceptor, Mockito.only()).interceptViewChallenge(Mockito.eq(challengeId), Mockito.isNull());
        Mockito.verify(permissionService, Mockito.times(1)).checkHasEditPermission(Mockito.eq(challenge), Mockito.eq(KeycloakUtils.getUserOrDefault(null)), Mockito.any(StringBuilder.class));
    }

    @Test
    @WithMockUser(username = "callEr@uni-due.de", roles = {"caller"})
    public void updateGeneralInformation() throws Exception {
        GeneralInformation generalInformation = new GeneralInformation(
                "neuerTitel",
                "KurzB2",
                Visibility.OPEN
        );
        UpdateIdeaFieldDTO postArgument = new UpdateIdeaFieldDTO(
                UpdateIdeaFieldDTO.Field.GENERAL_INFORMATION,
                generalInformation
        );
        ArgumentCaptor<GeneralInformation> saveCaptor = ArgumentCaptor.forClass(GeneralInformation.class);


        String challengeId = "5c6c25fe3accaf414c792f49";
        Mockito.doNothing().when(denyAccessInterceptor).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.doNothing().when(challengeService).updateGeneralInformation(Mockito.eq(challengeId), saveCaptor.capture());

        this.mockMvc.perform(
                put("/api/challenge/" + challengeId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(postArgument))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(challengeId));

        Mockito.verify(denyAccessInterceptor, Mockito.times(1)).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.verify(challengeService, Mockito.times(1)).updateGeneralInformation(Mockito.eq(challengeId), saveCaptor.capture());
        Assertions.assertThat(saveCaptor.getValue()).isEqualTo(generalInformation);
    }

    @Test
    @WithMockUser(username = "callEr@uni-due.de", roles = {"caller"})
    public void updateGeneralInformationValidatesInput() throws Exception {
        GeneralInformation generalInformation = new GeneralInformation(
                "",
                "KurzB2",
                Visibility.OPEN
        );
        UpdateIdeaFieldDTO postArgument = new UpdateIdeaFieldDTO(
                UpdateIdeaFieldDTO.Field.GENERAL_INFORMATION,
                generalInformation
        );
        ArgumentCaptor<GeneralInformation> saveCaptor = ArgumentCaptor.forClass(GeneralInformation.class);


        String challengeId = "5c6c25fe3accaf414c792f49";
        Mockito.doNothing().when(denyAccessInterceptor).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.doNothing().when(challengeService).updateGeneralInformation(Mockito.eq(challengeId), saveCaptor.capture());

        this.mockMvc.perform(
                put("/api/challenge/" + challengeId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(postArgument))
        )
                .andDo(print())
                .andExpect(status().isBadRequest());

        Mockito.verify(denyAccessInterceptor, Mockito.times(1)).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.verify(challengeService, Mockito.never()).updateGeneralInformation(Mockito.eq(challengeId), saveCaptor.capture());
    }

    @Test
    @WithMockUser(username = "callEr@uni-due.de", roles = {"caller"})
    public void updateMileStones() throws Exception {
        MileStones mileStones = new MileStones(
                parseDate("05.09.2019 10:10:00").getTime(),
                parseDate("06.09.2019 10:10:00").getTime(),
                parseDate("07.09.2019 10:10:00").getTime(),
                parseDate("08.09.2019 10:10:00").getTime(),
                parseDate("09.09.2019 10:10:00").getTime(),
                parseDate("10.09.2019 10:10:00").getTime()
        );
        UpdateIdeaFieldDTO postArgument = new UpdateIdeaFieldDTO(
                UpdateIdeaFieldDTO.Field.MILESTONES,
                mileStones
        );
        ArgumentCaptor<MileStones> saveCaptor = ArgumentCaptor.forClass(MileStones.class);


        String challengeId = "5c6c25fe3accaf414c792f49";
        Mockito.doNothing().when(denyAccessInterceptor).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.doNothing().when(challengeService).updateMileStones(Mockito.eq(challengeId), saveCaptor.capture());

        this.mockMvc.perform(
                put("/api/challenge/" + challengeId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(postArgument))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(challengeId));

        Mockito.verify(denyAccessInterceptor, Mockito.times(1)).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.verify(challengeService, Mockito.times(1)).updateMileStones(Mockito.eq(challengeId), saveCaptor.capture());
        Assertions.assertThat(saveCaptor.getValue()).isEqualTo(mileStones);
    }

    @Test
    @WithMockUser(username = "callEr@uni-due.de", roles = {"caller"})
    public void updateMileStonesValidatesInput() throws Exception {
        MileStones mileStones = new MileStones(
                parseDate("05.09.2019 10:10:00").getTime(),
                parseDate("06.09.2019 10:10:00").getTime(),
                parseDate("07.09.2019 10:10:00").getTime(),
                parseDate("08.09.2019 10:10:00").getTime(),
                parseDate("04.08.2019 10:10:00").getTime(),
                parseDate("09.09.2019 10:10:00").getTime()
        );
        UpdateIdeaFieldDTO postArgument = new UpdateIdeaFieldDTO(
                UpdateIdeaFieldDTO.Field.MILESTONES,
                        mileStones
        );
        ArgumentCaptor<MileStones> saveCaptor = ArgumentCaptor.forClass(MileStones.class);


        String challengeId = "5c6c25fe3accaf414c792f49";
        Mockito.doNothing().when(denyAccessInterceptor).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.doNothing().when(challengeService).updateMileStones(Mockito.eq(challengeId), saveCaptor.capture());

        this.mockMvc.perform(
                put("/api/challenge/" + challengeId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(postArgument))
        )
                .andDo(print())
                .andExpect(status().isBadRequest());

        Mockito.verify(denyAccessInterceptor, Mockito.times(1)).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.verify(challengeService, Mockito.never()).updateMileStones(Mockito.eq(challengeId), saveCaptor.capture());
    }

    @Test
    @WithMockUser(username = "callEr@uni-due.de", roles = {"caller"})
    public void updateDescription() throws Exception {
        UpdateIdeaFieldDTO postArgument = new UpdateIdeaFieldDTO(
                UpdateIdeaFieldDTO.Field.DESCRIPTION,
                "NEue Beschreibung :)"
        );
        ArgumentCaptor<String> saveCaptor = ArgumentCaptor.forClass(String.class);


        String challengeId = "5c6c25fe3accaf414c792f49";
        Mockito.doNothing().when(denyAccessInterceptor).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.doNothing().when(challengeService).updateDescription(Mockito.eq(challengeId), saveCaptor.capture());

        this.mockMvc.perform(
                put("/api/challenge/" + challengeId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(postArgument))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(challengeId));

        Mockito.verify(denyAccessInterceptor, Mockito.times(1)).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.verify(challengeService, Mockito.times(1)).updateDescription(Mockito.eq(challengeId), saveCaptor.capture());
        Assertions.assertThat(saveCaptor.getValue()).isEqualTo(postArgument.getNewValueDTO());
    }

    @Test
    @WithMockUser(username = "callEr@uni-due.de", roles = {"caller"})
    public void updateAwards() throws Exception {
        UpdateIdeaFieldDTO postArgument = new UpdateIdeaFieldDTO(
                UpdateIdeaFieldDTO.Field.AWARDS,
                "NEue Preise :) :)"
        );
        ArgumentCaptor<String> saveCaptor = ArgumentCaptor.forClass(String.class);


        String challengeId = "5c6c25fe3accaf414c792f49";
        Mockito.doNothing().when(denyAccessInterceptor).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.doNothing().when(challengeService).updateAwards(Mockito.eq(challengeId), saveCaptor.capture());

        this.mockMvc.perform(
                put("/api/challenge/" + challengeId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(postArgument))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(challengeId));

        Mockito.verify(denyAccessInterceptor, Mockito.times(1)).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.verify(challengeService, Mockito.times(1)).updateAwards(Mockito.eq(challengeId), saveCaptor.capture());
        Assertions.assertThat(saveCaptor.getValue()).isEqualTo(postArgument.getNewValueDTO());
    }

    @Test
    @WithMockUser(username = "callEr@uni-due.de", roles = {"caller"})
    public void updateInvitedUsers() throws Exception {
        UpdatePersons up = new UpdatePersons(new HashSet<>());
        up.getPersons().add(Person.builder().name("iv1").email("iv1@example.com").build());
        up.getPersons().add(Person.builder().name("iv2").email("iv2@example.com").build());

        UpdateIdeaFieldDTO postArgument = new UpdateIdeaFieldDTO(
                UpdateIdeaFieldDTO.Field.INVITED_USERS,
                up
        );
        ArgumentCaptor<Set<Person>> saveCaptor = ArgumentCaptor.forClass(Set.class);


        String challengeId = "5c6c25fe3accaf414c792f49";
        Mockito.doNothing().when(denyAccessInterceptor).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.doNothing().when(challengeService).updateInvitedUsers(Mockito.eq(challengeId), saveCaptor.capture());
        Mockito.when(keycloakUserService.checkIfUserExists(Mockito.eq("iv1@example.com"))).thenReturn(true);
        Mockito.when(keycloakUserService.checkIfUserExists(Mockito.eq("iv2@example.com"))).thenReturn(true);

        this.mockMvc.perform(
                put("/api/challenge/" + challengeId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(postArgument))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(challengeId));

        Mockito.verify(denyAccessInterceptor, Mockito.times(1)).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.verify(challengeService, Mockito.times(1)).updateInvitedUsers(Mockito.eq(challengeId), saveCaptor.capture());
        Mockito.verify(keycloakUserService, Mockito.times(1)).checkIfUserExists(Mockito.eq("iv1@example.com"));
        Mockito.verify(keycloakUserService, Mockito.times(1)).checkIfUserExists(Mockito.eq("iv2@example.com"));
        Assertions.assertThat(saveCaptor.getValue()).isEqualTo(up.getPersons());
    }

    @Test
    @WithMockUser(username = "callEr@uni-due.de", roles = {"caller"})
    public void updateReferees() throws Exception {
        Person caller = Person.builder().email("caller@example.com").name("caller callerson").build();
        Challenge c = Challenge.builder().caller(caller).build();
        UpdatePersons up = new UpdatePersons(new HashSet<>());
        up.getPersons().add(Person.builder().name("ref1").email("ref1@example.com").build());
        up.getPersons().add(Person.builder().name("ref2").email("ref2@example.com").build());

        UpdateIdeaFieldDTO postArgument = new UpdateIdeaFieldDTO(
                UpdateIdeaFieldDTO.Field.REFEREES,
                up
        );
        ArgumentCaptor<Set<Person>> saveCaptor = ArgumentCaptor.forClass(Set.class);


        String challengeId = "5c6c25fe3accaf414c792f49";
        Mockito.doNothing().when(denyAccessInterceptor).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.doNothing().when(challengeService).updateReferees(Mockito.eq(challengeId), saveCaptor.capture(), Mockito.eq(caller));
        Mockito.when(keycloakUserService.checkIfUserExists(Mockito.eq("ref1@example.com"))).thenReturn(true);
        Mockito.when(keycloakUserService.checkIfUserExists(Mockito.eq("ref2@example.com"))).thenReturn(true);
        Mockito.when(challengeService.getChallengeDetails(Mockito.eq(challengeId))).thenReturn(c);

        this.mockMvc.perform(
                put("/api/challenge/" + challengeId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(postArgument))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(challengeId));

        Mockito.verify(denyAccessInterceptor, Mockito.times(1)).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.verify(challengeService, Mockito.times(1)).updateReferees(Mockito.eq(challengeId), saveCaptor.capture(), Mockito.eq(caller));
        Mockito.verify(keycloakUserService, Mockito.times(1)).checkIfUserExists(Mockito.eq("ref1@example.com"));
        Mockito.verify(keycloakUserService, Mockito.times(1)).checkIfUserExists(Mockito.eq("ref2@example.com"));
        Assertions.assertThat(saveCaptor.getValue()).isEqualTo(up.getPersons());
    }

    @Test
    @WithMockUser(username = "callEr@uni-due.de", roles = {"caller"})
    public void updateAttachments() throws Exception {

        UpdateIdeaFieldDTO postArgument = new UpdateIdeaFieldDTO(
                UpdateIdeaFieldDTO.Field.ATTACHMENTS,
                new UpdateAttachments(Arrays.asList("id1", "id2"))
        );
        ArgumentCaptor<List<String>> saveCaptor = ArgumentCaptor.forClass(List.class);


        String challengeId = "5c6c25fe3accaf414c792f49";
        Mockito.doNothing().when(denyAccessInterceptor).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.doNothing().when(attachmentService).deleteAllUnreferencedChallengeAttachments(Mockito.eq(challengeId), Mockito.eq(Arrays.asList("id1", "id2")));

        this.mockMvc.perform(
                put("/api/challenge/" + challengeId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(postArgument))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(challengeId));

        Mockito.verify(denyAccessInterceptor, Mockito.times(1)).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.verify(attachmentService, Mockito.times(1)).deleteAllUnreferencedChallengeAttachments(Mockito.eq(challengeId), Mockito.eq(Arrays.asList("id1", "id2")));
    }

    @Test
    @WithMockUser(username = "callEr@uni-due.de", roles = {"caller"})
    public void updateNews() throws Exception {
        UpdateIdeaFieldDTO postArgument = new UpdateIdeaFieldDTO(
                UpdateIdeaFieldDTO.Field.NEW_NEWS_ENTRY,
                NewsItemDTO.builder().content("Neuer News Eintrag").date(parseDate("08.04.2020 10:10:10").getTime()).type(NewsItem.NewsItemType.PILOTING).build()
        );
        ArgumentCaptor<NewsItem> saveCaptor = ArgumentCaptor.forClass(NewsItem.class);


        String challengeId = "5c6c25fe3accaf414c792f49";
        Mockito.doNothing().when(denyAccessInterceptor).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.doNothing().when(newsService).save(saveCaptor.capture());

        this.mockMvc.perform(
                put("/api/challenge/" + challengeId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(postArgument))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(challengeId));

        Mockito.verify(denyAccessInterceptor, Mockito.times(1)).interceptEditChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.verify(newsService, Mockito.times(1)).save(saveCaptor.capture());
        Assertions.assertThat(saveCaptor.getValue()).isEqualTo(new NewsItem(null, challengeId, parseDate("08.04.2020 10:10:10"), NewsItem.NewsItemType.PILOTING, "Neuer News Eintrag"));
    }


    private GridFSFindIterable findAttachmentMock(String firstId, String firstName, String secondId, String secondName) {
        GridFSFindIterable result = Mockito.mock(GridFSFindIterable.class);
        MongoCursor cursor = Mockito.mock(MongoCursor.class);
        Mockito.when(cursor.hasNext()).thenReturn(true, true, false);
        Document metaDataFile1 = new Document();
        metaDataFile1.append(AttachmentService.META_DATA_FIELD_FILE_NAME, firstName);
        metaDataFile1.append(AttachmentService.META_DATA_FIELD_CONTENT_TYPE, "image/png");
        Document metaDataFile2 = new Document();
        metaDataFile2.append(AttachmentService.META_DATA_FIELD_FILE_NAME, secondName);
        metaDataFile2.append(AttachmentService.META_DATA_FIELD_CONTENT_TYPE, "image/png");
        Mockito.when(cursor.next())
                .thenReturn(new GridFSFile(new BsonObjectId(new ObjectId(firstId)), firstId, 0, 0, new Date(), "hash", metaDataFile1))
                .thenReturn(new GridFSFile(new BsonObjectId(new ObjectId(secondId)), secondId, 0, 0, new Date(), "hash", metaDataFile2))
                .thenReturn(null);

        Mockito.when(result.iterator()).thenReturn(cursor);
        return result;
    }

}
