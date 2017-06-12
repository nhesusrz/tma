import utils.MovieBag;
import utils.TmaLogger;
import utils.Values;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import ontology.IsMyZeuthen;
import ontology.MCPOntology;
import ontology.SeeMovie;

import java.util.Date;
import java.util.Random;

public class InitiatorAgent extends Agent {

    private Codec codec = new SLCodec();
    private Ontology mcp_ontology = MCPOntology.getInstance();

    private IsMyZeuthen zeuthen;
    private MovieBag movies;

    private int respondersCount = 0;
    private boolean accepted = false;

    class ProposeMovieBehaviour extends SimpleBehaviour {
        public void action() {
            AID[] responders = searchDF();
            if (responders != null) {
                try {
                    respondersCount = responders.length;
                    if (!movies.isEmpty() && responders.length != 0) {
                        AID responder = responders[0];
                        ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
                        message.setSender(getAID());
                        message.setLanguage("English");
                        message.addReceiver(responder);
                        message.setOntology(mcp_ontology.getName());
                        SeeMovie see_movie = new SeeMovie(movies.pickUp(), new Date());
                        getContentManager().fillContent(message, see_movie);
                        send(message);
                        TmaLogger.get().debug("Message sent to " + responder.getLocalName() + "\n Message: ===>" + message.toString());
                        block();
                        respondersCount--;
                    }
                } catch (CodecException | OntologyException e) {
                    TmaLogger.get().error("Exception: " + e.getMessage());
                }
            } else {
                TmaLogger.get().debug("No responders founds in DF.");
            }
        }

        public boolean done() {
            return accepted || respondersCount == 0;
        }
    }

    class ResponseMovieBehaviour extends Behaviour {
        public void action() {
            ACLMessage message = blockingReceive();
            if (message != null) {
                try {
                    TmaLogger.get().debug("Received message: " + message.toString());
                    if (message.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                        accepted = true;
                        TmaLogger.get().debug(
                                "Propose ACCEPTED to Initiator: " + getAID() + "by Responder: " + message.getSender());
                    } else if (message.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                        TmaLogger.get().debug(
                                "Propose REJECTED to Initiator: " + getAID() + "by Responder: " + message.getSender());
                        ContentElement ce = getContentManager().extractContent(message);

                        //Cuando lo rechaza me tiene uqe llegar el valor de U para calcular mi Z.
                        zeuthen = new IsMyZeuthen(c - u / c);
                        ACLMessage reply = message.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        // reply.setLanguage("English");
                        // reply.setOntology(mcp_ontology.getName());
                        getContentManager().fillContent(message, zeuthen);
                        TmaLogger.get().debug(
                                "Initiator: " + getAID() + "sending zeuthen to Responder: " + message.getSender());
                        send(reply);
                    } else if (message.getPerformative() == ACLMessage.INFORM) {
                        ContentElement ce = getContentManager().extractContent(message);
                        if (ce instanceof IsMyZeuthen) {
                            System.out.println("Received zeuthen from Receiver: " + message.getSender() + " Zeuthen: " + ((IsMyZeuthen) ce).getValue());
                            if (true /* CALCULAR ZEUTHEN Y VER SI HAY CONFLICTO */) {
                                ACLMessage reply = message.createReply();
                                reply.setPerformative(ACLMessage.CANCEL);
                                getContentManager().fillContent(message, zeuthen);
                                System.out.println("Initiator: " + getAID() + "sending CANCEL to Responder: "
                                        + message.getSender());
                                send(reply);
                            }
                        }
                    }
                } catch (CodecException | OntologyException e) {
                    e.printStackTrace();
                }
            }

        }

        public boolean done() {
            return accepted || respondersCount == 0;
        }
    }

    protected void setup() {
        TmaLogger.get().debug("Setting Up Agent: " + getAID().getName());
        movies = new MovieBag(getAID().getName(), getArguments());

        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(mcp_ontology);

        addBehaviour(new ProposeMovieBehaviour());
        addBehaviour(new ResponseMovieBehaviour());
        TmaLogger.get().debug("Ending Setting Up Agent: " + getAID().getName());
    }

    protected void takeDown() {
        TmaLogger.get().debug("Terminating Agent: " + getAID().getName());
    }

    private AID[] searchDF() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(Values.DF_TYPE.getValue());
        sd.setName(Values.DF_NAME.getValue());
        template.addServices(sd);
        try {
            TmaLogger.get().debug("Agent: " + getAID().getName() + " Searching Services in DF");
            DFAgentDescription[] results = DFService.search(this, template);
            if (results.length > 0) {
                TmaLogger.get().debug("Agent: " + getAID().getName() + " Founded " + results.length + " Services in DF");
                AID[] agents = new AID[results.length];
                for (int i = 0; i < results.length; i++)
                    agents[i] = results[i].getName();
                return agents;
            } else
                TmaLogger.get().debug("Agent: " + getAID().getName() + " No Results Found in DF");
            System.out.println("Not results found: " + getName());

        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return null;
    }

}
