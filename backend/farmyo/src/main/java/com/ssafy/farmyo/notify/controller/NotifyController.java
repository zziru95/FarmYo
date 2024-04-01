package com.ssafy.farmyo.notify.controller;

import com.ssafy.farmyo.common.auth.CustomUserDetails;
import com.ssafy.farmyo.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notify")
@Tag(name="8.Notify", description="Notify API")
public class NotifyController {

//    private final NotifyService notifyService;

    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public void subscribe(Authentication authentication,
                          @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        log.info("{}",  userDetails.getId());

//        return notifyService.subscribe(principal.getUsername(), lastEventId);

    }
}
