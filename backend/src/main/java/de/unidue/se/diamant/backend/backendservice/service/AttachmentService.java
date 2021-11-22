package de.unidue.se.diamant.backend.backendservice.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.GridFSFindIterable;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttachmentService {

    public enum AttachmentType {
        CHALLENGE_ATTACHMENT,
        IDEA_ATTACHMENT
    }

    public static final String META_DATA_FIELD_TYPE = "attachmentType";
    public static final String META_DATA_FIELD_CHALLENGE_ID = "challengeId";
    public static final String META_DATA_FIELD_IDEA_ID = "ideaId";
    public static final String META_DATA_FIELD_ATTACHMENT_BUSINESS_ID = "businessId";
    public static final String META_DATA_FIELD_CONTENT_TYPE = "contentType";
    public static final String META_DATA_FIELD_FILE_NAME = "fileName";

    private final GridFsOperations gridFsOperations;

    @Autowired
    public AttachmentService(GridFsOperations gridFsOperations) {
        this.gridFsOperations = gridFsOperations;
    }


    /**
     * Speichert einen Anhang und gibt seine ID zurück
     *
     * @param challengeId Challenge zu der der Anhang gehört
     * @param attachment  Anhang
     * @return Id des gespeicherten Anhangs
     */
    public String saveChallengeAttachment(String challengeId, MultipartFile attachment) throws IOException {
        DBObject metaData = new BasicDBObject();
        metaData.put(META_DATA_FIELD_CHALLENGE_ID, challengeId);
        metaData.put(META_DATA_FIELD_FILE_NAME, attachment.getOriginalFilename());
        metaData.put(META_DATA_FIELD_CONTENT_TYPE, attachment.getContentType());
        metaData.put(META_DATA_FIELD_TYPE, AttachmentType.CHALLENGE_ATTACHMENT.toString());
        return gridFsOperations.store(attachment.getInputStream(), attachment.getOriginalFilename(), attachment.getContentType(), metaData).toHexString();
    }

    public GridFSFindIterable getChallengeAttachmentsByChallengeId(String challengeId) {
        return gridFsOperations.find(
                Query.query(
                        Criteria.where("metadata." + META_DATA_FIELD_CHALLENGE_ID).is(challengeId)
                                .andOperator(
                                        Criteria.where("metadata." + META_DATA_FIELD_TYPE).is(AttachmentType.CHALLENGE_ATTACHMENT.toString())
                                )
                ));
    }

    /**
     * Speichert den Anhang mit einer (fachlichen) ID für eine bestimmte Idee
     *
     * @param attachmentBusinessId Fachliche Id des Anhangs
     * @param challengeId          Id der Challenge zu der der Anhang (bzw. die Idee) gehört
     * @param ideaId               Id der Idee zu der der Anhang gehört
     * @param attachment           Anhang
     * @return Id des gespeicherten Anhangs
     */
    public String saveIdeaAttachment(String attachmentBusinessId, String challengeId, String ideaId, MultipartFile attachment) throws IOException {
        deleteIdeaAttachmentByIdeaIdAndBusinessId(ideaId, attachmentBusinessId);

        DBObject metaData = new BasicDBObject();
        metaData.put(META_DATA_FIELD_CHALLENGE_ID, challengeId);
        metaData.put(META_DATA_FIELD_IDEA_ID, ideaId);
        metaData.put(META_DATA_FIELD_ATTACHMENT_BUSINESS_ID, attachmentBusinessId);
        metaData.put(META_DATA_FIELD_FILE_NAME, attachment.getOriginalFilename());
        metaData.put(META_DATA_FIELD_CONTENT_TYPE, attachment.getContentType());
        metaData.put(META_DATA_FIELD_TYPE, AttachmentType.IDEA_ATTACHMENT.toString());
        return gridFsOperations.store(attachment.getInputStream(), attachment.getOriginalFilename(), attachment.getContentType(), metaData).toHexString();
    }

    private void deleteIdeaAttachmentByIdeaIdAndBusinessId(String ideaId, String attachmentBusinessId){
        gridFsOperations.delete(
                Query.query(
                        Criteria.where("metadata." + META_DATA_FIELD_IDEA_ID).is(ideaId)
                                .andOperator(
                                        Criteria.where("metadata." + META_DATA_FIELD_TYPE).is(AttachmentType.IDEA_ATTACHMENT.toString()),
                                        Criteria.where("metadata." + META_DATA_FIELD_ATTACHMENT_BUSINESS_ID).is(attachmentBusinessId)
                                )
                ));
    }

    public GridFSFindIterable findIdeaAttachmentsByIdeaId(String ideaId) {
        return gridFsOperations.find(
                Query.query(
                        Criteria.where("metadata." + META_DATA_FIELD_IDEA_ID).is(ideaId)
                                .andOperator(
                                        Criteria.where("metadata." + META_DATA_FIELD_TYPE).is(AttachmentType.IDEA_ATTACHMENT.toString())
                                )
                ));
    }

    /**
     * Alle CanvasElemente löschen, deren Ids nicht mehr verwendet werden
     */
    public void deleteAllUnreferencedAttachments(String ideaId, List<String> stillExistingCanvasElementIds) {
        gridFsOperations.delete(
                Query.query(
                        Criteria.where("metadata." + META_DATA_FIELD_IDEA_ID).is(ideaId)
                                .andOperator(
                                        Criteria.where("metadata." + META_DATA_FIELD_TYPE).is(AttachmentType.IDEA_ATTACHMENT.toString()),
                                        Criteria.where("metadata." + META_DATA_FIELD_ATTACHMENT_BUSINESS_ID).not().in(stillExistingCanvasElementIds)
                                )
                ));
    }

    /**
     * Alle CanvasElemente löschen, deren Ids nicht mehr verwendet werden
     */
    public void deleteAllUnreferencedChallengeAttachments(String challengeId, List<String> stillExistingAttachmentIds) {
        gridFsOperations.delete(
                Query.query(
                        Criteria.where("metadata." + META_DATA_FIELD_CHALLENGE_ID).is(challengeId)
                                .andOperator(
                                        Criteria.where("_id").not().in(stillExistingAttachmentIds.stream().map(ObjectId::new).collect(Collectors.toList()))
                                )
                ));
    }
}
