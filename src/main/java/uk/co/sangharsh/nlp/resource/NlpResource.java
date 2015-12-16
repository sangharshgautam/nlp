package uk.co.sangharsh.nlp.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.springframework.stereotype.Component;

@Component
@Path(NlpResource.ROOT)
public class NlpResource {
	public static final String ROOT = "nlp";

	@GET
	@Path("summarize")
	public String summarize() {
		return "ABC";
	}
}
