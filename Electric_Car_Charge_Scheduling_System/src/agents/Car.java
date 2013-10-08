package agents;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.SequentialBehaviour;
import behaviours.AskSlots;
import behaviours.AskToCharge;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class Car extends Agent {

    private static final long serialVersionUID = -2481137036537418853L;
    
    protected void setup() {
        /*
         * Register this agent with DF.
         */
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("Car");
        serviceDescription.setName(super.getLocalName());
        
        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(super.getAID());      
        agentDescription.addServices(serviceDescription);
        
        try {
            DFService.register(this, agentDescription);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        
        Double timeTillUse = new Double(180);
        Double chargeNeeded = new Double(400);        
        
        DataStore ds = new DataStore();
        
        ds.put("timeTillUse", timeTillUse);
        ds.put("chargeNeeded", chargeNeeded);
        
        SequentialBehaviour carSuperBehaviour = new SequentialBehaviour();
        carSuperBehaviour.setDataStore(ds);
        
        super.addBehaviour(carSuperBehaviour);
        carSuperBehaviour.addSubBehaviour(new AskSlots());
        carSuperBehaviour.addSubBehaviour(new AskToCharge());
          
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
