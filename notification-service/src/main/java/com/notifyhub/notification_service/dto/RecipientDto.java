package com.notifyhub.notification_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecipientDto {

	@NotNull
	private String emailId;
	
	@NotNull
	private String phoneNumber;
}
