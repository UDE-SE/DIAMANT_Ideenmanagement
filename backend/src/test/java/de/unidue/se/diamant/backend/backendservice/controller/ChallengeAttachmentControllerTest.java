package de.unidue.se.diamant.backend.backendservice.controller;

import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import de.unidue.se.diamant.backend.backendservice.service.AttachmentService;
import de.unidue.se.diamant.backend.backendservice.service.permissions.DenyAccessInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;

import java.io.InputStream;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ChallengeAttachmentControllerTest extends RestDocTest {

    @MockBean
    private GridFsOperations gridFsOperations;

    @MockBean
    private DenyAccessInterceptor denyAccessInterceptor;


    private static final String dummyUsername = "caller@example.com";

    @Test
    @WithMockUser(username = dummyUsername, roles = {"caller"})
    public void saveAttachment() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String fileName = "uploadedFile";
        String contentType = "image/jpeg";
        ObjectId savedAttachmentId = new ObjectId("5c6c25fe3accaf414c792f49");
        ArgumentCaptor<DBObject> metaDataCaptor = ArgumentCaptor.forClass(DBObject.class);

        Mockito.when(gridFsOperations.store(Mockito.any(InputStream.class), Mockito.eq(fileName), Mockito.eq(contentType), metaDataCaptor.capture())).thenReturn(savedAttachmentId);


        MockMultipartFile multipartFile = new MockMultipartFile("attachment", fileName, "image/jpeg", "content".getBytes());

        this.mockMvc.perform(
                multipart("/api/challenge/{challengeId}/attachment", challengeId)
                        .file(multipartFile)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(savedAttachmentId.toHexString()));


        Mockito.verify(denyAccessInterceptor, Mockito.only()).interceptUploadAttachmentToChallenge(Mockito.eq(challengeId), Mockito.any(Authentication.class));
        Mockito.verify(gridFsOperations, Mockito.only()).store(Mockito.any(InputStream.class), Mockito.eq(fileName), Mockito.eq(contentType), metaDataCaptor.capture());
        Assertions.assertThat(metaDataCaptor.getValue().get(AttachmentService.META_DATA_FIELD_CHALLENGE_ID)).isEqualTo(challengeId);
        Assertions.assertThat(metaDataCaptor.getValue().get(AttachmentService.META_DATA_FIELD_CONTENT_TYPE)).isEqualTo(contentType);
        Assertions.assertThat(metaDataCaptor.getValue().get(AttachmentService.META_DATA_FIELD_FILE_NAME)).isEqualTo(fileName);
        Assertions.assertThat(metaDataCaptor.getValue().get(AttachmentService.META_DATA_FIELD_TYPE)).isEqualTo(AttachmentService.AttachmentType.CHALLENGE_ATTACHMENT.toString());
    }

    @Test
    @WithMockUser(username = dummyUsername, roles = {"user"})
    public void saveAttachmentNotLoggedIn() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String fileName = "uploadedFile";

        MockMultipartFile multipartFile = new MockMultipartFile("attachment", fileName, "image/jpeg", "content".getBytes());
        this.mockMvc.perform(
                multipart("/api/challenge/{challengeId}/attachment", challengeId)
                        .file(multipartFile)
        )
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(StringUtils.EMPTY));

        Mockito.verifyNoInteractions(denyAccessInterceptor, gridFsOperations);
    }

    @Test
    public void getAttachmentWithoutUser() throws Exception {
        String dummyId = "5c6c25fe3accaf414c792f49";
        String dummyChallengeId = "5c617f7c0f4a6437b4f769fc";

        Document metadata = new Document();
        metadata.append(AttachmentService.META_DATA_FIELD_CHALLENGE_ID, dummyChallengeId);
        metadata.append(AttachmentService.META_DATA_FIELD_CONTENT_TYPE, "image/jpeg");
        GridFSFile dummyAttachment = new GridFSFile(new BsonString(dummyId), dummyId, 0, 0, new Date(), "md5", metadata);

        Mockito.when(gridFsOperations.findOne(Query.query(Criteria.where("_id").is(dummyId)))).thenReturn(dummyAttachment);

        GridFsResource dummyResource = new GridFsResource(dummyAttachment);
        Mockito.when(gridFsOperations.getResource(dummyAttachment)).thenReturn(dummyResource);

        this.mockMvc.perform(
                get("/api/challenge/{challengeId}/attachment/{attachmentId}", dummyChallengeId, dummyId)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline"));

        Mockito.verify(gridFsOperations, Mockito.times(1)).findOne(Query.query(Criteria.where("_id").is(dummyId)));
        Mockito.verify(gridFsOperations, Mockito.times(1)).getResource(dummyAttachment);
        Mockito.verify(denyAccessInterceptor, Mockito.times(1)).interceptViewChallenge(Mockito.eq(dummyChallengeId), Mockito.isNull());
    }
    @Test
    @WithMockUser(username = dummyUsername, roles = {"user"})
    public void getAttachmentWithUser() throws Exception {
        String dummyId = "5c6c25fe3accaf414c792f49";
        String dummyChallengeId = "5c617f7c0f4a6437b4f769fc";

        Document metadata = new Document();
        metadata.append(AttachmentService.META_DATA_FIELD_CHALLENGE_ID, dummyChallengeId);
        metadata.append(AttachmentService.META_DATA_FIELD_CONTENT_TYPE, "image/jpeg");
        GridFSFile dummyAttachment = new GridFSFile(new BsonString(dummyId), dummyId, 0, 0, new Date(), "md5", metadata);

        Mockito.when(gridFsOperations.findOne(Query.query(Criteria.where("_id").is(dummyId)))).thenReturn(dummyAttachment);

        GridFsResource dummyResource = new GridFsResource(dummyAttachment);
        Mockito.when(gridFsOperations.getResource(dummyAttachment)).thenReturn(dummyResource);

        this.mockMvc.perform(
                get("/api/challenge/{challengeId}/attachment/{attachmentId}", dummyChallengeId, dummyId)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline"));

        Mockito.verify(gridFsOperations, Mockito.times(1)).findOne(Query.query(Criteria.where("_id").is(dummyId)));
        Mockito.verify(gridFsOperations, Mockito.times(1)).getResource(dummyAttachment);
        Mockito.verify(denyAccessInterceptor, Mockito.times(1)).interceptViewChallenge(Mockito.eq(dummyChallengeId), Mockito.any(Authentication.class));
    }

}
