package com.notifyhub.notification_service.entity;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.notifyhub.notification_service.enums.ChannelType;
import com.notifyhub.notification_service.enums.EventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "notification_template")
@Data
public class NotificationTemplate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long templateId;
	
	@NotNull
	@Enumerated(EnumType.STRING)
    private EventType eventType;
    
	@NotNull
	@Enumerated(EnumType.STRING)
    private ChannelType channel;
	
	@Column(columnDefinition = "TEXT")
    private String templateText;
    
    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> requiredAttributes;
    
    @NotNull
    private Boolean active;
    
    @NotNull
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @NotNull
    @UpdateTimestamp
    private LocalDateTime updatedAt;
	

}
