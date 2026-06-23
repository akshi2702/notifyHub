package com.notifyhub.notification_service.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
	private String errorCode;
	
	private String message;
	
	private LocalDateTime timestamp;

}
