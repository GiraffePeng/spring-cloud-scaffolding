package com.peng.userservice.server;

public class UserLoginException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserLoginException(String message) {
        super(message);
    }
}
