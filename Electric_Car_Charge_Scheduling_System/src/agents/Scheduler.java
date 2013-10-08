
package agents;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.SequentialBehaviour;
import behaviours.AskForPower;
import behaviours.InformCar;
import behaviours.ReplySlots;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class Scheduler extends Agent {

    private static final long serialVersionUID = -6999131954910262235L;

    static int slotLimit = 2;
    int slotsUsed = 0;

    protected void setup() {
        /*
         * Register this agent with DF.
         */
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("Scheduler");
        serviceDescription.setName(super.getLocalName());

        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(super.getAID());
        agentDescription.addServices(serviceDescription);
        
        try {
            DFService.register(this, agentDescription);
        } catch (FIPAException e) {
            e.printStackTrace();
        }


        Integer carLimit = new Integer(2);
        Integer carsStored = new Integer(0);
        DataStore ds = new DataStore();
        ds.put("carLimit", carLimit);
        ds.put("carsStored", carsStored);
        
        
        SequentialBehaviour schedulerSuperBehaviour = new SequentialBehaviour();
        schedulerSuperBehaviour.setDataStore(ds);
        
        super.addBehaviour(schedulerSuperBehaviour);
        schedulerSuperBehaviour.addSubBehaviour(new ReplySlots());
        schedulerSuperBehaviour.addSubBehaviour(new AskForPower());
        schedulerSuperBehaviour.addSubBehaviour(new InformCar());

        
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
