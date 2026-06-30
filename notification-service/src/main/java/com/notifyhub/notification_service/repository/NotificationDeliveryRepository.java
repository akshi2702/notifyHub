package com.notifyhub.notification_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.notifyhub.notification_service.entity.NotificationDelivery;

@Repository
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, Long>{

}
