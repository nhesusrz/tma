package ontology;

import jade.content.Predicate;

public class IsMyZeuthen implements Predicate {

	private static final long serialVersionUID = -7881835329921272316L;
	private double value;

	public IsMyZeuthen(double value) {
		this.value = value;
	}

	public double getValue() {
		return value;
	}

}
