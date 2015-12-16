package uk.co.sangharsh.nlp.resource;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.springframework.stereotype.Component;

@Component
@Path(NlpResource.ROOT)
public class NlpResource {
	public static final String ROOT = "nlp";

	@Path("summarize/{lines}")
	public String summarize(@PathParam("lines") int lines) {
		return "ABC";
	}
}
