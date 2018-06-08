import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.FSMBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import utils.TmaLogger;
import utils.Values;

public class Run {

    public static FSMBehaviour createFSMBehaviour(MovieAgent agt) {
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

    public static FSMBehaviour createInitiatorFSMBehaviour(MovieAgent agt) {
        FSMBehaviour fsmBehaviour = createFSMBehaviour(agt);
        fsmBehaviour.registerFirstState(agt.new DFNotification(), MovieAgent.STATE_FIRST_INITIATOR);
        fsmBehaviour.registerDefaultTransition(MovieAgent.STATE_FIRST_INITIATOR, MovieAgent.STATE_PROPOSE);
        fsmBehaviour.registerTransition(MovieAgent.STATE_EVAL_PROPOSAL, MovieAgent.STATE_FIRST_INITIATOR, 2);
        return fsmBehaviour;
    }

    public static FSMBehaviour createResponderFSMBehaviour(MovieAgent agt) {
        FSMBehaviour fsmBehaviour = createFSMBehaviour(agt);
        fsmBehaviour.registerFirstState(agt.new DFRegistration(), MovieAgent.STATE_FIRST_RESPONDER);
        fsmBehaviour.registerDefaultTransition(MovieAgent.STATE_FIRST_RESPONDER, MovieAgent.STATE_EVAL_PROPOSAL);
        return fsmBehaviour;
    }

    public static void main(String[] args) {
        Profile profile = new ProfileImpl();
        profile.setParameter(ProfileImpl.CONTAINER_NAME, Values.CONTAINER_NAME.getValue());
        profile.setParameter(ProfileImpl.LOCAL_HOST, Values.HOST.getValue());
        profile.setParameter(ProfileImpl.LOCAL_PORT, Values.PORT.getValue());
        jade.core.Runtime runtime = jade.core.Runtime.instance();
        ContainerController container = runtime.createMainContainer(profile);
        try {
            MovieAgent agt1 = new MovieAgent();
            agt1.setArguments(new Object[]{"The Seven Samurai", "1954", "Akira Kurosawa", "Toshiro Mifune, Takashi Shimura", "2", "Bonnie and Clyde", "1967", "Arthur Penn", "Warren Beatty, Faye Dunaway", "2.5", "The Shawshank Redemption", "1994", "Frank Darabont", "Morgan Freeman, Rita Hayworth, Tim Robbins", "2.9"});
            agt1.addBehaviour(createInitiatorFSMBehaviour(agt1));
            AgentController ac1 = container.acceptNewAgent("INITIATOR_1", agt1);
            ac1.start();

            MovieAgent agt2 = new MovieAgent();
            agt2.setArguments(new Object[]{"Airplane!", "1980", "Jim Abrahams", "Robert Hays, Julie Hagerty, Leslie Nielsen", "2", "Goodfellas", "1990", "Martin Scorses", "Robert De Niro, Ray Liotta, Joe Pesc", "1.5"});
            agt2.addBehaviour(createResponderFSMBehaviour(agt2));
            AgentController ac2 = container.acceptNewAgent("RECEIVER_1", agt2);
            ac2.start();

            MovieAgent agt3 = new MovieAgent();
            agt3.setArguments(new Object[]{"Reservoir Dogs", "1992", "Quentin Tarantino", "Tim Roth, Chris Penn", "3", "Slumdog Millionaire", "2008", "Danny Boyle ", "Dev Patel, Freida Pinto", "1"});
            agt3.addBehaviour(createResponderFSMBehaviour(agt3));
            AgentController ac3 = container.acceptNewAgent("RECEIVER_2", agt3);
            ac3.start();
        } catch (Exception e) {
            TmaLogger.get().debug("Main class error:" + e.toString());
        }
    }
}