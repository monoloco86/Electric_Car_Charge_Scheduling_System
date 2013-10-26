package agents;

import util.MapUtil;
import gui.SummaryGui;

import java.util.HashMap;
import java.util.Map;

import jade.core.behaviours.CyclicBehaviour;
import behaviours.AskSlotPositions;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;


public class SummaryAgent extends GuiAgent {

	private static final long serialVersionUID = -37780069230983858L;

	transient protected SummaryGui myGui;

	static final int WAIT = -1;
	final static int EXIT_SIGNAL = 0;
	final static int UPDATE_SIGNAL = 66;
	final static int ALT_SIGNAL = 71;
	private int command = WAIT;
	Integer slotInt;

	Map<String, Integer> map = new HashMap<String, Integer>();

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

		setQueueSize(0);

		// Instanciate the gui
		myGui = new SummaryGui(this);
		myGui.setVisible(true);

		super.addBehaviour(new CyclicBehaviour(this) {

			private static final long serialVersionUID = 1274196283439389278L;

			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					System.out.println(getLocalName() + " recieved: \""
							+ msg.getContent().toString() + "\" - from "
							+ msg.getSender().getLocalName());
					if (msg.getContent().contains("my slot value is")) {
						slotInt = Integer.parseInt(msg.getContent().substring(
								msg.getContent().lastIndexOf(" ") + 1));
						System.out.println(msg.getSender().getLocalName()
								+ " has a slot value of " + slotInt);
						map.put(msg.getSender().getLocalName().toString(),
								slotInt);

						System.out.println("Before sort");
						for (Map.Entry<String, Integer> entry : map.entrySet()) {
							System.out.println("LOOPING");
							System.out.println(entry.getKey() + ": "
									+ entry.getValue());
						}

						if (map.size() > 1)
							map = MapUtil.sortByValueLargest(map);

						System.out.println("After sort");
						for (Map.Entry<String, Integer> entry : map.entrySet()) {
							System.out.println("LOOPING");
							System.out.println(entry.getKey() + ": "
									+ entry.getValue());
						}
						alertGui(map);
					}
				} else
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
		} else if (command == UPDATE_SIGNAL) {
			System.out.println("UPDATING");
			updateInfo();
		}
	}

	void updateInfo() {
		map.clear();
		addBehaviour(new AskSlotPositions());
	}

	public void alertGui(Object response) {
		myGui.alertResponse(response);
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
