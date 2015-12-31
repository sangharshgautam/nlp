package uk.co.sangharsh.nlp.resource;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.sangharsh.nlp.resource.pojo.Conversation;
import uk.co.sangharsh.nlp.resource.pojo.Result;
import uk.co.sangharsh.nlp.resource.pojo.SummarizeRequest;
import uk.co.sangharsh.nlp.service.NlpService;

@Component
@Path(NlpResource.ROOT)
@Produces({MediaType.APPLICATION_JSON})
public class NlpResource {
	public static final String ROOT = "nlp";

	@Autowired
	private NlpService nlpService; 
	
	@GET
	@Path("summarize/{lines}")
	public Result<String> summarize(@PathParam(value = "lines") int lines, @QueryParam(value = "text") @NotBlank String text) {
		List<String> summary = nlpService.summarize(text, lines);
		return Result.ok(summary);
	}

	@POST
	@Path("summarize")
	@Consumes(MediaType.APPLICATION_JSON)
	public Result<String> summarize(SummarizeRequest request) {
		List<String> summary = nlpService.summarize(request.text(), request.lines());
		return Result.ok(summary);
	}
	@POST
	@Path("summarize/conversation/{lines}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Result<String> summarize(@PathParam(value = "lines") int lines, Conversation conversation) {
		List<String> summary = nlpService.summarize(conversation, lines);
		return Result.ok(summary);
	}
	
	@POST
	@Path("actionitems/conversation")
	@Consumes(MediaType.APPLICATION_JSON)
	public Result<String> actionitems(Conversation conversation) {
		List<String> summary = nlpService.actionitems(conversation);
		return Result.ok(summary);
	}
	
	@GET
	@Path("recognize/ne")
	public Result<String> recognizeNe(@QueryParam(value = "text") @NotBlank String text) {
		List<String> namedEntity = nlpService.recognizeNamedEntity(text);
		return Result.ok(namedEntity);
	}
	
}
