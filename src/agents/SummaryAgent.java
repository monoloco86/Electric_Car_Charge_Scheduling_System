
package agents;

import gui.SummaryGui;

import java.util.HashMap;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.SequentialBehaviour;
import behaviours.AskSlotValues;
import behaviours.GetInfo;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

public class SummaryAgent extends GuiAgent {

    private static final long serialVersionUID = -2481137036537418853L;

    transient protected SummaryGui myGui;

    private DataStore ds = new DataStore();

    static final int WAIT = -1;
    final static int EXIT_SIGNAL = 0;
    final static int UPDATE_SIGNAL = 65;
    final static int ALT_SIGNAL = 70;
    private int command = WAIT;
    Integer slotInt;

    SequentialBehaviour summarySuperBehaviour = new SequentialBehaviour();
    HashMap<String, Integer> hm;

    protected void setup() {

        /*
         * Register this agent with DF.
         */
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("SummaryAgent");
        serviceDescription.setName(super.getLocalName());

        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(super.getAID());
        agentDescription.addServices(serviceDescription);

        try {
            DFService.register(this, agentDescription);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        summarySuperBehaviour.setDataStore(ds);
        super.addBehaviour(summarySuperBehaviour);
        summarySuperBehaviour.addSubBehaviour(new GetInfo());

        // Instanciate the gui
        myGui = new SummaryGui(this);
        myGui.setVisible(true);

        super.addBehaviour(new CyclicBehaviour(this) {

            private static final long serialVersionUID = 1274196283439389278L;

            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getContent().contains("my slot value is")) {
                        slotInt = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(" ") + 1));
                        System.out.println(msg.getSender() + " has a slot value of " + slotInt);
                        ds.put(msg.getSender(), slotInt);
                    }
                }
                else
                    block();
            }
        });
    }

    protected void onGuiEvent(GuiEvent ge) {
        command = ge.getType();
        if (command == EXIT_SIGNAL) {
            alertGui("Bye!");
            doDelete();
            System.exit(EXIT_SIGNAL);
        }
        else if (command == UPDATE_SIGNAL) {
            updateInfo();
        }
    }

    void updateInfo() {
        addBehaviour(new AskSlotValues());
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
