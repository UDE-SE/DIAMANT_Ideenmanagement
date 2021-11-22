package de.unidue.se.diamant.backend.backendservice.controller;

import de.unidue.se.diamant.backend.backendservice.controller.sse.SseChannel;
import de.unidue.se.diamant.backend.backendservice.controller.sse.SseEvent;
import lombok.extern.java.Log;
import org.springframework.context.ApplicationListener;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

@Log
@RestController
@RequestMapping(value = "/api")
public class ServerSendEventController {

    private CopyOnWriteArrayList<SseChannel> channelList = new CopyOnWriteArrayList<>();

    @GetMapping(path="/challenge/{challengeId}/idea/{ideaId}/sse", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String challengeId, @PathVariable String ideaId, @RequestHeader(value = "last-event-id", required = false) String lastEventId, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");

        String channelId = SseChannel.createChannelId(challengeId, ideaId);
        SseChannel correctChannel = null;
        for (SseChannel sseChannel : channelList) {
            if (sseChannel.getChannelId().equals(channelId)) {
                correctChannel = sseChannel;
                break;
            }
        }
        if (correctChannel == null) {
            correctChannel = new SseChannel(channelId);
            channelList.add(correctChannel);
        }
        SseEmitter subscribe = correctChannel.subscribe();
        if (StringUtils.isEmpty(lastEventId)) {
            try {
                long lastEventIdLong = Long.parseLong(lastEventId);
                correctChannel.sendAllEventsSinceGivenId(lastEventIdLong, subscribe);
            } catch (NumberFormatException formatException) {
                log.log(Level.WARNING, String.format("Es wurde versucht ein SSE-Listener zu erstellen, wobei die lastEventId '%s' angegeben wurde. Sie ist nicht gültig!", lastEventId));
            } catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage());
            }
        }
        return subscribe;
    }

    public void sendSseEvent(String challengeId, String ideaId, SseEvent.SseEventType eventType){
        String channelId = SseChannel.createChannelId(challengeId, ideaId);
        for (SseChannel channel : channelList) {
            if (channel.getChannelId().equals(channelId)) {
                channel.createAndSentEvent(channelId, eventType);
            }
        }
    }
}
