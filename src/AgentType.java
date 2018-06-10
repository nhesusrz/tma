public enum AgentType {
    INITIATOR("Initiator"),
    RESPONDER("Responder");

    private String type;

    AgentType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
