package uk.co.sangharsh.nlp.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import uk.co.sangharsh.nlp.resource.Conversation;
import uk.co.sangharsh.nlp.resource.Utterance;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
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

public class HplTest {

	public static void main(String[] args) {
//		String text = "I will also check with Alliance Travel Agency";
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props());
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		
		
		Conversation conversation = new Conversation();
		conversation.add(Utterance.statement("Sangharsh", "I will also check with Alliance Travel Agency"));
		conversation.add(Utterance.statement("Sangharsh", "Pavan will handle the issuance of the LC"));
		
		conversation.add(Utterance.statement("Sangharsh", "Pavan will you please look into the production issue?"));
		conversation.add(Utterance.statement("Sangharsh", "Can you please provide me the details about the amounts from prior months by this Friday?."));
		/*conversation.add(Utterance.statement("Pavan", "Ok, I will look into it."));
		conversation.add(Utterance.statement("Sangharsh", "who is looking into mail from steve about the File upload being slow?"));
		conversation.add(Utterance.statement("Pavan", "Julie will be looking in to mail from steve about file upload being slow"));
		
		conversation.add(Utterance.statement("Sangharsh", "who is looking into mail from steve about the File upload being slow?"));
		conversation.add(Utterance.statement("Pavan", "Julie will be looking in to mail from steve about file upload being slow"));
		
		conversation.add(Utterance.statement("Sangharsh", "Arvinder mailed saying he is not coming to office because he is sick"));
		conversation.add(Utterance.statement("Pavan", "I will pick up Arvinder's task"));
		
		conversation.add(Utterance.statement("Sangharsh", "good"));
		conversation.add(Utterance.statement("Pavan", "Yesterday I worked on 5578. Today I will work on 5589"));
		conversation.add(Utterance.statement("Sangharsh", "Steve turner will be looking into the PLM activity today"));*/
		
		process(pipeline, gsf, conversation);
	}
	private static void process(StanfordCoreNLP pipeline, GrammaticalStructureFactory gsf, Conversation conversation) {
		for(Utterance utterance : conversation.utterances()){
			processUtterance(pipeline, gsf, utterance);
		}
	}
	private static void processUtterance(StanfordCoreNLP pipeline, GrammaticalStructureFactory gsf, Utterance utterance) {
		Annotation document = pipeline.process(utterance.text());
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for(CoreMap sentence : sentences){
			Task task = null;
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
			if(commitment){
				task = new Task(subject, object, action);
			}
			//Algorithm 3: identify delegate commitments(sentence)
			System.out.println(task);
			
		}
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
	private static Properties props() {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner, parse");
		props.setProperty("tokenize.language", "en");
		props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/english/english-left3words-distsim.tagger");
		return props;
	}
}
