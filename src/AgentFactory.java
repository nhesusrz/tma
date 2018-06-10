import jade.core.behaviours.FSMBehaviour;


public class AgentFactory {

    public static MovieAgent getAgent(AgentType type, String nickname, Object[] movie_list) {
        if (type.getType().equals(AgentType.INITIATOR.getType())) {
            return createInitiatorAgent(nickname, movie_list);
        }
        if (type.getType().equals(AgentType.RESPONDER.getType())) {
            return createResponderAgent(nickname, movie_list);
        }
        return null;
    }

    private static MovieAgent createResponderAgent(String nickname, Object[] movie_list) {
        MovieAgent agent = new MovieAgent(nickname);
        agent.setArguments(movie_list);
        agent.addBehaviour(createResponderFSMBehaviour(agent));
        return agent;
    }

    private static MovieAgent createInitiatorAgent(String nickname, Object[] movie_list) {
        MovieAgent agent = new MovieAgent(nickname);
        agent.setArguments(movie_list);
        agent.addBehaviour(createInitiatorFSMBehaviour(agent));
        return agent;
    }

    private static FSMBehaviour createFSMBehaviour(MovieAgent agt) {
        FSMBehaviour fsmBehaviour = new FSMBehaviour(agt);
        fsmBehaviour.registerState(agt.new Propose(), MovieAgent.STATE_PROPOSE);
        fsmBehaviour.registerState(agt.new EvaluateProposal(), MovieAgent.STATE_EVAL_PROPOSAL);
        fsmBehaviour.registerState(agt.new CalculateZeuthen(), MovieAgent.STATE_CALCULATE_ZEUTHEN);
        fsmBehaviour.registerState(agt.new Decide(), MovieAgent.STATE_DECIDE);
        fsmBehaviour.registerLastState(agt.new EndAgent(), MovieAgent.STATE_END);
        fsmBehaviour.registerTransition(MovieAgent.STATE_PROPOSE, MovieAgent.STATE_END, 0);
        fsmBehaviour.registerTransition(MovieAgent.STATE_PROPOSE, MovieAgent.STATE_EVAL_PROPOSAL, 1);
        fsmBehaviour.registerTransition(MovieAgent.STATE_EVAL_PROPOSAL, MovieAgent.STATE_END, 0);
        fsmBehaviour.registerTransition(MovieAgent.STATE_EVAL_PROPOSAL, MovieAgent.STATE_CALCULATE_ZEUTHEN, 1);
        fsmBehaviour.registerDefaultTransition(MovieAgent.STATE_CALCULATE_ZEUTHEN, MovieAgent.STATE_DECIDE);
        fsmBehaviour.registerTransition(MovieAgent.STATE_DECIDE, MovieAgent.STATE_PROPOSE, 0);
        fsmBehaviour.registerTransition(MovieAgent.STATE_DECIDE, MovieAgent.STATE_EVAL_PROPOSAL, 1);
        return fsmBehaviour;
    }

    private static FSMBehaviour createInitiatorFSMBehaviour(MovieAgent agt) {
        FSMBehaviour fsmBehaviour = createFSMBehaviour(agt);
        fsmBehaviour.registerFirstState(agt.new DFNotification(), MovieAgent.STATE_FIRST_INITIATOR);
        fsmBehaviour.registerDefaultTransition(MovieAgent.STATE_FIRST_INITIATOR, MovieAgent.STATE_PROPOSE);
        fsmBehaviour.registerTransition(MovieAgent.STATE_EVAL_PROPOSAL, MovieAgent.STATE_FIRST_INITIATOR, 2);
        return fsmBehaviour;
    }

    private static FSMBehaviour createResponderFSMBehaviour(MovieAgent agt) {
        FSMBehaviour fsmBehaviour = createFSMBehaviour(agt);
        fsmBehaviour.registerFirstState(agt.new DFRegistration(), MovieAgent.STATE_FIRST_RESPONDER);
        fsmBehaviour.registerDefaultTransition(MovieAgent.STATE_FIRST_RESPONDER, MovieAgent.STATE_EVAL_PROPOSAL);
        return fsmBehaviour;
    }
}
