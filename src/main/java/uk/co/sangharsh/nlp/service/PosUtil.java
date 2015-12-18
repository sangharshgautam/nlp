package uk.co.sangharsh.nlp.service;
//http://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
public class PosUtil {
	
	public static final String COORDINATING_CONJUNCTION = "CC";
	public static final String CARDINAL_NUMBER = "CD";
	public static final String EXISTENTIAL_THERE = "EX";
	public static final String FOREIGN_WORD = "FW";
	public static final String PREPOSITION = "IN";	    
	public static final String DETERMINER = "DT";
	
	public class Adjective{
		public static final String ADJECTIVE = "JJ";
		public static final String COMPARATIVE = "JJR"; 
		public static final String SUPERLATIVE = "JJS"; 
	}
	public static final String LIST_ITEM_MARKER = "LS"; 
	public static final String MODAL = "MD";
	public class Noun{
		public static final String SINGULAR_OR_MASS = "NN";
		public static final String PLURAL = "NNS"; 
		public static final String PROPER_SINGULAR = "NNP"; 
		public static final String PROPER_PLURAL = "NNPS";
		public static final String START = "N";
	}
	public static final String Predeterminer = "PDT";
	public static final String POSSESSIVE_ENDING = "POS"; 
	
	public class Pronoun{
		public static final String PERSONAL = "PRP";
		public static final String POSSESSIVE = "PRP$";
	}
	
	public class Adverb{
		public static final String Adverb = "RB";
		public static final String COMPARATIVE = "RBR";
		public static final String SUPERLATIVE = "RBS";
	}
	
	public static final String Particle = "RP";
	public static final String Symbol = "SYM";
	public static final String to = "TO";
	public static final String Interjection = "UH";
	public class Verb{
		public static final String BASE_FORM = "VB";
		public static final String PAST_TENSE = "VBD";
		public static final String GERUND_OR_PRESENT_PARTICIPLE = "VBG";
		public static final String PAST_PARTICIPLE = "VBN"; 
		public static final String NON­3RD_PERSON_SINGULAR_PRESENT = "VBP";
		public static final String T3RD_PERSON_SINGULAR_PRESENT = "VBZ";
	}
	public class Wh{
		public static final String DETERMINER = "WDT";
		public static final String PRONOUN = "WP";
		public static final String POSSESSIVE_WH­PRONOUN = "WP$";
		public static final String ADVERB = "WRB";
	}
	public static final boolean isNoun(String pos) {
		return pos.toUpperCase().startsWith(PosUtil.Noun.START);
	}	
}
