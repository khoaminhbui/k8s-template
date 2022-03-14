package com.example.notificationservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${app.version}")
    private String appVersion;

    @GetMapping("newest")
    public String getNewestNotification() {
        LocalDateTime now = LocalDateTime.now();
        logger.info("Getting notification check at {}", now);
        return "Getting new notification at " + now + " from app version " + appVersion;
    }

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody Notification notification) {
        logger.info("--------------- Receiving create notification command ---------------");
        if (notification.getId() <= 10) {
            logger.error("--------------- Creating new notification error with id <= 10 ---------------");
            return new ResponseEntity<>("Error when creating new notification with id " + notification.getId(), HttpStatus.BAD_REQUEST);
        }
        LocalDateTime now = LocalDateTime.now();
        logger.info("--------------- Created new notification at " + now + " ---------------");
        return ResponseEntity.ok("Created new notification at " + now);
    }
}
