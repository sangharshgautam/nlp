package uk.co.sangharsh.nlp.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import uk.co.sangharsh.nlp.resource.pojo.Conversation;
import uk.co.sangharsh.nlp.resource.pojo.Utterance;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.util.CoreMap;

@Service
public class NlpServiceImpl implements NlpService {

	private static final String NLP_RESOURCE = "/var/lib/openshift/5671bd132d527185c6000072/app-root/repo/nlp-resources/";

	private static final String DF_COUNTER_PATH = NLP_RESOURCE+"df-counts.ser";
	
	private StanfordCoreNLP pipeline;

	private Counter<String> dfCounter;
	
	private static final String SERIALIZED_CLASSIFIER = NLP_RESOURCE + "classifiers/english.all.3class.distsim.crf.ser.gz";
	 
	@PostConstruct
	public void setNlp() throws ClassNotFoundException, IOException {
		this.dfCounter = ObjectUtil.loadObjectNoExceptions(DF_COUNTER_PATH, Counter.class);
	    
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,ssplit,pos");
		props.setProperty("tokenize.language", "en");
		props.setProperty(
				"pos.model",
				"edu/stanford/nlp/models/pos-tagger/english/english-left3words-distsim.tagger");

		pipeline = new StanfordCoreNLP(props);
	}

	@Override
	public List<String> summarize(String document, int numSentences) {
		List<String> result = new ArrayList<String>();
		Annotation annotation = pipeline.process(document);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

		Counter<String> tfs = getTermFrequencies(sentences);
		sentences = rankSentences(sentences, tfs);
		int max = Math.min(numSentences, sentences.size());
		for (CoreMap sentence: sentences.subList(0, max-1)) {
			result.add(sentence.toString());
		}
		return result;
	}
	private List<CoreLabel> makeSentence(String sentence) {
	    String[] words = sentence.split(" ");
	    List<CoreLabel> labels = new ArrayList<CoreLabel>();
	    for (String word : words) {
	      CoreLabel label = new CoreLabel();
	      label.setWord(word);
	      label.setValue(word);
	      labels.add(label);
	    }
	    return labels;
	  }
	@Override
	public List<String> summarize(Conversation conversation, int numSentences) {
		/*List<String> result = new ArrayList<String>();
		List<CoreMap> sentences = new ArrayList<>();
		for(Utterance utterance : conversation.utterances()){
			CoreMap map = new ArrayCoreMap(1);
			String string = new StringBuilder().append(utterance.speaker()).append(" said \"").append(utterance.text()).append("\" ").toString();
			map.set(CoreAnnotations.TokensAnnotation.class, makeSentence(string));
			sentences.add(map);
		}
		Counter<String> tfs = getTermFrequencies(sentences);
		sentences = rankSentences(sentences, tfs);
		int max = Math.min(numSentences, sentences.size());
		for (CoreMap sentence: sentences.subList(0, max-1)) {
			result.add(sentence.toString());
		}
		return result;*/
		StringBuilder builder = new StringBuilder();
		for(Utterance utterance : conversation.utterances()){
			builder.append(utterance.speaker()).append(" said ").append(utterance.text()).append(" ");
		}
		return summarize(builder.toString(), numSentences);
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
		Collections.sort(sentences, new SentenceComparator(dfCounter, tfs));
		return sentences;
	}
	@Override
	public List<String> recognizeNamedEntity(String text) {
		List<String> result = new ArrayList<String>();
		CRFClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(SERIALIZED_CLASSIFIER);
	    List<List<CoreLabel>>	classify =	classifier.classify(text);
        for (List<CoreLabel> coreLabels : classify) {
            for (CoreLabel coreLabel : coreLabels) {
                String word = coreLabel.word();
                String answer = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
                if(!"O".equals(answer)){
                	result.add(word+" : "+answer);
                }
 
            }
        }
        return result;
	}
}
