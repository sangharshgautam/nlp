package uk.co.sangharsh.nlp.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import uk.co.sangharsh.nlp.resource.pojo.Conversation;
import uk.co.sangharsh.nlp.resource.pojo.Utterance;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
//sh -c "mvn jetty:run &"
@Service
public class NlpServiceImpl implements NlpService {

	private static final String NLP_RESOURCE = "/home/ubuntu/GIT/nlp/nlp-resources/";

	private static final String DF_COUNTER_PATH = NLP_RESOURCE+"df-counts.ser";
	
	private StanfordCoreNLP pipeline;

	private Counter<String> dfCounter;
	
	
	private TreebankLanguagePack tlp = new PennTreebankLanguagePack();
	 
	GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
	
	@PostConstruct
	public void setNlp() throws ClassNotFoundException, IOException {
		this.dfCounter = ObjectUtil.loadObjectNoExceptions(DF_COUNTER_PATH, Counter.class);
	    
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner, parse");
		props.setProperty("tokenize.language", "en");

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
		
		return actionitems(conversation);
	}
	@Override
	public List<String> actionitems(Conversation conversation) {
		List<Dota> tasks = new ArrayList<Dota>();
		for(Utterance utterance : conversation.utterances()){
			tasks.addAll(processUtterance(utterance));
		}
		List<String> result = new ArrayList<String>();
		for(Dota dota : tasks){
			result.add(dota.toString());
		}
		return result;
	}
	private List<Dota> processUtterance(Utterance utterance) {
		List<Dota> tasks = new ArrayList<Dota>();
		Annotation document = pipeline.process(utterance.text());
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for(CoreMap sentence : sentences){
			Dota task = new Task();
			Tree tree = sentence.get(TreeAnnotation.class);
//			tree.pennPrint(System.out);
			GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
			Collection<TypedDependency> tdl = gs.typedDependenciesCollapsedTree();
			
			Collection<TypedDependency> nSubjs = getNSubjs(tdl);
			String subject = null;
			String object = null;
			String sender = utterance.speaker();
			TreeGraphNode action = null;
			for(TypedDependency nsubj : nSubjs){
				System.out.println(nsubj);
				TreeGraphNode  dependent = nsubj.dep();
				TreeGraphNode  governer = nsubj.gov();
				boolean validSubject = false;
				if(firstPerson(dependent)){
					//first person
					subject = sender;
					validSubject = true;
				}else if(secondPerson(dependent)){
					subject = sender;
					object = dependent.label().word();
					validSubject = true;
				}else if(thirdPerson(dependent)){
					//third person
					subject = sender;
					object = dependent.label().word();//extractObject(sentence);
					validSubject = validSubject(dependent);
				}else{
					System.out.println("Unhandled scenario");
					
				}
				boolean validVerb = validVerb(governer);
				System.out.println("ValidSubject: "+validSubject+" ValidVerb "+validVerb);
				if(validSubject && validVerb){
					action = extractActionDetails(governer);
				}
			}
			//Algorithm 2: identify create commitment(sentence)
			
			
			//check if the action is in present tense (VB)
			boolean commitment = false;
			if(action!=null && getPos(action).equals("VB")){
				//check if there is a relation that associates the action with a modal verb or please.
				for(TypedDependency td : tdl){
					TreeGraphNode governer = td.gov();
					TreeGraphNode dependent = td.dep();
					if(hasCommitment(dependent, governer, action) || hasCommitment(governer, dependent, action)){
						commitment = true;
						break;
					}
				}
			}
			task = new Task(subject, object, action);
			if(commitment){
				if(task.isNotEmpty()){
					
				}else{
					task = new Commitment();
				}
				task.commit(sender);
			}
			//Algorithm 3: identify delegate commitments(sentence)
			if(task.isNotEmpty()){
				System.out.println(task);
				tasks.add(task);
			}
		}
		return tasks;
	}
	private List<String> actionItems(String document) {
		List<String> result = new ArrayList<String>();
		Annotation annotation = pipeline.process(document);
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		for(CoreMap sentence : sentences){
			GrammaticalStructure gs = gsf.newGrammaticalStructure(sentence.get(TreeAnnotation.class));
			Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
			boolean hasNSubj = false;
			for(TypedDependency td : tdl){
				if(td.reln().getShortName().equalsIgnoreCase("nsubj")){
					hasNSubj = true;
					break;
				}
			}
			if(!hasNSubj){
				result.add(sentence.toString());
			}
		}
		return result;
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
		/*CRFClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(SERIALIZED_CLASSIFIER);
	    List<List<CoreLabel>>	classify =	classifier.classify(text);
        for (List<CoreLabel> coreLabels : classify) {
            for (CoreLabel coreLabel : coreLabels) {
                String word = coreLabel.word();
                String answer = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
                if(!"O".equals(answer)){
                	result.add(word+" : "+answer);
                }
 
            }
        }*/
        return result;
	}
	private static boolean hasCommitment(TreeGraphNode node1, TreeGraphNode node2, TreeGraphNode action) {
		return node1.equals(action) && ("MD".equals(getPos(node2)) || "please".equalsIgnoreCase(node2.label().word()));
	}
	private static TreeGraphNode extractActionDetails(TreeGraphNode governer) {
		String pos = getPos(governer);
		return pos.toUpperCase().startsWith("VB") ? governer : null;
	}
	
	private static boolean validVerb(TreeGraphNode governer) {
		String pos = getPos(governer);
		List<String> validVerbs =  new ArrayList<String>(){{
			add("VB");
			add("VBD");
			add("VBP");
			add("VBZ");
			add("VBN");
		}};
		return validVerbs.contains(pos.toUpperCase());
	}
	private static boolean validSubject(TreeGraphNode dependent) {
		CoreLabel label = dependent.label();
		String pos = getPos(dependent);
		List<String> validSubjectNer =  new ArrayList<String>(){{
			add("PERSON");
			add("ORGANIZATION");
		}};
		
		return "NNP".equalsIgnoreCase(pos) || validSubjectNer.contains(label.ner().toUpperCase());
	}
	private static String extractObject(CoreMap sentence) {
		return "Extract object";
	}
	private static boolean secondPerson(TreeGraphNode node) {
		String pos = getPos(node);
		return "PRP".equalsIgnoreCase(pos) && "You".equalsIgnoreCase(node.label().word());
	}
	private static String getPos(TreeGraphNode node) {
		return node.label().get(PartOfSpeechAnnotation.class);
	}
	private static boolean thirdPerson(TreeGraphNode node) {
		String pos = getPos(node);
		return "NNP".equalsIgnoreCase(pos);
	}
	private static boolean firstPerson(TreeGraphNode node) {
		String pos = getPos(node);
		return "PRP".equalsIgnoreCase(pos) && "I".equalsIgnoreCase(node.label().word());
	}
	private static Collection<TypedDependency> getNSubjs(Collection<TypedDependency> tdl) {
		List<TypedDependency> nsubjs = new ArrayList<TypedDependency>();
		for(TypedDependency td : tdl){
			TreeGraphNode gov= td.gov();
			GrammaticalRelation gr = td.reln();
			if("NSUBJ".equalsIgnoreCase(gr.getShortName())){
				nsubjs.add(td);
			}
		}
		return nsubjs;
	}
}
