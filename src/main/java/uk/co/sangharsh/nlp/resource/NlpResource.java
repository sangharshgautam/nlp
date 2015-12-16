package uk.co.sangharsh.nlp.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.sangharsh.nlp.service.Summarizer;

@Component
@Path(NlpResource.ROOT)
public class NlpResource {
	public static final String ROOT = "nlp";

	@Autowired
	private Summarizer summarizer; 
	
	@GET
	@Path("summarize")
	public String summarize() {
		return summarizer.test();
	}
}
