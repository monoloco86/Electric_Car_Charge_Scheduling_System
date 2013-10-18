package behaviours;

import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class Initialise extends OneShotBehaviour {

	/**
     * 
     */
    private static final long serialVersionUID = 4910212871030772229L;
    
    private final String agentName;
	private final String agentClass;
	private final Object[] argsToPass;
	
	public Initialise(String agentNameIn, String agentClassIn, Object[] argsToPassIn) {
		agentName = agentNameIn;
		agentClass = agentClassIn;
		argsToPass = argsToPassIn;
	}

	/*
	 * Deploy an agent.
	 * (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		
		/*
		 * Get the owner agent's container.
		 * Import from jade.Wrapper
		 */
		AgentContainer containerController = super.myAgent.getContainerController();
    	try {
    	
    		/*
    		 * Initialise an agent.
    		 * Import form jade.Wrapper
    		 */
    		AgentController agentController = 
    				containerController.createNewAgent(agentName, agentClass, argsToPass);
    		agentController.start();
    	}
    	catch (Exception e){e.printStackTrace();}

		/*
		 * Announce initialisation of an agent.
		 */
		System.out.println(super.myAgent.getLocalName() + ": " + agentName + " initialised.");
	}

}
