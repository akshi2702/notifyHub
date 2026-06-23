package com.notifyhub.notification_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notifyhub.notification_service.dto.CreateNotificationRequest;
import com.notifyhub.notification_service.dto.CreateNotificationResponse;
import com.notifyhub.notification_service.service.NotificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/notification")
public class NotificationController {
	@Autowired
	private NotificationService notificationService;
	
	@PostMapping
	public CreateNotificationResponse createNotification(@Valid @RequestBody CreateNotificationRequest createNotifReq) {
		return notificationService.createNotification(createNotifReq);
		
	}

}
