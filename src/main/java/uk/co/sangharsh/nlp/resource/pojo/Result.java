package uk.co.sangharsh.nlp.resource.pojo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Result<T> {

	private boolean ok;
	private List<T> doc;
	public Result(){}
	private Result(boolean ok, List<T> doc) {
		super();
		this.ok = ok;
		this.doc = doc;
	}
	public static final <T> Result<T> ok(List<T> doc){
		return new Result(true, doc);
	}
}
