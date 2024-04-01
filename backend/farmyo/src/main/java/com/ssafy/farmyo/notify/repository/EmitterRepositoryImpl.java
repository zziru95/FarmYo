package com.ssafy.farmyo.notify.repository;

import com.ssafy.farmyo.entity.Notify;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class EmitterRepositoryImpl implements EmitterRepository{

    // 실시간 알림을 전송하기 위해 서버에서 SSE Emitter 관리
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    // 네트워크 오류 등으로 인한 알림 유실을 위한 캐시
    private final Map<String, Notify> eventCache = new ConcurrentHashMap<>();

    @Override
    public SseEmitter saveEmitter(String emitterId, SseEmitter emitter) {
        emitters.put(emitterId, emitter);
        log.info("Emitter Count : {}", emitters.size());
        return emitter;
    }

    @Override
    public void deleteEmitterById(String emitterId) {
        emitters.remove(emitterId);
    }

    @Override
    public void deleteEmittersByUserId(String userId) {
        emitters.forEach(
                (key, emitter)->{
                    if(key.startsWith(userId)){
                        emitters.remove(key);
                    }
                }
        );
    }

    @Override
    public void deleteEventCacheByUserId() {
        // 3개월이 지난 EventCache는 삭제
        eventCache.forEach(
                (key, noti) ->{
                    LocalDateTime createdTime = noti.getCreatedAt().plusMonths(3L); // 생성 후 3시간이 지난 알림
                    LocalDateTime now = LocalDateTime.now(); // 현재 시간
                    if(now.compareTo(createdTime) <= 0){ // 생성한 지 3개월이 지난 알림은 캐시에서 제거
                        eventCache.remove(noti);
                    }
                }
        );
    }

    @Override
    public Map<String, SseEmitter> findSseEmittersById(String userId) {
        return emitters.entrySet().stream()
                .filter(entry->entry.getKey().startsWith(userId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, Notify> findEventCachesByUserId(String userId) {
        return eventCache.entrySet().stream()
                .filter(entry->entry.getKey().startsWith(userId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void saveEventCache(String key, Notify noti){
        eventCache.put(key, noti);
    }

}
