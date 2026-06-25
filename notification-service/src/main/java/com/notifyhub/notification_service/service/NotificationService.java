package com.notifyhub.notification_service.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.notifyhub.notification_service.dto.CreateNotificationRequest;
import com.notifyhub.notification_service.dto.CreateNotificationResponse;
import com.notifyhub.notification_service.dto.RecipientDto;
import com.notifyhub.notification_service.entity.NotificationDelivery;
import com.notifyhub.notification_service.entity.NotificationRequest;
import com.notifyhub.notification_service.entity.NotificationTemplate;
import com.notifyhub.notification_service.enums.ChannelType;
import com.notifyhub.notification_service.enums.DeliveryStatus;
import com.notifyhub.notification_service.exception.DuplicateRequestException;
import com.notifyhub.notification_service.exception.ValidationException;
import com.notifyhub.notification_service.repository.NotificationDeliveryRepository;
import com.notifyhub.notification_service.repository.NotificationRequestRepository;
import com.notifyhub.notification_service.repository.NotificationTemplateRepository;

@Service
public class NotificationService {

	@Autowired
	private NotificationRequestRepository reqRepo;

	@Autowired
	private NotificationTemplateRepository templateRepo;
	
	@Autowired
	private NotificationDeliveryRepository deliveryRepo;
	
	@Transactional
	public CreateNotificationResponse createNotification(CreateNotificationRequest createNotifReq) {

		CreateNotificationResponse notificationResp ;
		Optional<NotificationRequest> notifReqDbResponse = reqRepo.findByRequestId(createNotifReq.getRequestId());

		if (notifReqDbResponse.isPresent()){
			throw new DuplicateRequestException	("Duplicate Request");
		}
		else{
			//Get Active Template By EventType
			List<NotificationTemplate> templateResultset = templateRepo
					.findByEventTypeAndActiveTrue(createNotifReq.getEventType());
						
			if(templateResultset.isEmpty()) {
				throw new ValidationException("Invalid Event Type "+ createNotifReq.getEventType());
			}
			//Check Valid Attributes as per template in request
			validateRequiredAttributes(createNotifReq.getAttributes(), templateResultset);
			
			//Save Valid record in notification_request table
			NotificationRequest savedReqDbObject = saveNotifReqEntityObj(createNotifReq);
			
			notificationResp = new CreateNotificationResponse(savedReqDbObject.getNotificationId(), createNotifReq.getRequestId());

			//create Delivery record rows in notification_delivery
			saveNotificationDeliveryRows(templateResultset, savedReqDbObject.getNotificationId(), createNotifReq);
			
			
			
			//TODO
			
			//publish event to Kafka topics
		}
		return notificationResp;

	}

	public void validateRequiredAttributes(Map<String, String> requestAttrMap,
			List<NotificationTemplate> templateResultset) {
				
		for (NotificationTemplate templateRes : templateResultset) {
			List<String> requiredAttrList = templateRes.getRequiredAttributes();
			
			for (String requiredAttr : requiredAttrList) {
				String value = requestAttrMap.get(requiredAttr);

				if(value == null || value.isBlank()) {
					throw new ValidationException("Required Atrribute: "+ requiredAttr + " is missing in payload");
				}
			}
			
		}
	}
	
	public NotificationRequest saveNotifReqEntityObj(CreateNotificationRequest reqObj) {
		NotificationRequest reqDbObject = new NotificationRequest();

		reqDbObject.setRequestId(reqObj.getRequestId());
		reqDbObject.setEventType(reqObj.getEventType());
		reqDbObject.setRecipientDetails(reqObj.getRecipientDetails());
		reqDbObject.setAttributes(reqObj.getAttributes());
		
		return reqRepo.save(reqDbObject);
	}
	
	public List<NotificationDelivery> saveNotificationDeliveryRows(List<NotificationTemplate> templateResultset, long notificationId, CreateNotificationRequest reqObj) {
		
		List<NotificationDelivery> deliveryRows = new ArrayList<NotificationDelivery>();
		
		for (NotificationTemplate templateRes : templateResultset) {
			NotificationDelivery deliveryEntity = new NotificationDelivery();
			
			String msg = messageBuilder(reqObj.getAttributes(), templateRes.getTemplateText()) ;
			String recipient = deliveryRecipientBuilder(reqObj.getRecipientDetails(), templateRes.getChannel());

			deliveryEntity.setNotificationId(notificationId);
			deliveryEntity.setChannel(templateRes.getChannel());
			deliveryEntity.setRecipient(recipient);
			deliveryEntity.setMessage(msg);
			deliveryEntity.setStatus(DeliveryStatus.PENDING);
			deliveryRows.add(deliveryEntity);

		}
		return deliveryRepo.saveAll(deliveryRows);

	}
	
	public String messageBuilder(Map<String, String> reqAttributes, String templateText) {
		
		String message = templateText;
		
		for(Entry<String,String>reqAttrEntry : reqAttributes.entrySet()) {
			
			String placeholder = "{"+ reqAttrEntry.getKey() +"}";
			
			message = message.replace(placeholder, reqAttrEntry.getValue());
		}
		return message;
		
	}
	
	public String deliveryRecipientBuilder(RecipientDto reqRecipient, ChannelType tempChannel) {
		switch (tempChannel) {
			case EMAIL: {
				
				return reqRecipient.getEmailId();
			}
			case SMS: {
				
				return reqRecipient.getPhoneNumber();
			}
			default:
				throw new ValidationException("Unsupported Channel: " + tempChannel);
		} 	
	}

}
