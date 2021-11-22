package de.unidue.se.diamant.backend.backendservice.controller;

import com.mongodb.client.gridfs.model.GridFSFile;
import de.unidue.se.diamant.backend.backendservice.dto.ResourceNotFoundException;
import de.unidue.se.diamant.backend.backendservice.service.AttachmentService;
import de.unidue.se.diamant.backend.backendservice.service.permissions.DenyAccessInterceptor;
import lombok.extern.java.Log;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.logging.Level;

@Log
public abstract class AbstractAttachmentController {

    private final GridFsOperations gridFsOperations;
    protected AttachmentService attachmentService;
    protected DenyAccessInterceptor denyAccessInterceptor;

    protected AbstractAttachmentController(GridFsOperations gridFsOperations, AttachmentService attachmentService, DenyAccessInterceptor denyAccessInterceptor) {
        this.gridFsOperations = gridFsOperations;
        this.attachmentService = attachmentService;
        this.denyAccessInterceptor = denyAccessInterceptor;
    }

    public ResponseEntity<GridFsResource> getAttachmentById(String attachmentId) {
        GridFSFile attachment = gridFsOperations.findOne(Query.query(Criteria.where("_id").is(attachmentId)));
        if (attachment == null) {
            String error = String.format("Kein Anhang mit der Id '%s' vorhanden.", attachmentId);
            log.log(Level.WARNING, error);
            throw new ResourceNotFoundException(error);
        }

        assert attachment.getMetadata() != null;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentLength(attachment.getLength())
                .contentType(MediaType.parseMediaType((String) attachment.getMetadata().get(AttachmentService.META_DATA_FIELD_CONTENT_TYPE)))
                .body(gridFsOperations.getResource(attachment));
    }
}
