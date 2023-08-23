
package com.example.trying.ExceptionHandler;

public class CustomerFailureListException extends RuntimeException {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	public CustomerFailureListException(String message) {
		super(message);
	}
}