package com.revolut.test.exception;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandler extends StatusService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandler.class);

	@Override
	public Status toStatus(final Throwable throwable, final Request request, final Response response) {
		LOGGER.warn("Exception thrown and caught, with throwable: " + throwable);
		return new Status(500, throwable.getMessage());
	}

	@Override
	public Representation toRepresentation(final Status status, final Request request, final Response response) {
		return toJson(status);
	}

	private Representation toJson(Status status) {
		return new JacksonRepresentation<ErrorResponse>(
				ErrorResponse.builder().code(status.getCode()).messgae(status.getReasonPhrase()).build());
	}
}
