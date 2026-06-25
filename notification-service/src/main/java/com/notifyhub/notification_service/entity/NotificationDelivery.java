package com.notifyhub.notification_service.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.notifyhub.notification_service.enums.ChannelType;
import com.notifyhub.notification_service.enums.DeliveryStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "notification_delivery")
@Data
public class NotificationDelivery {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long deliveryId ;
	
	@NotNull
	private Long notificationId;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	private ChannelType channel;
	
	@NotEmpty
	private String recipient;
	
	@NotEmpty
	@Column(columnDefinition = "TEXT")
	private String message;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	private DeliveryStatus status;
	
	@Column(length = 500)
	private String failureReason;
	
	@CreationTimestamp
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	private LocalDateTime updatedAt;

}
