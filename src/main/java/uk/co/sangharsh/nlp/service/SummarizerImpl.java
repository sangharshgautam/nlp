package uk.co.sangharsh.nlp.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.CoreMap;

@Service
public class SummarizerImpl implements Summarizer {

	private static final String DF_COUNTER_PATH = "df-counts.ser";
	
	private StanfordCoreNLP pipeline;

	private Counter<String> dfCounter;
	private int numDocuments;
	 
	@PostConstruct
	public void setNlp() throws ClassNotFoundException, IOException {
		this.dfCounter = loadDfCounter(DF_COUNTER_PATH);
	    this.numDocuments = (int) dfCounter.getCount("__all__");
	    
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,ssplit,pos");
		props.setProperty("tokenize.language", "en");
		props.setProperty(
				"pos.model",
				"edu/stanford/nlp/models/pos-tagger/english/english-left3words-distsim.tagger");

		pipeline = new StanfordCoreNLP(props);
	}

	private static Counter<String> loadDfCounter(String path)throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
		Counter<String> readObject = (Counter<String>) ois.readObject();
		ois.close();
		return readObject;
	}
	@Override
	public String summarize(String document, int numSentences) {
		Annotation annotation = pipeline.process(document);
		List<CoreMap> sentences = annotation
				.get(CoreAnnotations.SentencesAnnotation.class);

		Counter<String> tfs = getTermFrequencies(sentences);
		sentences = rankSentences(sentences, tfs);

		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < numSentences; i++) {
			ret.append(sentences.get(i));
			ret.append(" ");
		}

		return ret.toString();
	}

	private static Counter<String> getTermFrequencies(List<CoreMap> sentences) {
		Counter<String> ret = new ClassicCounter<String>();

		for (CoreMap sentence : sentences)
			for (CoreLabel cl : sentence
					.get(CoreAnnotations.TokensAnnotation.class))
				ret.incrementCount(cl.get(CoreAnnotations.TextAnnotation.class));

		return ret;
	}

	private List<CoreMap> rankSentences(List<CoreMap> sentences,
			Counter<String> tfs) {
		Collections.sort(sentences, new SentenceComparator(tfs));
		return sentences;
	}
	private class SentenceComparator implements Comparator<CoreMap> {

	    private final Counter<String> termFrequencies;

	    public SentenceComparator(Counter<String> termFrequencies) {
	      this.termFrequencies = termFrequencies;
	    }

	    @Override
	    public int compare(CoreMap o1, CoreMap o2) {
	      return (int) Math.round(score(o2) - score(o1));
	    }

	    /**
	     * Compute sentence score (higher is better).
	     */
	    private double score(CoreMap sentence) {
	      double tfidf = tfIDFWeights(sentence);

	      // Weight by position of sentence in document
	      int index = sentence.get(CoreAnnotations.SentenceIndexAnnotation.class);
	      double indexWeight = 5.0 / index;

	      return indexWeight * tfidf * 100;
	    }

	    private double tfIDFWeights(CoreMap sentence) {
	      double total = 0;
	      for (CoreLabel cl : sentence.get(CoreAnnotations.TokensAnnotation.class))
	        if (cl.get(CoreAnnotations.PartOfSpeechAnnotation.class).startsWith("n"))
	          total += tfIDFWeight(cl.get(CoreAnnotations.TextAnnotation.class));

	      return total;
	    }

	    private double tfIDFWeight(String word) {
	      if (dfCounter.getCount(word) == 0)
	        return 0;

	      double tf = 1 + Math.log(termFrequencies.getCount(word));
	      double idf = Math.log(numDocuments / (1 + dfCounter.getCount(word)));
	      return tf * idf;
	    }
	  
	}
}
