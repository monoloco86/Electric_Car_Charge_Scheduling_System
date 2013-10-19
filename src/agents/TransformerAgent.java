
package agents;

import java.util.Random;

import gui.TransformerGui;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import behaviours.AskSlotValues;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

public class TransformerAgent extends GuiAgent {

    private static final long serialVersionUID = 797974564683033413L;

    transient protected TransformerGui myGui;

    static final int WAIT = -1;
    final static int EXIT_SIGNAL = 0;
    final static int UPDATE_SIGNAL = 65;
    final static int RAND_SIGNAL = 70;
    private int command = WAIT;

    private Integer energyLimit = new Integer(1000);
    private Integer energyPerCar = new Integer(100);
    private Integer currentEnergy = new Integer(0);
    private Integer slotInt;

    Random random = new Random();

    SequentialBehaviour transformerSuperBehaviour = new SequentialBehaviour();

    protected void setup() {

        /*
         * Register this agent with DF.
         */
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("TransformerAgent");
        serviceDescription.setName(super.getLocalName());

        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(super.getAID());
        agentDescription.addServices(serviceDescription);

        try {
            DFService.register(this, agentDescription);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        // Instanciate the gui
        myGui = new TransformerGui(this, energyLimit);
        myGui.setVisible(true);

        super.addBehaviour(new CyclicBehaviour(this) {

            private static final long serialVersionUID = 3947683897113338999L;

            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getContent().contains("my slot value is")) {
                        System.out.println(super.myAgent.getLocalName()
                                + ": MESSAGE RECEIVED: "
                                + msg.getContent() + " ---- From: "
                                + msg.getSender().getLocalName());
                        slotInt = Integer.parseInt(msg.getContent().substring(
                                msg.getContent().lastIndexOf(" ") + 1));
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        if (slotInt > 0) {
                            if ((energyPerCar + currentEnergy) > energyLimit) {
                                reply.setContent("sorry you will have to wait");
                            } else {
                                currentEnergy += energyPerCar;
                                reply.setContent("you are charging");
                                myGui.alertCurrent(currentEnergy);
                            }
                            super.myAgent.send(reply);
                        }
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
            sendInfo();
        }
        else if (command == RAND_SIGNAL) {
            randCharge();
        }
    }

    void sendInfo() {
        addBehaviour(new AskSlotValues());
    }

    void randCharge() {
        energyLimit = random.nextInt(2000);
        while (energyLimit < currentEnergy)
            energyLimit = random.nextInt(2000);
        System.out.println(getLocalName() + " has an energy limit of " + energyLimit);
        myGui.alertLimit(energyLimit);
    }

    public void alertGui(String response) {
        myGui.alertResponse(response);
    }
    
    public void alertGuiLimit(Integer response) {
        myGui.alertLimit(response);
    }   
    
    public void alertGuiCurrent(Integer response) {
        myGui.alertCurrent(response);
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
