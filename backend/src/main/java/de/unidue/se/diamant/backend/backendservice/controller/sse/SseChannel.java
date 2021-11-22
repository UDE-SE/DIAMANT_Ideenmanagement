package de.unidue.se.diamant.backend.backendservice.controller.sse;

import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event;

@Log
public class SseChannel {
    @Getter
    private final String channelId;
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final Deque<SseEvent> eventsQueue = new ArrayDeque<>(30);

    public SseChannel(String channelId) {
        this.channelId = channelId;
        log.info(String.format("New SSE-Channel for id '%s' created.", channelId));
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(60_000L);
        this.emitters.add(emitter);
        ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();
        sseMvcExecutor.execute(() -> {
            try {
                for (int i = 0; true; i++) {
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                            .data("Heartbeat@" + LocalTime.now().toString())
                            .id(String.valueOf(i))
                            .name("Heartbeat");
                    emitter.send(event);
                    Thread.sleep(5* 1000);
                }
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        return emitter;
    }

    public void createAndSentEvent(String channelId, SseEvent.SseEventType eventType) {
        SseEvent peek = eventsQueue.peekLast();
        long id = (peek == null ? 1 : (peek.getId() + 1));
        SseEvent sse = new SseEvent(id, channelId, eventType);
        eventsQueue.add(sse);
        sentEvent(sse);
    }

    public void sendAllEventsSinceGivenId(long lastKnownId, SseEmitter emitter) throws IOException {
        for (SseEvent sse : eventsQueue) {
            if (sse.getId() > lastKnownId) {
                emitter.send(event().name("message").data(sse.getField().toString()).id(String.valueOf(sse.getId())));
            }
        }
    }

    public static String createChannelId(String challengeId, String ideaId) {
        return String.format("%s@%s", challengeId, ideaId);
    }

    private void sentEvent(SseEvent sse) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        this.emitters.forEach(emitter -> {
            try {
                emitter.send(event().name("message").data(sse.getField().toString()).id(String.valueOf(sse.getId())));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        });

        this.emitters.removeAll(deadEmitters);
    }
}
