package uk.co.sangharsh.nlp.service;

public interface Summarizer {
	String summarize(String document, int numOfSentences);

	String test();
}
