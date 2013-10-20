
package behaviours;

import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class AskSlotValues extends OneShotBehaviour {

    private static final long serialVersionUID = 7926453073894379747L;

    @Override
    public void action() {
        System.out.println("ASKING FOR SLOT VALUES");
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

        if (result.length > 0) {

            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.setContent("what are your slot values");

            for (DFAgentDescription agent : result) {
                message.addReceiver(agent.getName());
                super.myAgent.send(message);
                System.out.println(super.getAgent().getLocalName() + " has sent the following: "
                        + message.getContent().toString() + " to - "
                        + agent.getName().getLocalName());
            }
        }

    }
}
