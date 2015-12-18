package me.foldl.corenlp_summarizer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.nlp.stats.Counter;

public class CounterWrapper implements Serializable{

	private Map<String, Counter<String>> counterMap = new HashMap<String, Counter<String>>();
	private final String fileName;

	public CounterWrapper(String fileName){
		this.fileName = fileName;
	}
	public Map<String, Counter<String>> counterMap() {
		return counterMap;
	}

	public String fileName() {
		return this.fileName+".ser";
	}
	
}