import jade.content.ContentElement;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import ontology.IsMyZeuthen;
import ontology.MCPOntology;
import ontology.Movie;
import ontology.SeeMovie;
import utils.TmaLogger;
import utils.Values;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * The MovieAgent class implements a Jade agent. This agent proposes movies to other agents
 * using the monotonic concession protocol applying the Zeuthen strategy is a
 * negotiation strategy.
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Martín Pacheco - UNICEN University. Tandil, Argentina. http://www.exa.unicen.edu.ar/
 */

public class MovieAgent extends Agent {

    public static final String STATE_FIRST_RESPONDER = "DFRegistration";
    public static final String STATE_FIRST_INITIATOR = "DFNotification";
    public static final String STATE_PROPOSE = "Propose";
    public static final String STATE_EVAL_PROPOSAL = "EvaluateProposal";
    public static final String STATE_CALCULATE_ZEUTHEN = "CalculateZeuthen";
    public static final String STATE_DECIDE = "Decide";
    public static final String STATE_END = "EndAgent";

    // Initiator variables.
    private boolean df_initiator_subscribed = false;
    private ArrayList<AID> agents_queue;
    private ArrayList<AID> agents_chatted;
    private ACLMessage df_subscribe_msg;

    // Initiator and Responder variables.
    private DFAgentDescription df_description_service;
    private AID lastResponderAgentID;
    private ArrayList<Movie> movieList;
    private Movie my_movie;
    private Movie prop_movie;
    private IsMyZeuthen my_zeuthen;


    /**
     * The initiator agent subscribes to DF and waits for the DF notification when an agent register its services.
     * This is the first behavior of an initiator agent.
     */
    class DFNotification extends SimpleBehaviour {
        private boolean terminate = false;

        public void action() {
            if (!df_initiator_subscribed) {
                df_initiator_subscribed = dfSubscription(getAgent());
                agents_chatted = new ArrayList<>();
                agents_queue = new ArrayList<>();
            }
            ACLMessage msg = receiveMessage(STATE_FIRST_INITIATOR);
            if (msg != null) {
                try {
                    DFAgentDescription[] dfad = DFService.decodeNotification(msg.getContent());
                    if (dfad.length > 0) {
                        if (agents_chatted.contains(dfad[0].getName())) {
                            TmaLogger.get().debug(dfad[0].getName().getName() + Values.DEREGISTER.getValue());
                            reset();
                        } else {
                            agents_queue.add(dfad[0].getName());
                        }
                        if (!agents_queue.isEmpty()) {
                            lastResponderAgentID = agents_queue.remove(0);
                            agents_chatted.add(lastResponderAgentID);
                            terminate = true;
                        }
                    }
                } catch (Exception e) {
                    TmaLogger.get().debug(STATE_FIRST_INITIATOR + Values.ERROR.getValue() + e.toString());
                }
            } else {
                reset();
                block();
            }
        }

        public void reset() {
            terminate = false;
        }

        public boolean done() {
            return terminate;
        }
    }

    /**
     * An agent registers its services into DF.
     * This is the first behavior for responder agent.
     */
    class DFRegistration extends OneShotBehaviour {

        public void action() {
            try {
                TmaLogger.get().debug(STATE_FIRST_RESPONDER + " Registering services into DF.");
                DFService.register(getAgent(), df_description_service);
            } catch (FIPAException fe) {
                TmaLogger.get().debug(STATE_FIRST_RESPONDER + Values.ERROR.getValue() + fe.toString());
            }
        }
    }

    /**
     * The agent sends the proposal to the responder agent.
     */
    class Propose extends OneShotBehaviour {
        private int next = 1;

        public void action() {
            try {
                if (!movieList.isEmpty()) {
                    my_movie = movieList.remove(0);
                    SeeMovie see_movie = new SeeMovie();
                    see_movie.setMovie(my_movie);
                    see_movie.setDate(new Date());
                    ACLMessage msg = createMessage(ACLMessage.PROPOSE, lastResponderAgentID, new Action(getAID(), see_movie));
                    sendMessage(STATE_PROPOSE, msg);
                } else {
                    TmaLogger.get().debug(Values.NO_MOVIE_OFFER.getValue());
                    ACLMessage msg = createMessage(ACLMessage.CANCEL, lastResponderAgentID, null);
                    sendMessage(STATE_PROPOSE, msg);
                    next = 0;
                }
            } catch (Exception e) {
                TmaLogger.get().debug(STATE_PROPOSE + Values.ERROR.getValue() + e.toString());
            }
        }

