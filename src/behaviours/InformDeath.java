package behaviours;

import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class InformDeath extends OneShotBehaviour {

    private static final long serialVersionUID = -3823630727043044734L;
    
	public void action() {

        //Find all car agents
		setDataStore(super.getDataStore());
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType("Initialiser");
		DFAgentDescription agentDescription = new DFAgentDescription();
		agentDescription.addServices(serviceDescription);

		DFAgentDescription[] result = new DFAgentDescription[0];
		try {
			result = DFService.search(super.myAgent, agentDescription);
		} catch (FIPAException e) {
			e.printStackTrace();
		}

        //Send a message to all car agents but itself 
		if (result.length > 0) {

			ACLMessage message = new ACLMessage(ACLMessage.INFORM);
			for (DFAgentDescription agent : result) {
				message.addReceiver(agent.getName());
			}
			message.setContent("im leaving");

			message.removeReceiver(super.myAgent.getAID());
			super.myAgent.send(message);
		}

	}
}
