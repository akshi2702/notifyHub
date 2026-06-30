package com.notifyhub.notification_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecipientDto {

	@Email(message = "Enter valid emailId")
	private String emailId;
	
	@NotBlank(message = "Enter valid phonenumber")
	private String phoneNumber;
}
