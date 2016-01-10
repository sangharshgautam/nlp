package uk.co.sangharsh.nlp.service;

import edu.stanford.nlp.trees.TreeGraphNode;

public class InformationItem {

	private TreeGraphNode subject;
	private TreeGraphNode object;
	private TreeGraphNode predicate;

	public InformationItem(TreeGraphNode subject, TreeGraphNode predicate, TreeGraphNode object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}

	public TreeGraphNode getSubject() {
		return subject;
	}

	public TreeGraphNode getObject() {
		return object;
	}

	public TreeGraphNode getPredicate() {
		return predicate;
	}

}
