
package agents;

import gui.InitialiserGui;
import behaviours.Initialise;
import behaviours.RemoveCar;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

public class Initialiser extends GuiAgent {

    // initialise variables
    private static final long serialVersionUID = 8349492506914849055L;

    transient protected InitialiserGui myGui;

    static final int WAIT = -1;
    final static int EXIT_SIGNAL = 0;
    final static int ADD_SIGNAL = 69;
    final static int REMOVE_SIGNAL = 59;

    Integer carCounter = new Integer(0);
    private int command = WAIT;

    protected void setup() {

        /*
         * Register this agent with DF.
         */
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("Initialiser");
        serviceDescription.setName(super.getLocalName());

        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(super.getAID());
        agentDescription.addServices(serviceDescription);

        try {
            DFService.register(this, agentDescription);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        setQueueSize(0);

        /*
         * Deploy Sniffer agent. uncomment to add the sniffer
         */
        // super.addBehaviour(new
        // Initialise("Sniffer","jade.tools.sniffer.Sniffer",new
        // Object[]{"*"}));

        /*
         * Deploy Car agents.
         */

        // Create initial car agents
        Integer carAmount = new Integer(4);
        for (carCounter = 0; carCounter < carAmount; carCounter++) {
            super.addBehaviour(new Initialise("CarAgent" + carCounter.toString(),
                    "agents.CarAgent", null));
        }

        // create and show initialiser GUI
        myGui = new InitialiserGui(this, carCounter);
        myGui.setVisible(true);

        /*
         * Deploy Summary agents.
         */

        // Create initial summary agents
        Integer summaryCounter = new Integer(0);
        Integer summaryAmount = new Integer(1);
        for (summaryCounter = 0; summaryCounter < summaryAmount; summaryCounter++) {
            super.addBehaviour(new Initialise("SummaryAgent" + summaryCounter.toString(),
                    "agents.SummaryAgent", null));
        }

        /*
         * Deploy Transformer agents.
         */

        // Create initial transformer agents
        Integer transformerCounter = new Integer(0);
        Integer transformerAmount = new Integer(1);
        for (transformerCounter = 0; transformerCounter < transformerAmount; transformerCounter++) {
            super.addBehaviour(new Initialise("TransformerAgent" + transformerCounter.toString(),
                    "agents.TransformerAgent", null));
        }
        super.addBehaviour(new CyclicBehaviour(this) {

            private static final long serialVersionUID = -7532975404825550772L;

            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println(getLocalName() + " recieved: \""
                            + msg.getContent().toString() + "\" - from "
                            + msg.getSender().getLocalName());
                    if (msg.getContent().contains("im leaving")) {
                        carCounter--;
                        alertGuiCount(carCounter.toString());
                    }
                } else
                    block();
            }
        });
    }

    // Provide functions for gui events
    protected void onGuiEvent(GuiEvent ge) {
        command = ge.getType();
        if (command == EXIT_SIGNAL) {
            alertGui("Bye!");
            doDelete();
            System.exit(EXIT_SIGNAL);
        } else if (command == ADD_SIGNAL) {
            System.out.println("ADDING CAR AGENT");
            alertGui("Adding car agent");
            super.addBehaviour(new Initialise("CarAgent" + carCounter.toString(),
                    "agents.CarAgent", null));
            carCounter++;
            alertGui("Car added");
            alertGuiCount(carCounter.toString());
        } else if (command == REMOVE_SIGNAL) {
            removeCar();
        }
    }

    // Remove latest car agent
    public void removeCar() {
        if (carCounter > 0) {
            System.out.println("REMOVING CAR AGENT");
            alertGui("Removing car agent");
            addBehaviour(new RemoveCar(carCounter));
            carCounter--;
            alertGui("Car removed");
            alertGuiCount(carCounter.toString());
        }
        else
            alertGui("No cars to remove");            
    }

    // Send information to the gui
    public void alertGui(String response) {
        myGui.alertResponse(response);
    }

    // Send the amount of cars to the gui
    public void alertGuiCount(String response) {
        myGui.alertCount(response);
    }
}
