package uk.co.sangharsh.nlp.service;

import edu.stanford.nlp.trees.TreeGraphNode;

public class Task extends Commitment{

	
	private String subject;
	private String object;
	private TreeGraphNode action;
	private boolean committed;

	public Task(String sentence, String subject, String object, TreeGraphNode action) {
		super(sentence);
		this.subject = subject;
		this.object = object;
		this.action = action;
	}

	public Task(String sentence) {
		super(sentence);
	}

	public boolean isNotEmpty() {
		return action != null;
	}

	public void committed() {
		this.committed = true;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public boolean isCommitted() {
		return committed;
	}

	public void setCommitted(boolean committed) {
		this.committed = committed;
	}

	
}