        public int onEnd() {
            return next;
        }
    }


    /**
     * The agent evaluates the proposal. The agent accepts or rejects the proposal.
     * Also the initiator agent receives the answer.
     * If the initiator agent terminates then returns to the initial behaviour.
     */
    public class EvaluateProposal extends SimpleBehaviour {
        private int next = 1;
        private boolean terminate = false;

        public void action() {
            ACLMessage msg = receiveMessage(STATE_EVAL_PROPOSAL);
            if (msg != null) {
                try {
                    if (msg.getPerformative() == ACLMessage.PROPOSE) {
                        ACLMessage reply = msg.createReply();
                        ContentElement ce = getContentManager().extractContent(msg);
                        SeeMovie seeMovie = (SeeMovie) ((Action) ce).getAction();
                        prop_movie = seeMovie.getMovie();
                        if (!movieList.isEmpty()) {
                            my_movie = movieList.get(0);
                            if (my_movie.getUtility() <= prop_movie.getUtility()) {
                                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                next = getNextState();
                            } else {
                                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                                seeMovie.setMovie(my_movie);
                                getContentManager().fillContent(reply, new Action(getAID(), seeMovie));
                                lastResponderAgentID = msg.getSender();
                            }
                        } else {
                            TmaLogger.get().debug(Values.NO_MOVIE_COMPARE.getValue());
                            reply.setPerformative(ACLMessage.CANCEL);
                            next = getNextState();
                        }
                        sendMessage(STATE_EVAL_PROPOSAL, reply);
                    }
                    if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                        ContentElement ce = getContentManager().extractContent(msg);
                        SeeMovie seeMovie = (SeeMovie) ((Action) ce).getAction();
                        prop_movie = seeMovie.getMovie();
                    }
                    if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL || msg.getPerformative() == ACLMessage.CANCEL) {
                        next = getNextState();
                    }
                    if (msg.getPerformative() == ACLMessage.INFORM) {
                        processDFNotification(STATE_EVAL_PROPOSAL, msg);
                        reset();
                    } else {
                        terminate = true;
                    }
                } catch (Exception e) {
                    TmaLogger.get().debug(STATE_EVAL_PROPOSAL + Values.ERROR.getValue() + e.toString());
                }
            } else {
                reset();
                block();
            }
        }

        public void reset() {
            terminate = false;
            next = 1;
        }

        public int onEnd() {
            return next;
        }

        public boolean done() {
            return terminate;
        }

        private int getNextState() {
            if (df_initiator_subscribed)
                return 2;
            return 0;
        }
    }

    /**
     * The agent calculates the Zeuthen value and informs it to the other agent.
     */
    class CalculateZeuthen extends OneShotBehaviour {

        public void action() {
            try {
                my_zeuthen = new IsMyZeuthen();
                my_zeuthen.setValue(IsMyZeuthen.calculate(my_movie.getUtility(), prop_movie.getUtility()));
                ACLMessage msg = createMessage(ACLMessage.INFORM, lastResponderAgentID, my_zeuthen);
                sendMessage(STATE_CALCULATE_ZEUTHEN, msg);
            } catch (Exception ex) {
                TmaLogger.get().debug(STATE_CALCULATE_ZEUTHEN + Values.ERROR.getValue() + ex.toString());
            }
        }
    }

    /**
     * The agent according to the received Zeuthen goes to new proposal state or goes to evaluate proposal state.
     */
    class Decide extends SimpleBehaviour {
        private int next = 1;
        private boolean terminate = false;

        public void action() {
            ACLMessage msg = receiveMessage(STATE_DECIDE);
            if (msg != null) {
                try {
                    if (msg.getPerformative() == ACLMessage.INFORM) {
                        if (msg.getSender().getName().contains(Values.DF_SENDER.getValue())) {
                            processDFNotification(STATE_DECIDE, msg);
                            reset();
                        } else {
                            ContentElement ce = getContentManager().extractContent(msg);
                            IsMyZeuthen rec_zeuthen = ((IsMyZeuthen) ce);
                            if (my_zeuthen.getValue() < rec_zeuthen.getValue())
                                next = 0;
                            terminate = true;
                        }
                    }
                } catch (Exception ex) {
                    TmaLogger.get().debug(STATE_DECIDE + Values.ERROR.getValue() + ex.toString());
                }
            } else {
                reset();
                block();
            }
        }

        public void reset() {
            terminate = false;
            next = 1;
        }

        public int onEnd() {
            return next;
        }

        public boolean done() {
            return terminate;
        }

    }

    /**
     * The agent makes a state transition from active, suspended or waiting to deleted state within
     * Agent Platform Life Cycle, thereby destroying the agent.
     */
    class EndAgent extends OneShotBehaviour {

        public void action() {
            doDelete();
            TmaLogger.get().debug(Values.FINISHED.getValue());
        }
    }

    protected void setup() {
        movieList = createMovieList(getArguments());
        getContentManager().registerLanguage(MCPOntology.getCodecInstance());
        getContentManager().registerOntology(MCPOntology.getInstance());
        ServiceDescription servicedescription = new ServiceDescription();
        servicedescription.setType(Values.DF_TYPE.getValue());
        servicedescription.setName(Values.DF_NAME.getValue());
        servicedescription.addOntologies(MCPOntology.getInstance().getName());
        df_description_service = new DFAgentDescription();
        df_description_service.addServices(servicedescription);
    }

    protected void takeDown() {
        try {
            if (df_initiator_subscribed)
                send(DFService.createCancelMessage(this, getDefaultDF(), df_subscribe_msg));
            else
                DFService.deregister(this);
        } catch (FIPAException e) {
            TmaLogger.get().debug(Values.ERROR.getValue() + e.toString());
        }
    }

    private ACLMessage createMessage(int type, AID receiver, ContentElement content) {
        ACLMessage message = new ACLMessage(type);
        message.setSender(getAID());
        message.addReceiver(receiver);
        message.setLanguage(MCPOntology.getCodecInstance().getName());
        message.setOntology(MCPOntology.getInstance().getName());
        try {
            getContentManager().fillContent(message, content);
        } catch (CodecException | OntologyException e) {
            TmaLogger.get().debug(Values.ERROR.getValue() + e.toString());
        } catch (NullPointerException e) {
            TmaLogger.get().debug(Values.MSG_NO_CONTENT.getValue());
        }
        return message;
    }

    private void sendMessage(String behaviour, ACLMessage msg) {
        TmaLogger.get().debug(Values.BEHAVIOUR.getValue() + behaviour + Values.SENT_MESSAGE.getValue() + msg.toString());
        send(msg);
    }

    private ACLMessage receiveMessage(String behaviour) {
        ACLMessage msg = receive();
        if (msg != null)
            TmaLogger.get().debug(Values.BEHAVIOUR.getValue() + behaviour + Values.RECEIVE_MESSAGE.getValue() + msg.toString());
        return msg;
    }

    private boolean dfSubscription(Agent agent) {
        try {
            SearchConstraints sc = new SearchConstraints();
            sc.setMaxResults(new Long(1));
            df_subscribe_msg = DFService.createSubscriptionMessage(agent, getDefaultDF(), df_description_service, sc);
            send(df_subscribe_msg);
            return true;
        } catch (Exception e) {
            TmaLogger.get().debug(Values.ERROR.getValue() + e.toString());
            return false;
        }
    }

    private void processDFNotification(String STATE, ACLMessage msg) {
        try {
            DFAgentDescription[] dfad = DFService.decodeNotification(msg.getContent());
            if (dfad.length > 0)
                if (df_initiator_subscribed && !agents_chatted.contains(dfad[0].getName()))
                    agents_queue.add(dfad[0].getName());
                else
                    TmaLogger.get().debug(STATE + " - " + dfad[0].getName().getName() + Values.DEREGISTER.getValue());
        } catch (FIPAException ex) {
            TmaLogger.get().debug(STATE + Values.ERROR.getValue() + ex.toString());
        }
    }

    private ArrayList<Movie> createMovieList(Object[] list) {
        ArrayList<Movie> movies = new ArrayList<>();
        try {
            if (list != null && list.length > 0) {
                String[] string_list = Arrays.copyOf(list, list.length, String[].class);
                for (int i = 0; i < string_list.length; i = i + 5) {
                    Movie movie = new Movie();
                    movie.setName(string_list[i]);
                    Calendar.getInstance().set(Integer.parseInt(string_list[i + 1]), 1, 1);
                    movie.setYear(Calendar.getInstance().getTime());
                    movie.setDirector(string_list[i + 2]);
                    movie.setActors(string_list[i + 3]);
                    movie.setUtility(new Float(string_list[i + 4]));
                    movies.add(movie);
                }
                TmaLogger.get().debug(Values.MOVIE_LIST_CREATED.getValue() + movies.toString());
            }
        } catch (Exception e) {
            TmaLogger.get().debug(Values.ERROR.getValue() + e.toString());
        }
        return movies;
    }
}