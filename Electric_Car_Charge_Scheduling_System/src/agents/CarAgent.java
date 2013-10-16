
package agents;

import gui.CarGui;

import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.SequentialBehaviour;
import behaviours.GetInfo;
import behaviours.InformWorld;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

public class CarAgent extends GuiAgent {

    private static final long serialVersionUID = -2481137036537418853L;

    transient protected CarGui myGui;

    private DataStore ds = new DataStore();

    static final int WAIT = -1;
    static final int QUIT = 0;
    private int command = WAIT;

    SequentialBehaviour carSuperBehaviour = new SequentialBehaviour();

    protected void setup() {

        /*
         * Register this agent with DF.
         */
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("CarAgent");
        serviceDescription.setName(super.getLocalName());

        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(super.getAID());
        agentDescription.addServices(serviceDescription);

        try {
            DFService.register(this, agentDescription);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        /*
         * Random randomTime = new Random(); Random randomCharge = new Random();
         * Double timeTillUse = new Double(1000 * randomTime.nextDouble());
         * Double chargeNeeded = new Double(500 * randomCharge.nextDouble());
         */

        Random randomTime = new Random();

        Integer slotValue = new Integer(0);
        Integer timeNeeded = new Integer(10000);
        Integer timeTillUse = new Integer(10000);

        ds.put("slotValue", slotValue);
        ds.put("timeNeeded", timeNeeded);
        ds.put("timeTillUse", timeTillUse);

        carSuperBehaviour.setDataStore(ds);
        super.addBehaviour(carSuperBehaviour);
        carSuperBehaviour.addSubBehaviour(new GetInfo());

        // Instanciate the gui
        myGui = new CarGui(this, (Integer) this.ds.get("slotValue"));
        myGui.setVisible(true);

        super.addBehaviour(new CyclicBehaviour(this) {         
            
            private static final long serialVersionUID = -5221452177252946977L;

            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (!msg.getSender().equals(getAID()))
                        if (msg.getContent().contains("i need to charge")){
                            System.out.println(getLocalName() + " is changing");
                        }
                }
                else
                    block();
            }
        });
    }

    protected void onGuiEvent(GuiEvent ge) {
        command = ge.getType();
        if (command == QUIT) {
            alertGui("Bye!");
            doDelete();
            System.exit(0);
        }
        else if (command == 55) {
            ds.put("timeNeeded", (Integer) ge.getParameter(0));
            ds.put("timeTillUse", (Integer) ge.getParameter(1));
            sendInfo();
        }
    }

    void sendInfo() {
        addBehaviour(new InformWorld(ds.get("timeNeeded"), ds.get("timeTillUse")));
    }

    public void alertGui(String response) {
        myGui.alertResponse(response);
    }

    void resetStatusGui() {
        myGui.resetStatus();
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
        if (myGui != null) {
            myGui.setVisible(false);
            myGui.dispose();
        }
    }
}
