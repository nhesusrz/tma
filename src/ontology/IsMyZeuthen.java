package ontology;

import jade.content.Predicate;

public class IsMyZeuthen implements Predicate {

    private Float value = new Float(0);

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public static Float calculate(Float utility_1, Float utility_2) {
        return new Float(Math.abs(utility_1 - utility_2) / utility_1);
    }
}