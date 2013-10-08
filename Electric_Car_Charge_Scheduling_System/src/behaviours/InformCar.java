
package behaviours;

import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class InformCar extends SimpleBehaviour{

    /**
     * 
     */
    private static final long serialVersionUID = -1150148550201188149L;
    boolean finished = false;

    @Override
    public void action() {        
        ACLMessage message = super.myAgent.receive();
        if (message != null) {
            // Handle the message.
            if (message.getContent().contains("yes") && message.getContent().contains("enough") && message.getContent().contains("power")) {
                //get power needed
                System.out.println(super.myAgent.getLocalName() + ": MESSAGE RECEIVED: "
                        + message.getContent() + " ---- From: "
                        + message.getSender().getLocalName());
                
                finished = true;

                ServiceDescription serviceDescription = new ServiceDescription();
                serviceDescription.setType("Car");
                DFAgentDescription agentDescription = new DFAgentDescription();
                agentDescription.addServices(serviceDescription);
                

                DFAgentDescription[] result = new DFAgentDescription[0];
                try {
                    result = DFService.search(super.myAgent, agentDescription);
                } catch (FIPAException e) {
                    e.printStackTrace();
                }
                
                if (result.length > 0) {
                    ACLMessage msgToTransformer = new ACLMessage(ACLMessage.INFORM);
                    for (DFAgentDescription agent : result) {
                        msgToTransformer.addReceiver(agent.getName());
                    }
                    msgToTransformer.setContent("I am charging you in slot1");
                    super.myAgent.send(msgToTransformer);
                }                              
            }
        }
    }

    @Override
    public boolean done() {
        return finished;
    }

}
