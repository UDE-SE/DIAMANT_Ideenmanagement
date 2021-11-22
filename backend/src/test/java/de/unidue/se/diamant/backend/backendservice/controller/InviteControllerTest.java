package de.unidue.se.diamant.backend.backendservice.controller;

import de.unidue.se.diamant.backend.backendservice.dto.user.NewUserDTO;
import de.unidue.se.diamant.backend.backendservice.service.challenge.ChallengeService;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import de.unidue.se.diamant.backend.backendservice.service.idea.IdeaService;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.Idea;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.TeamMember;
import de.unidue.se.diamant.backend.backendservice.service.invitation.InvitationInformation;
import de.unidue.se.diamant.backend.backendservice.service.invitation.InvitationType;
import de.unidue.se.diamant.backend.backendservice.service.invitation.JWTService;
import de.unidue.se.diamant.backend.backendservice.service.keycloak.KeycloakUserService;
import de.unidue.se.diamant.backend.backendservice.service.keycloak.dto.KeycloakUser;
import de.unidue.se.diamant.backend.backendservice.service.permissions.DenyAccessInterceptor;
import de.unidue.se.diamant.backend.backendservice.service.permissions.PermissionServiceFacade;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InviteControllerTest extends RestDocTest {

    @MockBean
    private ChallengeService challengeService;
    @MockBean
    private DenyAccessInterceptor denyAccessInterceptor;
    @MockBean
    private IdeaService ideaService;
    @MockBean
    private JWTService jwtService;


    @Test
    public void acceptInvitationFailsIfNotLoggedIn() throws Exception {

        this.mockMvc.perform(
                post("/api/invitation/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content("token")
        )
                .andDo(print())
                .andExpect(status().is(403));
    }

    @Test
    @WithMockUser(username = "user@uni-due.de", roles = {"user"})
    public void acceptInvitationFailsWithBlankToken() throws Exception {

        Mockito.when(jwtService.getInvitationInformationFromToken(Mockito.anyString())).thenCallRealMethod();

        this.mockMvc.perform(
                post("/api/invitation/accept")
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@uni-due.de", roles = {"user"})
    public void acceptInvitationAsReferee() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String mail = "user@uni-due.de";
        String fakeToken = "eyJhbGciOiJIUzUxMiJ9.eyJUWVBFIjoiSU5WSVRFRF9VU0VSIiwiVEFSR0VUX0lEIjoiNDIiLCJBRERJVElPTkFMX0lORk9STUFUSU9OIjoiRGluZ2UiLCJleHAiOjE1OTMwNzQyNTR9.zNHVJZPsRiOHtlJiEZfr50L_UfF3yZrcoxugtE9XzJ4gzWiK3HLk9urLJrxiguQ_xd_Cc7Kd6ws0ORFXMEvooQ";
        InvitationInformation invitationInformation = InvitationInformation.builder()
                .challengeId(challengeId).type(InvitationType.REFEREE).build();
        Challenge challenge = new Challenge();
        challenge.getReferees().add(Person.builder().email("test@person.de").build());
        challenge.setCaller(Person.builder().email("callerMail").build());

        Mockito.when(challengeService.getChallengeDetails(challengeId)).thenReturn(challenge);
        ArgumentCaptor<Set<Person>> saveCaptor = ArgumentCaptor.forClass(Set.class);
        Mockito.doNothing().when(challengeService).updateReferees(Mockito.any(), saveCaptor.capture(), Mockito.notNull());
        Mockito.when(jwtService.getInvitationInformationFromToken(Mockito.eq(fakeToken))).thenReturn(invitationInformation);

        this.mockMvc.perform(
                post("/api/invitation/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fakeToken)
        )
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(challengeService, Mockito.times(1)).updateReferees(Mockito.eq(challengeId), saveCaptor.capture(), Mockito.eq(challenge.getCaller()));
        Assertions.assertThat(new ArrayList(saveCaptor.getValue())).contains(Person.builder().email(mail).name(String.format("%s %s", mail, mail)).build());
        Assertions.assertThat(saveCaptor.getValue().size()).isEqualTo(2);
    }

    @Test
    @WithMockUser(username = "user@uni-due.de", roles = {"user"})
    public void acceptInvitationAsParticipant() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String mail = "user@uni-due.de";
        String fakeToken = "eyJhbGciOiJIUzUxMiJ9.eyJUWVBFIjoiSU5WSVRFRF9VU0VSIiwiVEFSR0VUX0lEIjoiNDIiLCJBRERJVElPTkFMX0lORk9STUFUSU9OIjoiRGluZ2UiLCJleHAiOjE1OTMwNzQyNTR9.zNHVJZPsRiOHtlJiEZfr50L_UfF3yZrcoxugtE9XzJ4gzWiK3HLk9urLJrxiguQ_xd_Cc7Kd6ws0ORFXMEvooQ";
        InvitationInformation invitationInformation = InvitationInformation.builder()
                .challengeId(challengeId).type(InvitationType.INVITED_USER).build();
        Challenge challenge = new Challenge();

        Mockito.when(challengeService.getChallengeDetails(challengeId)).thenReturn(challenge);
        ArgumentCaptor<Set<Person>> saveCaptor = ArgumentCaptor.forClass(Set.class);
        Mockito.doNothing().when(challengeService).updateInvitedUsers(Mockito.any(), saveCaptor.capture());
        Mockito.when(jwtService.getInvitationInformationFromToken(Mockito.eq(fakeToken))).thenReturn(invitationInformation);

        this.mockMvc.perform(
                post("/api/invitation/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fakeToken)
        )
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(challengeService, Mockito.times(1)).updateInvitedUsers(Mockito.eq(challengeId), saveCaptor.capture());
        Assertions.assertThat(new ArrayList(saveCaptor.getValue())).contains(Person.builder().email(mail).name(String.format("%s %s", mail, mail)).build());
        Assertions.assertThat(saveCaptor.getValue().size()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "user@uni-due.de", roles = {"user"})
    public void acceptInvitationAsTeamMember() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47dcc0c23c1bc43b8a";
        String mail = "user@uni-due.de";
        String fakeToken = "eyJhbGciOiJIUzUxMiJ9.eyJUWVBFIjoiSU5WSVRFRF9VU0VSIiwiVEFSR0VUX0lEIjoiNDIiLCJBRERJVElPTkFMX0lORk9STUFUSU9OIjoiRGluZ2UiLCJleHAiOjE1OTMwNzQyNTR9.zNHVJZPsRiOHtlJiEZfr50L_UfF3yZrcoxugtE9XzJ4gzWiK3HLk9urLJrxiguQ_xd_Cc7Kd6ws0ORFXMEvooQ";
        InvitationInformation invitationInformation = InvitationInformation.builder()
                .challengeId(challengeId).type(InvitationType.TEAM_MEMBER).ideaId(ideaId).build();
        Idea idea = new Idea();
        idea.getTeamMember().add(TeamMember.fromPerson(Person.builder().email("test@person.de").build()));

        Mockito.when(ideaService.findById(ideaId)).thenReturn(idea);
        ArgumentCaptor<List<TeamMember>> saveCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.doNothing().when(ideaService).updateTeamMember(Mockito.any(), saveCaptor.capture());
        Mockito.when(jwtService.getInvitationInformationFromToken(Mockito.eq(fakeToken))).thenReturn(invitationInformation);

        this.mockMvc.perform(
                post("/api/invitation/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fakeToken)
        )
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(denyAccessInterceptor, Mockito.only()).interceptViewChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.verify(ideaService, Mockito.times(1)).updateTeamMember(Mockito.eq(ideaId), saveCaptor.capture());
        Assertions.assertThat(new ArrayList(saveCaptor.getValue())).contains(TeamMember.fromPerson(Person.builder().email(mail).name(String.format("%s %s", mail, mail)).build()));
        Assertions.assertThat(saveCaptor.getValue().size()).isEqualTo(2);
    }
}
