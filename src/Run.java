import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import utils.TmaLogger;
import utils.Values;

public class Run {

    public static void main(String[] args) {
        Profile profile = new ProfileImpl();
        profile.setParameter(ProfileImpl.CONTAINER_NAME, Values.CONTAINER_NAME.getValue());
        profile.setParameter(ProfileImpl.LOCAL_HOST, Values.HOST.getValue());
        profile.setParameter(ProfileImpl.LOCAL_PORT, Values.PORT.getValue());
        jade.core.Runtime runtime = jade.core.Runtime.instance();
        ContainerController container = runtime.createMainContainer(profile);
        try {
            Object[] movie_list_1 = {"The Seven Samurai", "1954", "Akira Kurosawa", "Toshiro Mifune, Takashi Shimura", "2", "Bonnie and Clyde", "1967", "Arthur Penn", "Warren Beatty, Faye Dunaway", "2.5", "The Shawshank Redemption", "1994", "Frank Darabont", "Morgan Freeman, Rita Hayworth, Tim Robbins", "2.9"};
            Object[] movie_list_2 = {"Airplane!", "1980", "Jim Abrahams", "Robert Hays, Julie Hagerty, Leslie Nielsen", "2", "Goodfellas", "1990", "Martin Scorses", "Robert De Niro, Ray Liotta, Joe Pesc", "1.5"};
            Object[] movie_list_3 = {"Reservoir Dogs", "1992", "Quentin Tarantino", "Tim Roth, Chris Penn", "3", "Slumdog Millionaire", "2008", "Danny Boyle ", "Dev Patel, Freida Pinto", "1"};
            MovieAgent agent_1 = AgentFactory.getAgent(AgentType.INITIATOR, "INITIATOR_1", movie_list_1);
            MovieAgent agent_2 = AgentFactory.getAgent(AgentType.RESPONDER, "RECEIVER_1", movie_list_2);
            MovieAgent agent_3 = AgentFactory.getAgent(AgentType.RESPONDER, "RECEIVER_2", movie_list_3);
            AgentController ac1 = container.acceptNewAgent(agent_1.getNickname(), agent_1);
            ac1.start();
            AgentController ac2 = container.acceptNewAgent(agent_2.getNickname(), agent_2);
            ac2.start();
            AgentController ac3 = container.acceptNewAgent(agent_3.getNickname(), agent_3);
            ac3.start();
        } catch (Exception e) {
            TmaLogger.get().debug("Main class error:" + e.toString());
        }
    }
}