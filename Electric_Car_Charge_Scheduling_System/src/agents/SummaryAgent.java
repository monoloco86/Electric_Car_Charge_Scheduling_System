
package agents;

import gui.SummaryGui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.SequentialBehaviour;
import behaviours.AskSlotValues;
import behaviours.GetInfo;
import behaviours.InformWorld;
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
    static final int QUIT = 0;
    private int command = WAIT;

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

            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getContent().contains("my slot value is")) {
                        ds.put(msg.getSender(), Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(" ") + 1)));
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
            sendInfo();
        }
    }

    void sendInfo() {
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
