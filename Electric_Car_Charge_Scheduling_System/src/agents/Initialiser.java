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
		// super.addBehaviour(new Initialise("Sniffer","jade.tools.sniffer.Sniffer",new Object[]{ "*" }));

		/*
		 * Deploy Car agents.
		 */

		Integer carCounter = new Integer(0);
		Integer carAmount = new Integer(3);
		for (carCounter = 0; carCounter < carAmount; carCounter++) {
			super.addBehaviour(new Initialise("CarAgent" + carCounter.toString(),"agents.CarAgent", null));
		}
	}

}
