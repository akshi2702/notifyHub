package com.notifyhub.notification_service.dto;

import java.sql.Time;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;

import com.notifyhub.notification_service.enums.EventType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class CreateNotificationRequest {

	
    private Long notificationId ;
	
    @NotBlank(message = "requestId is mandatory")
    private String requestId;
	
    @NotNull(message = "eventType is mandatory")
	private EventType eventType;
	
    @Valid
    @NotNull(message = "recipientDetails is mandatory")
    private RecipientDto recipientDetails;
    
    @NotNull(message = "attributes is mandatory")
    private Map<String, String> attributes;
    
    @CreationTimestamp
    private Time createdAt;
}
