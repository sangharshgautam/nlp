package uk.co.sangharsh.nlp.service;

import java.util.List;

public interface NlpService {
	List<String> summarize(String document, int numOfSentences);
	
	public List<String> recognizeNamedEntity(String text);
}
