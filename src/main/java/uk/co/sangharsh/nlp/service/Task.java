package uk.co.sangharsh.nlp.service;

import edu.stanford.nlp.trees.TreeGraphNode;

public class Task extends Commitment{

	private String subject;
	private String object;
	private TreeGraphNode action;
	private boolean committed;

	public Task(String subject, String object, TreeGraphNode action) {
		this.subject = subject;
		this.object = object;
		this.action = action;
	}

	public Task() {
	}

	public boolean isNotEmpty() {
		return action != null;
	}

	public void committed() {
		this.committed = true;
	}

	@Override
	public String toString() {
		return "Task [subject=" + subject + ", object=" + object + ", action="
				+ action + ", committed=" + committed + "]";
	}

}
