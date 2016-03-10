package uk.co.sangharsh.nlp.service;


public class Commitment implements Dota{

	private String debtor;

	@Override
	public boolean isNotEmpty() {
		return debtor != null;
	}

	@Override
	public void commit(String debtor) {
		this.debtor = debtor;
	}

	@Override
	public String toString() {
		return "Commitment [debtor=" + debtor + "]";
	}
	
}
