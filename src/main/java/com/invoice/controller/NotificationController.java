package com.invoice.controller;

import com.invoice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;

    @GetMapping(produces = "application/json", value = "/email")
    public ResponseEntity<Void> sendEmail()
    {
        log.info("request: NotificationController sendEmail()");
        emailService.sendEmails();

        log.info("request: NotificationController sendEmail()");
        return ResponseEntity.ok().build();
    }
}
