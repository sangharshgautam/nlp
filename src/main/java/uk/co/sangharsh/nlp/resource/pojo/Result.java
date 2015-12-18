package uk.co.sangharsh.nlp.resource.pojo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Result {

	private boolean ok;
	private List<String> doc;
	public Result(){}
	private Result(boolean ok, List<String> doc) {
		super();
		this.ok = ok;
		this.doc = doc;
	}
	public static final Result ok(List<String> doc){
		return new Result(true, doc);
	}
}
