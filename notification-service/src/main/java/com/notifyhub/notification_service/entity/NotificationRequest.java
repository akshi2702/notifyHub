package com.notifyhub.notification_service.entity;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.notifyhub.notification_service.dto.RecipientDto;
import com.notifyhub.notification_service.enums.EventType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "notification_request")
@Data
public class NotificationRequest {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long notificationId ;
	
	@NotBlank
    private String requestId;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	private EventType eventType;
	
	@NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    private RecipientDto recipientDetails;
    
    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> attributes;
    
    @CreationTimestamp
    private LocalDateTime createdAt;

}
