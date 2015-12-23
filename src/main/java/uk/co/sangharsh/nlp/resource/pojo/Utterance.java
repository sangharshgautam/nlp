package uk.co.sangharsh.nlp.resource.pojo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Utterance {
	private String speaker;
	private String text;

	public String speaker() {
		return speaker;
	}

	public String text() {
		return text;
	}
}
