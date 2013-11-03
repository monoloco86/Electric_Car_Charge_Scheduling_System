package agents;

import util.MapUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import gui.CarGui;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.ParallelBehaviour;
import behaviours.AskSlotPositions;
import behaviours.AskSlotValues;
import behaviours.ChangeAlgorithm;
import behaviours.InformWorld;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

public class CarAgent extends GuiAgent {

	private static final long serialVersionUID = -2481137036537418853L;

	transient protected CarGui myGui;

	private DataStore ds = new DataStore();

	static final int WAIT = -1;
	final static int EXIT_SIGNAL = 0;
	final static int UPDATE_SIGNAL = 65;
	final static int STORE_SIGNAL = 55;
	final static int ALT_SIGNAL = 70;

	private int command = WAIT;
	private boolean startFlag = false;
	private boolean altFlag = false;

	ParallelBehaviour carSuperBehaviour = new ParallelBehaviour();

	Map<String, Integer> map = new HashMap<String, Integer>();

	protected void setup() {

		/*
		 * Register this agent with DF.
		 */
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType("CarAgent");
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

		Integer slotValue = new Integer(0);
		Integer slotPos = new Integer(999999);
		Integer timeNeeded = new Integer(999999);
		Integer timeTillUse = new Integer(999999);

		ds.put("slotValue", slotValue);
		ds.put("slotPos", slotPos);
		ds.put("timeNeeded", timeNeeded);
		ds.put("timeTillUse", timeTillUse);

		carSuperBehaviour.setDataStore(ds);
		super.addBehaviour(carSuperBehaviour);

		// carSuperBehaviour.addSubBehaviour(new AskSlotValues());
		carSuperBehaviour.addSubBehaviour(new CyclicBehaviour(this) {

			private static final long serialVersionUID = 908550917934326392L;

			@Override
			public void action() {

				if (startFlag) {
					Integer newTimeNeeded = Integer.parseInt(ds.get(
							"timeNeeded").toString()) - 1;
					Integer newTimeTillUse = Integer.parseInt(ds.get(
							"timeTillUse").toString()) - 1;
					ds.put("timeNeeded", newTimeNeeded);
					ds.put("timeTillUse", newTimeTillUse);
					if ((newTimeNeeded == 0) || (newTimeTillUse == 0)) {
						ds.put("timeNeeded", 0);
						ds.put("timeTillUse", 0);
						ServiceDescription serviceDescription = new ServiceDescription();
						serviceDescription.setType("TransformerAgent");
						DFAgentDescription agentDescription = new DFAgentDescription();
						agentDescription.addServices(serviceDescription);

						DFAgentDescription[] result = new DFAgentDescription[0];
						try {
							result = DFService.search(super.myAgent,
									agentDescription);
						} catch (FIPAException e) {
							e.printStackTrace();
						}

						if (result.length > 0) {

							ACLMessage message = new ACLMessage(
									ACLMessage.INFORM);
							for (DFAgentDescription agent : result) {
								message.addReceiver(agent.getName());
							}
							message.setContent("you can remove me");
							startFlag = false;
							super.myAgent.send(message);
						}
					}
					System.out.println("CHARGING");
					alertTimeNeeded(newTimeNeeded.toString());
					alertTimeTillUse(newTimeTillUse.toString());
					block(1000);
				}
			}
		});

		// Instanciate the gui
		myGui = new CarGui(this, (Integer) this.ds.get("slotValue"));
		myGui.setVisible(true);

		super.addBehaviour(new CyclicBehaviour(this) {

			private static final long serialVersionUID = -5221452177252946977L;

			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					if (msg.getContent() != null) {

						System.out.println(getLocalName() + " recieved: \""
								+ msg.getContent().toString() + "\" - from "
								+ msg.getSender().getLocalName());

						if (msg.getContent().contains("change algorithm")) {
							altFlag = !altFlag;
						}
						if (msg.getContent().contains("randomise your values")) {
							Random random = new Random();
							ds.put("timeNeeded", random.nextInt(200));
							ds.put("timeTillUse", random.nextInt(200));							
							addBehaviour(new InformWorld(ds.get("timeNeeded"),ds.get("timeTillUse")));
							carSuperBehaviour.addSubBehaviour(new AskSlotValues());
						}

						// ALGORITHM 1
						if (msg.getContent().contains("i need to charge")
								&& !altFlag) {
							System.out
									.println("Running Original Algorithm - altFlag = "
											+ altFlag);
							System.out.println(super.myAgent.getLocalName()
									+ ": MESSAGE RECEIVED: " + msg.getContent()
									+ " ---- From: "
									+ msg.getSender().getLocalName());
							String[] values = msg.getContent()
									.replaceAll("[\\D]", " ").trim()
									.replaceAll(" +", " ").split(" ", 2);
							Integer senderChargeBy = new Integer(values[0]);
							Integer senderChargeTime = new Integer(values[1]);
							Integer myChargeBy = Integer.parseInt(ds.get(
									"timeTillUse").toString());
							Integer myChargeTime = Integer.parseInt(ds.get(
									"timeNeeded").toString());
							Integer mySlotValue = Integer.parseInt(ds.get(
									"slotValue").toString());
							Integer newSlotValue = new Integer(mySlotValue);
							if (senderChargeBy == myChargeBy) {
								if (senderChargeTime == myChargeTime)
									newSlotValue = mySlotValue;
								else if (senderChargeTime < myChargeTime)
									newSlotValue = mySlotValue - 1;
								else
									newSlotValue = mySlotValue + 1;
							} else if (senderChargeBy < myChargeBy)
								newSlotValue = mySlotValue - 1;
							else
								newSlotValue = mySlotValue + 1;

							ds.put("slotValue", newSlotValue);
							System.out.println(super.myAgent.getLocalName()
									+ " has a slot value of "
									+ ds.get("slotValue").toString());

							alertGuiSlot(ds.get("slotValue").toString());
						}

						// ALGORITHM 2
						if (msg.getContent().contains("i need to charge")
								&& altFlag) {
							System.out
									.println("Running Alternative Algorithm - altFlag = "
											+ altFlag);
							System.out.println(super.myAgent.getLocalName()
									+ ": MESSAGE RECEIVED: " + msg.getContent()
									+ " ---- From: "
									+ msg.getSender().getLocalName());
							String[] values = msg.getContent()
									.replaceAll("[\\D]", " ").trim()
									.replaceAll(" +", " ").split(" ", 2);
							Integer senderChargeBy = new Integer(values[0]);
							Integer senderChargeTime = new Integer(values[1]);
							Integer myChargeBy = Integer.parseInt(ds.get(
									"timeTillUse").toString());
							Integer myChargeTime = Integer.parseInt(ds.get(
									"timeNeeded").toString());
							Integer mySlotValue = Integer.parseInt(ds.get(
									"slotValue").toString());
							Integer newSlotValue = new Integer(mySlotValue);
							if (senderChargeTime == myChargeTime) {
								if (senderChargeBy == myChargeBy)
									newSlotValue = mySlotValue;
								else if (senderChargeBy < myChargeBy)
									newSlotValue = mySlotValue - 1;
								else
									newSlotValue = mySlotValue + 1;
							} else if (senderChargeTime < myChargeTime)
								newSlotValue = mySlotValue - 1;
							else
								newSlotValue = mySlotValue + 1;

							ds.put("slotValue", newSlotValue);
							System.out.println(super.myAgent.getLocalName()
									+ " has a slot value of "
									+ ds.get("slotValue").toString());

							alertGuiSlot(ds.get("slotValue").toString());
						}

						if (msg.getContent().contains("you are charging")) {
							startFlag = true;
							System.out.println(getLocalName() + " is charging");
						}
						if (msg.getContent().contains(
								"sorry you will have to wait")) {
							System.out.println("not enough charge for "
									+ getLocalName());
							startFlag = false;
						}
						if (msg.getContent().contains(
								"what are your slot values")) {
							System.out.println(super.myAgent.getLocalName()
									+ ": MESSAGE RECEIVED: " + msg.getContent()
									+ " ---- From: "
									+ msg.getSender().getLocalName());

							Integer slotValue = Integer.parseInt(ds.get(
									"slotValue").toString());
							ACLMessage reply = msg.createReply();
							reply.setPerformative(ACLMessage.INFORM);
							if (slotValue != null) {
								reply.setContent("my slot value is "
										+ slotValue);
								System.out.println(getLocalName()
										+ ": my slot value is " + slotValue);
							} else {
								reply.setContent("slotValue not set");
								System.out.println(getLocalName()
										+ ": slotValue not set");
							}
							super.myAgent.send(reply);
						}
						if (msg.getContent().contains("my slot value is")) {
							Integer slotInt = Integer
									.parseInt(msg
											.getContent()
											.substring(
													msg.getContent()
															.lastIndexOf(" ") + 1));
							System.out.println(msg.getSender().getLocalName()
									+ " has a slot value of " + slotInt);
							map.put(msg.getSender().getLocalName().toString(),
									slotInt);

							map.put(getLocalName().toString(),
									(Integer) ds.get("slotValue"));

							if (map.size() > 1)
								map = MapUtil.sortByValueLargest(map);

							System.out.println("After sort");
							int count = 1;
							for (Map.Entry<String, Integer> entry : map
									.entrySet()) {
								System.out.println("LOOPING");
								System.out.println(entry.getKey() + ": "
										+ entry.getValue());
								if (!entry.getKey().toString()
										.contains(getLocalName().toString())) {
									count++;
								} else {
									ds.put("slotPos", count);
								}
							}
							System.out.println(getLocalName().toString()
									+ "'s slotPos is " + ds.get("slotPos"));
							alertGuiPos(ds.get("slotPos").toString());
							addBehaviour(new AskSlotPositions());
						}
						if (msg.getContent().contains("my slot position is ")) {
						    Integer otherSlotPos = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(" ") + 1));
						    if(otherSlotPos == ds.get("slotPos")){
						        System.out.println("THE SAME POS");
						        addBehaviour(new InformWorld(ds.get("timeNeeded"),ds.get("timeTillUse")));
						        }
						}
						if (msg.getContent().contains("what are your slot positions")) {
							System.out.println(super.myAgent.getLocalName()
									+ ": MESSAGE RECEIVED: " + msg.getContent()
									+ " ---- From: "
									+ msg.getSender().getLocalName());

							ACLMessage reply = msg.createReply();
							reply.setPerformative(ACLMessage.INFORM);
							if (ds.get("slotPos") != null) {
								reply.setContent("my slot position is "
										+ ds.get("slotPos").toString());
								System.out.println(getLocalName()
										+ ": my slot position is "
										+ ds.get("slotPos").toString());
							} else {
								reply.setContent("slotPos not set");
								System.out.println(getLocalName()
										+ ": slotPos not set");
							}
							super.myAgent.send(reply);
						}
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
		} else if (command == STORE_SIGNAL) {
			System.out.println("STORING");
			alertGui("Storing");
			ds.put("timeNeeded", (Integer) ge.getParameter(0));
			ds.put("timeTillUse", (Integer) ge.getParameter(1));
			System.out.println(getLocalName() + ": TIMENEEDED: "
					+ ds.get("timeNeeded"));
			System.out.println(getLocalName() + ": TIMETILLUSE: "
					+ ds.get("timeTillUse"));
		} else if (command == UPDATE_SIGNAL) {
			System.out.println("UPDATING");
			alertGui("Updating");
			sendInfo();
		} else if (command == ALT_SIGNAL) {
			System.out.println("CHANGING ALGORITHM");
			if (altFlag)
				alertGui("Changing to Orignal");
			else
				alertGui("Changing to Alternative");
			changeAlgorithms();
		}
	}

	void changeAlgorithms() {
		addBehaviour(new ChangeAlgorithm());
	}

	void sendInfo() {
		addBehaviour(new InformWorld(ds.get("timeNeeded"),
				ds.get("timeTillUse")));
		carSuperBehaviour.addSubBehaviour(new AskSlotValues());
	}

	public void alertGui(String response) {
		myGui.alertResponse(response);
	}

	public void alertGuiSlot(String response) {
		myGui.alertSlot(response);
	}

	public void alertGuiPos(String response) {
		myGui.alertPos(response);
	}

	public void alertTimeNeeded(String response) {
		myGui.alertNeeded(response);
	}

	public void alertTimeTillUse(String response) {
		myGui.alertUse(response);
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
