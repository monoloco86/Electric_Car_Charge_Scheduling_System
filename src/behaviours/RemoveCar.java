
package behaviours;

import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class RemoveCar extends OneShotBehaviour {

    private static final long serialVersionUID = 7926453073894379747L;

    Integer carCount = new Integer(0);

    public RemoveCar(Integer carCount) {
        this.carCount = carCount-1;
    }

    @Override
    public void action() {
        // Find all car agents
        setDataStore(super.getDataStore());
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("CarAgent");
        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.addServices(serviceDescription);

        DFAgentDescription[] result = new DFAgentDescription[0];
        try {
            result = DFService.search(super.myAgent, agentDescription);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        
        // Send a message to last car agent
        if (result.length > 0) {

            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.setContent("remove yourself");

            for (DFAgentDescription agent : result) {
                if (agent.getName().getLocalName().equals("CarAgent" + this.carCount.toString())) {
                    message.addReceiver(agent.getName());
                    System.out.println(super.getAgent().getLocalName()
                            + " has sent the following: "
                            + message.getContent().toString() + " to - "
                            + agent.getName().getLocalName());
                }
            }
            message.removeReceiver(super.myAgent.getAID());
            super.myAgent.send(message);
        }

    }
}
