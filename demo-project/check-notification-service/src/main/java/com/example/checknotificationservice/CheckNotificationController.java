package com.example.checknotificationservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Random;

@RestController
@RequestMapping("/api/checkNotification")
public class CheckNotificationController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Value("${notification.service}")
    private String notificationService;

    @GetMapping("refresh")
    public String refresh() {
        logger.info("Refresh list notification");
        String response = restTemplate.getForObject("http://" + notificationService + ":8081/api/notification/newest", String.class);
        logger.info("Received new notification");
        return response;
    }

    @PostMapping("/create")
    public String create(@RequestBody Notification notification) {
        logger.info("Sending command to create new notification");
        ResponseEntity<String> responseEntity = restTemplate.postForEntity("http://" + notificationService + ":8081/api/notification/create", notification, String.class);
        return responseEntity.getBody();
    }

    @GetMapping("/show")
    public String show() {
        return "You have " + new Random().nextInt(10) + " unread notification";
    }
}
