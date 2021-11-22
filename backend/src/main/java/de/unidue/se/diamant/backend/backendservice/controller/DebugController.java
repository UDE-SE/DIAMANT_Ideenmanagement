package de.unidue.se.diamant.backend.backendservice.controller;

import de.unidue.se.diamant.backend.backendservice.dto.debug.DebugDateTimeDTO;
import de.unidue.se.diamant.backend.backendservice.service.time.TimeService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Log
@RestController
@RequestMapping(value = "/api")
public class DebugController {

    @Value("${app.secret.admin.password}")
    private String secretAdminPassword;

    private TimeService timeService;

    @Autowired
    public DebugController(TimeService timeService) {
        this.timeService = timeService;
    }

    @PostMapping("/debug/servertime/set")
    public Long setServertime(@RequestBody @Valid DebugDateTimeDTO debugDateTimeDTO) throws ParseException {
        if(! secretAdminPassword.equals(debugDateTimeDTO.getPassword())){
            throw new AccessDeniedException("Falsches Passwort..");
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        timeService.setDateTime(simpleDateFormat.parse(debugDateTimeDTO.getDateTime()));
        return timeService.getTime().getTime();
    }

    @PostMapping("/debug/servertime/reset")
    public Long resetServertime(@RequestBody @Valid DebugDateTimeDTO debugDateTimeDTO) {
        if(! secretAdminPassword.equals(debugDateTimeDTO.getPassword())){
            throw new AccessDeniedException("Falsches Passwort..");
        }
        timeService.setDateTime(null);
        return timeService.getTime().getTime();
    }
}
