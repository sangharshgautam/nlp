package uk.co.sangharsh.nlp.resource.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Conversation {
	
	private List<Utterance> utterances;
	public Conversation(){
		utterances = new ArrayList<Utterance>();
	}
	public List<Utterance> utterances() {
		return utterances;
	}
	public Conversation add(Utterance utterance){
		this.utterances.add(utterance);
		return this;
	}
}
