package com.notifyhub.notification_service.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.notifyhub.notification_service.dto.CreateNotificationRequest;
import com.notifyhub.notification_service.dto.CreateNotificationResponse;
import com.notifyhub.notification_service.entity.NotificationRequest;
import com.notifyhub.notification_service.entity.NotificationTemplate;
import com.notifyhub.notification_service.exception.DuplicateRequestException;
import com.notifyhub.notification_service.exception.ValidationException;
import com.notifyhub.notification_service.repository.NotificationRequestRepository;
import com.notifyhub.notification_service.repository.NotificationTemplateRepository;

@Service
public class NotificationService {

	@Autowired
	private NotificationRequestRepository reqRepo;

	@Autowired
	private NotificationTemplateRepository templateRepo;
	
	public CreateNotificationResponse createNotification(CreateNotificationRequest createNotifReq) {

		Optional<NotificationRequest> notifReqDbResponse = reqRepo.findByRequestId(createNotifReq.getRequestId());

		if (notifReqDbResponse.isPresent()){
			throw new DuplicateRequestException	("Duplicate Request");
		}
		else{
			List<NotificationTemplate> templateResultset = templateRepo
					.findByEventTypeAndActiveTrue(createNotifReq.getEventType());
			
			System.out.println(templateResultset);
			
			if(templateResultset.isEmpty()) {
				throw new ValidationException("Invalid Event Type "+ createNotifReq.getEventType());
			}
			validateRequiredAttributes(createNotifReq.getAttributes(), templateResultset);
			//TODO
			//save record in notification_request table
			//create Deleivery record rows in notification_delivery
			//publish event to Kafka topics
		}
		return new CreateNotificationResponse(1L, createNotifReq.getRequestId());

	}

	public void validateRequiredAttributes(Map<String, String> requestAttrMap,
			List<NotificationTemplate> templateResultset) {
				
		for (NotificationTemplate templateRes : templateResultset) {
			List<String> requiredAttrList = templateRes.getRequiredAttributes();
			
			for (String requiredAttr : requiredAttrList) {
		
				if (!requestAttrMap.containsKey(requiredAttr)) {
					throw new ValidationException("Required Atrribute: "+ requiredAttr + " is missing in payload");
				}
			}
			
		}
	}

}
