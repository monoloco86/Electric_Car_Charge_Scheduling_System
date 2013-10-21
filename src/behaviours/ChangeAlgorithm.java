package behaviours;

import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class ChangeAlgorithm extends OneShotBehaviour {
    
    private static final long serialVersionUID = 5752574523519531019L;
            
    @Override
    public void action() {
    	
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
            for (DFAgentDescription agent : result) {
                message.addReceiver(agent.getName());
            }
            message.setContent("change algorithm");
            
            super.myAgent.send(message);            
        }
        
    }
}
