package de.unidue.se.diamant.backend.backendservice.controller;

import de.unidue.se.diamant.backend.backendservice.service.AttachmentService;
import de.unidue.se.diamant.backend.backendservice.service.permissions.DenyAccessInterceptor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Log
@RestController
@RequestMapping(value = "/api")
public class IdeaAttachmentController extends AbstractAttachmentController {



    @Autowired
    public IdeaAttachmentController(GridFsOperations gridFsOperations, AttachmentService attachmentService, DenyAccessInterceptor denyAccessInterceptor) {
        super(gridFsOperations, attachmentService, denyAccessInterceptor);
    }

    /**
     * Speichern eines neuen Anhangs für eine Challenge.
     *
     * @param challengeId Id der Challenge, für den der neue Anhang gespeichert werden soll
     * @param ideaId Id der Idee
     * @param attachmentId Business-Id des Anhangs
     * @param attachment  Anhang
     * @return Id des gespeicherten Anhangs
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/challenge/{challengeId}/idea/{ideaId}/attachment/{attachmentId}")
    public ResponseEntity<String> saveIdeaAttachment(@PathVariable String challengeId,
                                                     @PathVariable String ideaId,
                                                     @PathVariable String attachmentId,
                                                     @RequestParam("attachment") MultipartFile attachment, Authentication authentication) throws IOException {
        denyAccessInterceptor.interceptUploadAttachmentToIdea(challengeId, ideaId, attachmentId, authentication);
        return ResponseEntity.ok(attachmentService.saveIdeaAttachment(attachmentId, challengeId, ideaId, attachment));
    }

    /**
     * Abfragen eines Anhangs
     * @param challengeId Id der Challenge, für die der Anhang abgefragt werden soll.
     * @param ideaId Id der Idee
     * @param attachmentId Technische Id des abzufragenden Anhangs
     * @return Anhang
     */
    @GetMapping("/challenge/{challengeId}/idea/{ideaId}/attachment/{attachmentId}")
    public ResponseEntity<GridFsResource> getIdeaAttachment(@PathVariable String challengeId, @PathVariable String ideaId, @PathVariable String attachmentId, Authentication authentication) {
        denyAccessInterceptor.interceptViewIdea(challengeId, ideaId, authentication);
        return getAttachmentById(attachmentId);
    }
}
