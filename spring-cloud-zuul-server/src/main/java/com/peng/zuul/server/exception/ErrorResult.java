package com.peng.zuul.server.exception;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResult implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer code;
	
	private String msg;
}
