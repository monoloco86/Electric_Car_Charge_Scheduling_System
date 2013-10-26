
package agents;

import behaviours.Initialise;
import jade.core.Agent;

public class Initialiser extends Agent {

    private static final long serialVersionUID = 8349492506914849055L;

    protected void setup() {

        /*
         * Announce operation of this agent.
         */

        /*
         * Deploy Sniffer agent.
         */
         super.addBehaviour(new Initialise("Sniffer","jade.tools.sniffer.Sniffer",new Object[]{ "*"}));

        /*
         * Deploy Car agents.
         */

        Integer carCounter = new Integer(0);
        Integer carAmount = new Integer(4);
        for (carCounter = 0; carCounter < carAmount; carCounter++) {
            super.addBehaviour(new Initialise("CarAgent" + carCounter.toString(),
                    "agents.CarAgent", null));
        }

        /*
         * Deploy Summary agents.
         */

        Integer summaryCounter = new Integer(0);
        Integer summaryAmount = new Integer(1);
        for (summaryCounter = 0; summaryCounter < summaryAmount; summaryCounter++) {
            super.addBehaviour(new Initialise("SummaryAgent" + summaryCounter.toString(),
                    "agents.SummaryAgent", null));
        }

        /*
         * Deploy Summary agents.
         */

        Integer transformerCounter = new Integer(0);
        Integer transformerAmount = new Integer(1);
        for (transformerCounter = 0; transformerCounter < transformerAmount; transformerCounter++) {
            super.addBehaviour(new Initialise("TransformerAgent" + transformerCounter.toString(),
                    "agents.TransformerAgent", null));
        }
    }

}
