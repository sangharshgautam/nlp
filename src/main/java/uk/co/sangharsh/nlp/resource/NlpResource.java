package uk.co.sangharsh.nlp.resource;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.sangharsh.nlp.resource.pojo.Result;
import uk.co.sangharsh.nlp.resource.pojo.SummarizeRequest;
import uk.co.sangharsh.nlp.service.NlpService;

@Component
@Path(NlpResource.ROOT)
@Produces({MediaType.APPLICATION_XML})
public class NlpResource {
	public static final String ROOT = "nlp";

	private static final String DEFAULT_TEXT= "India, officially the Republic of India (Bhārat Gaṇarājya),[18][19][c] is a country in South Asia. It is the seventh-largest country by area, the second-most populous country with over 1.2 billion people, and the most populous democracy in the world. India is a federal constitutional republic governed under a parliamentary system consisting of 29 states and 7 union territories. A pluralistic, multilingual, and multi-ethnic society, the country is also home to a diversity of wildlife in a variety of protected habitats. Bounded by the Indian Ocean on the south, the Arabian Sea on the south-west, and the Bay of Bengal on the south-east, it shares land borders with Pakistan to the west;[d] China, Nepal, and Bhutan to the north-east; and Myanmar (Burma) and Bangladesh to the east. In the Indian Ocean, India is in the vicinity of Sri Lanka and the Maldives; in addition, India's Andaman and Nicobar Islands share a maritime border with Thailand and Indonesia.Home to the ancient Indus Valley Civilisation and a region of historic trade routes and vast empires, the Indian subcontinent was identified with its commercial and cultural wealth for much of its long history.[20] Four major religions—Hinduism, Buddhism, Jainism, and Sikhism—originated here, whereas Zoroastrianism and the Abrahamic religions of Judaism, Christianity, and Islam arrived in the first millennium CE and also shaped the region's diverse culture. Gradually brought under the administration of the British East India Company from the early 18th century and administered directly by the United Kingdom after the Indian Rebellion of 1857, India became an independent nation in 1947 after a struggle for independence led by Mahatma Gandhi that was marked by non-violent resistance. Upon the promulgation of its constitution, India became a republic on 26 January 1950.Following market-based economic reforms in 1991, India became one of the fastest-growing major economies and is considered a newly industrialised country. The Indian economy is the world's seventh-largest by nominal GDP and third-largest by purchasing power parity (PPP).[14] It has the third-largest standing army in the world and ranks ninth in military expenditure among nations, while being recognised as a nuclear weapons state and regional power. However, it continues to face the challenges of widespread poverty, corruption, malnutrition and inadequate public health.";
	
	@Autowired
	private NlpService nlpService; 
	
	@GET
	@Path("summarize/{lines}")
	public Result summarize(@PathParam(value = "lines") int lines, @QueryParam(value = "text") @DefaultValue(value = DEFAULT_TEXT) String text) {
		List<String> summary = nlpService.summarize(text, lines);
		return Result.ok(summary);
	}

	@POST
	@Path("summarize/{lines}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Result summarize(SummarizeRequest request) {
		System.out.println(request);
		List<String> summary = nlpService.summarize(request.text(), request.lines());
		return Result.ok(summary);
		
	}
	@GET
	@Path("recognize/ne")
	public Result recognizeNe(@QueryParam(value = "text") @DefaultValue(value = DEFAULT_TEXT) String text) {
		List<String> namedEntity = nlpService.recognizeNamedEntity(text);
		return Result.ok(namedEntity);
	}
	
}
