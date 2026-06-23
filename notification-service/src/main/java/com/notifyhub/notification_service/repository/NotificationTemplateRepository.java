package com.notifyhub.notification_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.notifyhub.notification_service.entity.NotificationTemplate;
import com.notifyhub.notification_service.enums.EventType;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Integer> {

	List<NotificationTemplate> findByEventTypeAndActiveTrue(EventType eventType) ;
	
	
}
