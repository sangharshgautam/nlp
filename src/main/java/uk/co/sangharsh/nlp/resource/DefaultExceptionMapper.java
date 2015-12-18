package uk.co.sangharsh.nlp.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {
//	private static final Logger LOG = LoggerFactory.getLogger(DefaultExceptionMapper.class);
	
	@Override
	public Response toResponse(Throwable exception) {
		exception.printStackTrace();
		ResponseBuilder builder;
		if (exception instanceof WebApplicationException) {
			WebApplicationException webAppExcept = (WebApplicationException) exception;
			builder = Response.fromResponse(webAppExcept.getResponse());
		} else {
//			LOG.warn("Internal server error (500)", exception);
			builder = Response.status(500).entity(exception);
		}
		return builder.type(MediaType.APPLICATION_JSON).build();
	}

}
