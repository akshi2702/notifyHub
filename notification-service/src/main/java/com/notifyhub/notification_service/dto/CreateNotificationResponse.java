package com.notifyhub.notification_service.dto;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateNotificationResponse {

	private Long notificationId;
	private String requestId;
}
