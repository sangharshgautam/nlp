package uk.co.sangharsh.nlp.service;

import java.util.Comparator;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.CoreMap;

public class SentenceComparator implements Comparator<CoreMap> {


    private final Counter<String> termFrequencies;
	private final Counter<String> dfCounter;
	private final int numDocuments;
    public SentenceComparator(Counter<String> dfCounter, Counter<String> termFrequencies) {
    	this.dfCounter = dfCounter;
    	this.numDocuments = (int) dfCounter.getCount("__all__");
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
      for (CoreLabel cl : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
		String pos = cl.get(CoreAnnotations.PartOfSpeechAnnotation.class);
		if (PosUtil.isNoun(pos))
          total += tfIDFWeight(cl.get(CoreAnnotations.TextAnnotation.class));
	}

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
