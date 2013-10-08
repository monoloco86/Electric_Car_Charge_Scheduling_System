package agents;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.SequentialBehaviour;
import behaviours.ReplyPower;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class Transformer extends Agent{

    private static final long serialVersionUID = -703939269358764444L;

    protected void setup() {
        /*
         * Register this agent with DF.
         */
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("Transformer");
        serviceDescription.setName(super.getLocalName());
        
        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(super.getAID());
        agentDescription.addServices(serviceDescription);
        
        try {
            DFService.register(this, agentDescription);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        Double powerLimit = new Double(1000);
        Double currentPower = new Double(0);
        
        DataStore ds = new DataStore();
        ds.put("powerLimit", powerLimit);
        ds.put("currentPower", currentPower);
        
        SequentialBehaviour transformerSuperBehaviour = new SequentialBehaviour();        
        transformerSuperBehaviour.setDataStore(ds);
        
        super.addBehaviour(transformerSuperBehaviour);                     
        transformerSuperBehaviour.addSubBehaviour(new ReplyPower());
        
    }
    
    protected void takeDown() {
        /*
         * Deregister this agent with DF.
         */
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }   
    }
}
