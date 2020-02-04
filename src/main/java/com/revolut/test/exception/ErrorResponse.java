package com.revolut.test.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
	
	private int code;
	private String messgae;

}
