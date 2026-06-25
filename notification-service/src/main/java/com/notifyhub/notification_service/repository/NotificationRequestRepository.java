package com.notifyhub.notification_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.notifyhub.notification_service.entity.NotificationRequest;

@Repository
@Component
public interface NotificationRequestRepository extends JpaRepository<NotificationRequest, Integer>{
	
	Optional<NotificationRequest>findByRequestId(String requestId);
	
	

}
