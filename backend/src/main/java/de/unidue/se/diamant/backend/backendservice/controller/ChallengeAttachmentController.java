package de.unidue.se.diamant.backend.backendservice.controller;

import de.unidue.se.diamant.backend.backendservice.service.AttachmentService;
import de.unidue.se.diamant.backend.backendservice.service.permissions.DenyAccessInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping(value = "/api")
public class ChallengeAttachmentController extends AbstractAttachmentController {


    @Autowired
    public ChallengeAttachmentController(DenyAccessInterceptor denyAccessInterceptor, AttachmentService attachmentService, GridFsOperations gridFsOperations) {
        super(gridFsOperations, attachmentService, denyAccessInterceptor);
    }


    /**
     * Speichern eines neuen Anhangs für eine Challenge.
     *
     * @param challengeId Id der Challenge, für den der neue Anhang gespeichert werden soll
     * @param attachment  Anhang
     * @return Id des gespeicherten Anhangs
     */
    @PreAuthorize("hasRole('caller')")
    @PostMapping("/challenge/{challengeId}/attachment")
    public ResponseEntity<String> saveChallengeAttachment(@PathVariable String challengeId, @RequestParam("attachment") MultipartFile attachment, Authentication authentication) throws IOException {
        denyAccessInterceptor.interceptUploadAttachmentToChallenge(challengeId, authentication);
        String attachmentId = attachmentService.saveChallengeAttachment(challengeId, attachment);
        return ResponseEntity.ok(attachmentId);
    }

    /**
     * Abfragen eines Anhangs
     *
     * @param challengeId  Id der Challenge, für die der Anhang abgefragt werden soll.
     * @param attachmentId Id des abzufragenden Anhangs
     * @return Anhang
     */
    @GetMapping("/challenge/{challengeId}/attachment/{attachmentId}")
    public ResponseEntity<GridFsResource> getChallengeAttachment(@PathVariable String challengeId, @PathVariable String attachmentId, Authentication authentication){
        denyAccessInterceptor.interceptViewChallenge(challengeId, authentication);
        return getAttachmentById(attachmentId);
    }
}
