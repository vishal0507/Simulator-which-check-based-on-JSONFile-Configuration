package com.example.trying.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {
	@ExceptionHandler(value = CustomeApiNameException.class)
	public ResponseEntity<Map<String, String>> handleApiNameException(CustomeApiNameException e) {
		Map<String, String> response = new HashMap<>();
		response.put("Status", "01");
		response.put("Description", "There is no apiname.");
		response.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	@ExceptionHandler(value = CustomeSuccessException.class)
	public ResponseEntity<Map<String, String>> handleSuccessException(CustomeSuccessException e) {
		Map<String, String> response = new HashMap<>();
		response.put("Status", "01");
		response.put("Description", "There is no success field in given apiname.");
		response.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	@ExceptionHandler(value = CustomerFailureException.class)
	public ResponseEntity<Map<String, String>> handleFailureException(CustomerFailureException e) {
		Map<String, String> response = new HashMap<>();
		response.put("Status", "01");
		response.put("Description", "There is no failure field in given apiname.");
		response.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	@ExceptionHandler(value = CustomerFailureListException.class)
	public ResponseEntity<Map<String, String>> handleFailureListException(CustomerFailureListException e) {
		Map<String, String> response = new HashMap<>();
		response.put("Status", "01");
		response.put("Description", "There is no failure list field in given apiname.");
		response.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	@ExceptionHandler(value = CustomeRandomFieldsException.class)
	public ResponseEntity<Map<String, String>> handleRandomFieldsException(CustomeRandomFieldsException e) {
		Map<String, String> response = new HashMap<>();
		response.put("Status", "01");
		response.put("Description", "There is no random list field in given apiname.");
		response.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	@ExceptionHandler(value = FailureTestOnException.class)
	public ResponseEntity<Map<String, String>> handleRandomFieldsException(FailureTestOnException e) {
		Map<String, String> response = new HashMap<>();
		response.put("Status", "01");
		response.put("Description", "There is no failureTest on field in given apiname.");
		response.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

}