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
//		super.addBehaviour(new Initialise("Sniffer","jade.tools.sniffer.Sniffer",new Object[]{ "*" }));
		
		/*
		 * Deploy Tester agents.
		 */
//		super.addBehaviour(new Initialise("Tester1", "agents.Tester", null));
//		super.addBehaviour(new Initialise("Tester2", "agents.Tester", null));
        super.addBehaviour(new Initialise("Car1", "agents.Car", null));
//        super.addBehaviour(new Initialise("Car2", "agents.Car", null));
//        super.addBehaviour(new Initialise("Car3", "agents.Car", null));
//        super.addBehaviour(new Initialise("Car4", "agents.Car", null));
        super.addBehaviour(new Initialise("Scheduler1", "agents.Scheduler", null));
//        super.addBehaviour(new Initialise("Scheduler2", "agents.Scheduler", null));
        super.addBehaviour(new Initialise("Transformer1", "agents.Transformer", null));
//        super.addBehaviour(new Initialise("Transformer2", "agents.Transformer", null));
	}

}
