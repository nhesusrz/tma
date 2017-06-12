package utils;

/**
 * Created by nhesusrz on 12/3/17.
 */
public enum Values {

    LOG4J_PROPERTIES(".\\properties\\log4j.properties"),
    DF_TYPE("negotiation"),
    DF_NAME("movies");

    private String value;

    Values(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
