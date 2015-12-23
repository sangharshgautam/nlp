package uk.co.sangharsh.nlp.service;

import java.util.List;

import uk.co.sangharsh.nlp.resource.pojo.Conversation;

public interface NlpService {
	List<String> summarize(String document, int numOfSentences);
	
	public List<String> recognizeNamedEntity(String text);

	List<String> summarize(Conversation conversation, int lines);
}
