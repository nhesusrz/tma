package utils;

public enum Values {

    // Configuration
    LOG4J_PROPERTIES(".//properties//log4j.properties"),
    DF_TYPE("negotiation"),
    DF_NAME("movies"),
    CONTAINER_NAME("TMAContainer"),
    HOST("localhost"),
    PORT("1099"),
    DF_SENDER("df@"),

    // Log messages
    ERROR(" error: "),
    BEHAVIOUR("Behaviour: "),
    SENT_MESSAGE(" - Sent message: "),
    RECEIVE_MESSAGE(" - Received message: "),
    MOVIE_LIST_CREATED("Movie list created: "),
    NO_MOVIE_OFFER("No more movies to offer."),
    NO_MOVIE_COMPARE("No more movies to compare"),
    MSG_NO_CONTENT("Message created without content."),
    DEREGISTER(" - Unregistered"),
    FINISHED("Finished.");


    private String value;

    Values(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
