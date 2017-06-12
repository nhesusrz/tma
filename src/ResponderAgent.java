import java.util.Random;

import ontology.Movie;
import utils.MovieBag;
import utils.TmaLogger;
import utils.Values;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import ontology.IsMyZeuthen;
import ontology.MCPOntology;
import ontology.SeeMovie;

public class ResponderAgent extends Agent {

	private Codec codec = new SLCodec();
	private Ontology mcp_ontology = MCPOntology.getInstance();
	
	private IsMyZeuthen zeuthen;
	private MovieBag movieBag;

	class ResponderMoviePropuseBehaviour extends CyclicBehaviour {
		private boolean fin = false;
		private boolean accept_one = false;

		public void action() {
			ACLMessage message = blockingReceive();
			if (message != null) {
				try {
					System.out.println("Received message: \n" + message.toString());
					if (message.getPerformative() == ACLMessage.PROPOSE) {
						ContentElement ce = getContentManager().extractContent(message);
						System.out.println("Received PROPOSE from Initiator: " + message.getSender() + "to Responder: " + getAID());
						if (ce instanceof SeeMovie) {
							Movie movie = movieBag.pickUp();
							if (movie.getUtility() <= ((SeeMovie) ce).getMovie().getUtility()) {
								ACLMessage reply = message.createReply();
								reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
								getContentManager().fillContent(reply, ce);
								System.out.println("Responder: " + getAID() + " ACCEPT PROPOSAL from Initiator: " + message.getSender());
								send(reply);
							} else {
								ACLMessage reply = message.createReply();
								reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
								getContentManager().fillContent(reply, ce);
								System.out.println("Responder: " + getAID() + " REJECT PROPOSAL from Initiator: " + message.getSender());
								send(reply);
							}
						} else if (ce instanceof IsMyZeuthen){
							System.out.println("Received zeuthen from Initiator: " + message.getSender() + " Zeuthen: " + ((IsMyZeuthen)ce).getValue());
							double c = new Random().nextDouble() * 10.0;
							zeuthen = new IsMyZeuthen(c);
							ACLMessage reply = message.createReply();
							reply.setPerformative(ACLMessage.INFORM);
							getContentManager().fillContent(message, zeuthen);
							System.out.println(
									"Responder: " + getAID() + "sending zeuthen to Initiator: " + message.getSender());
							send(reply);
						}

					}
				} catch (CodecException | OntologyException e) {
					e.printStackTrace();
				} // fin = true;
			} else {
				System.out.println("Responder " + getAID() + " waiting for a message.");
				block();
			}
		}
	}

	protected void setup() {
		TmaLogger.get().debug("Agent: " + getAID().getName() + " is ready.");
		addBehaviour(new ResponderMoviePropuseBehaviour());

		new MovieBag(getAID().getName(), getArguments());

		TmaLogger.get().debug("Registering the service Movies and agent in DF.");
		registerService(Values.DF_TYPE.getValue(), Values.DF_NAME.getValue());
		
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(mcp_ontology);
		Integer hola = new Integer(3);
		Integer hola3 = new Integer(4);
		if (hola == hola3){

		}
	}

	protected void takeDown() {
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("Agent: " + getAID().getName() + " is terminating.");
	}

	private void registerService(String type, String name) {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(type);
		sd.setName(name);
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
}