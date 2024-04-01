package com.ssafy.farmyo.notify.service;

import com.ssafy.farmyo.entity.Notify;
import com.ssafy.farmyo.entity.User;
import com.ssafy.farmyo.notify.repository.EmitterRepository;
import com.ssafy.farmyo.notify.repository.NotifyRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@Service
public class NotifyService {
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    // SSE 연결 지속 시간 설정

    private final EmitterRepository emitterRepository;
    private final NotifyRepository notifyRepository;

    public NotifyService(EmitterRepository emitterRepository, NotifyRepository notifyRepository) {
        this.emitterRepository = emitterRepository;
        this.notifyRepository = notifyRepository;
    }

    // [1] subscribe()
    public SseEmitter subscribe(String username, String lastEventId) {
        // SSE Emitter ID 생성
        String emitterId = makeTimeIncludeId(username);

        // SseEmitter 객체를 생성하고, emitterId를 키로 사용해 저장
        SseEmitter emitter = emitterRepository.saveEmitter(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        //
        emitter.onCompletion(() -> emitterRepository.deleteEmitterById(emitterId));
        emitter.onTimeout(() -> emitterRepository.deleteEmitterById(emitterId));

        // (1-5) 503 에러를 방지하기 위한 더미 이벤트 전송
        String eventId = makeTimeIncludeId(username);
        sendNotification(emitter, eventId, emitterId, "EventStream Created. [userEmail=" + username + "]");

        // (1-6) 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방
        if (hasLostData(lastEventId)) {
            sendLostData(lastEventId, username, emitterId, emitter);
        }

        return emitter; // (1-7)
    }

    private String makeTimeIncludeId(String email) { // (3)
        return email + "_" + System.currentTimeMillis();
    }

    private void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) { // (4)
        try {
            emitter.send(SseEmitter.event()
                    .id(eventId)
                    .name("sse")
                    .data(data)
            );
        } catch (IOException exception) {
            emitterRepository.deleteEmitterById(emitterId);
        }
    }

    private boolean hasLostData(String lastEventId) { // (5)
        return !lastEventId.isEmpty();
    }

    private void sendLostData(String lastEventId, String userEmail, String emitterId, SseEmitter emitter) { // (6)
        Map<String, Notify> eventCaches = emitterRepository.findEventCachesByUserId(String.valueOf(userEmail));
        eventCaches.entrySet().stream()
                .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
    }

    // [2] send()
    //@Override
    public void send(User receiver, Notify.NotificationType notificationType, String content, String url) {
        Notify notification = notifyRepository.save(createNotification(receiver, notificationType, content, url)); // (2-1)
        String receiverEmail = receiver.getEmail(); // (2-2)
        String eventId = receiverEmail + "_" + System.currentTimeMillis(); // (2-3)
        Map<String, SseEmitter> emitters = emitterRepository.findSseEmittersById(receiverEmail); // (2-4)
        emitters.forEach( // (2-5)
                (key, emitter) -> {
                    emitterRepository.saveEventCache(key, notification);
                    sendNotification(emitter, eventId, key, "NotifyDto.Response.createResponse(notification)");
                }
        );
    }

    private Notify createNotification(User receiver, Notify.NotificationType notificationType, String content, String url) { // (7)
        return Notify.builder()
                .receiver(receiver)
                .notificationType(notificationType)
                .content(content)
                .url(url)
                .isRead(false)
                .build();
    }
}