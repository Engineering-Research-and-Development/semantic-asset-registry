package it.eng.rest.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class EventAdapterException extends WebApplicationException {

	public EventAdapterException(String message) {
		super(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity(message).type(MediaType.TEXT_PLAIN).build());
	}

	public EventAdapterException(Response.Status status, String message) {
		super(Response.status(status).entity(message)
				.type(MediaType.TEXT_PLAIN).build());
	}
}
