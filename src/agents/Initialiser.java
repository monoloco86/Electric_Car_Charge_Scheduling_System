
package agents;

import gui.InitialiserGui;
import behaviours.Initialise;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

public class Initialiser extends GuiAgent {

    //initialise variables
    private static final long serialVersionUID = 8349492506914849055L;

    transient protected InitialiserGui myGui;

    static final int WAIT = -1;
    final static int EXIT_SIGNAL = 0;
    final static int ADD_SIGNAL = 69;

    Integer carCounter = new Integer(0);
    private int command = WAIT;

    protected void setup() {

        /*
         * Announce operation of this agent.
         */

        /*
         * Deploy Sniffer agent. uncomment to add the sniffer
         */
        // super.addBehaviour(new Initialise("Sniffer","jade.tools.sniffer.Sniffer",new Object[]{"*"}));

        /*
         * Deploy Car agents.
         */

        //Create initial car agents
        Integer carAmount = new Integer(3);
        for (carCounter = 0; carCounter < carAmount; carCounter++) {
            super.addBehaviour(new Initialise("CarAgent" + carCounter.toString(),
                    "agents.CarAgent", null));
        }

        //create and show initialiser GUI
        myGui = new InitialiserGui(this, carCounter);
        myGui.setVisible(true);

        /*
         * Deploy Summary agents.
         */

        //Create initial summary agents
        Integer summaryCounter = new Integer(0);
        Integer summaryAmount = new Integer(1);
        for (summaryCounter = 0; summaryCounter < summaryAmount; summaryCounter++) {
            super.addBehaviour(new Initialise("SummaryAgent" + summaryCounter.toString(),
                    "agents.SummaryAgent", null));
        }

        /*
         * Deploy Transformer agents.
         */

        //Create initial transformer agents
        Integer transformerCounter = new Integer(0);
        Integer transformerAmount = new Integer(1);
        for (transformerCounter = 0; transformerCounter < transformerAmount; transformerCounter++) {
            super.addBehaviour(new Initialise("TransformerAgent" + transformerCounter.toString(),
                    "agents.TransformerAgent", null));
        }
    }

    //Provide functions for gui events
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
        }
    }

    //Send information to the gui
    public void alertGui(String response) {
        myGui.alertResponse(response);
    }

    //Send the amount of cars to the gui
    public void alertGuiCount(String response) {
        myGui.alertCount(response);
    }
}
