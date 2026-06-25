package com.notifyhub.notification_service.exception;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.notifyhub.notification_service.enums.EventType;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ErrorResponse> handleValidationError(ValidationException ex) {
		ErrorResponse response = new ErrorResponse("VALIDATION_ERROR", ex.getMessage(), LocalDateTime.now());

		return new ResponseEntity<ErrorResponse>(response, HttpStatus.BAD_REQUEST);

	}

	@ExceptionHandler(DuplicateRequestException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateRequestError(DuplicateRequestException ex) {
		ErrorResponse response = new ErrorResponse("DUPLICATE_REQUEST", ex.getMessage(), LocalDateTime.now());

		return new ResponseEntity<ErrorResponse>(response, HttpStatus.CONFLICT);

	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleBeanValidationErr(MethodArgumentNotValidException ex) {

		String errMsg = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
		ErrorResponse resp = new ErrorResponse("VALIDATION_ERROR", errMsg, LocalDateTime.now());
		return new ResponseEntity<ErrorResponse>(resp, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleInvalidPayload(HttpMessageNotReadableException ex) {

		String errorMessage = "Invalid request payload";

		Throwable cause = ex.getCause();

		if (cause instanceof InvalidFormatException invalidFormatException) {

			if (invalidFormatException.getTargetType().equals(EventType.class)) {

				errorMessage = "Invalid eventType. Supported values are: " + Arrays.toString(EventType.values());
			}
		}

		ErrorResponse response = new ErrorResponse("VALIDATION_ERROR", errorMessage, LocalDateTime.now());

		return new ResponseEntity<ErrorResponse>(response, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
		System.out.println("INSIDE Generic Exception  HANDLER");

		ErrorResponse response = new ErrorResponse("INTERNAL_SERVER_ERROR", ex.getMessage(), LocalDateTime.now());

		return new ResponseEntity<ErrorResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);

	}

}
