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
import behaviours.InformWorldAlt;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;


public class CarAgent extends GuiAgent {

    //initialise variables
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

	    //initialise variables
		Integer slotValue = new Integer(0);
		Integer slotPos = new Integer(999999);
		Integer timeNeeded = new Integer(999999);
		Integer timeTillUse = new Integer(999999);

	    //store variables inside a datastore
		ds.put("slotValue", slotValue);
		ds.put("slotPos", slotPos);
		ds.put("timeNeeded", timeNeeded);
		ds.put("timeTillUse", timeTillUse);

		//set data store and add behaviours
		carSuperBehaviour.setDataStore(ds);
		super.addBehaviour(carSuperBehaviour);

		carSuperBehaviour.addSubBehaviour(new CyclicBehaviour(this) {

			private static final long serialVersionUID = 908550917934326392L;

			@Override
			public void action() {

			    //if started charging start charging
				if (startFlag) {
					Integer newTimeNeeded = Integer.parseInt(ds.get(
							"timeNeeded").toString()) - 1;
					Integer newTimeTillUse = Integer.parseInt(ds.get(
							"timeTillUse").toString()) - 1;
					ds.put("timeNeeded", newTimeNeeded);
					ds.put("timeTillUse", newTimeTillUse);
					//if finished charging ask to be removed
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
					//update gui
					System.out.println("CHARGING");
					alertTimeNeeded(newTimeNeeded.toString());
					alertTimeTillUse(newTimeTillUse.toString());
					block(1000);
				}
			}
		});

		// Instantiate the gui
		myGui = new CarGui(this, (Integer) this.ds.get("slotValue"));
		myGui.setVisible(true);

		super.addBehaviour(new CyclicBehaviour(this) {

			private static final long serialVersionUID = -5221452177252946977L;

			public void action() {
			    //if message recieve perform actions
				ACLMessage msg = receive();
				if (msg != null) {
					if (msg.getContent() != null) {

						System.out.println(getLocalName() + " recieved: \""
								+ msg.getContent().toString() + "\" - from "
								+ msg.getSender().getLocalName());

						//if message is change algorithm switch algorithm
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

						// run through algorithm one and alert the gui
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

                        // run through algorithm two and alert the gui
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
                 					.replaceAll(" +", " ").trim()
									.replaceAll(" +", " ").split(" ", 3);
							Integer senderChargeBy = new Integer(values[0]);
							Integer senderChargeTime = new Integer(values[1]);
							Integer senderSlotValue = new Integer(values[2]);
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
							
							int highSlotValue;
							int lowSlotValue = 1000;
							int slotValueVector = 0;
							int directedChange = 0;
							int error = 0;
							int lowestError = 100;
							Random randomNum = new Random();
							int randomChange = randomNum.nextInt(3) - 2;
							//int iteration = 0;
							if (newSlotValue < senderSlotValue){
								highSlotValue = senderSlotValue;
								if (senderSlotValue < lowSlotValue)
								lowSlotValue = senderSlotValue;
								}
							else{
								highSlotValue = newSlotValue;
								if (newSlotValue < lowSlotValue)
								lowSlotValue = newSlotValue;
								}
							//if (iteration < 5){
							slotValueVector = highSlotValue + randomChange + directedChange;
							error = errorCalculation(highSlotValue,lowSlotValue);
							// iteration ++;
							if (error <= lowestError){
								highSlotValue = slotValueVector;
								directedChange = directedChange + 2*slotValueVector;
								error = errorCalculation(highSlotValue,lowSlotValue);
								lowestError = error;
							}else if(error >= lowestError){
								slotValueVector = highSlotValue - randomChange + directedChange;
								error = errorCalculation(highSlotValue,lowSlotValue);
							}
							newSlotValue = slotValueVector;
							//}
							
							ds.put("slotValue", newSlotValue);
							System.out.println(super.myAgent.getLocalName()
									+ " has a slot value of "
									+ ds.get("slotValue").toString());

							alertGuiSlot(ds.get("slotValue").toString());
						}

						//if asked to charge start charging
						if (msg.getContent().contains("you are charging")) {
							startFlag = true;
							System.out.println(getLocalName() + " is charging");
						}
                        //if asked to stop charging stop charging
						if (msg.getContent().contains(
								"sorry you will have to wait")) {
							System.out.println("not enough charge for "
									+ getLocalName());
							startFlag = false;
						}
                        //if asked for values return them
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
                        //if told values store them into a map an sort them. After sorted update slot position and update gui
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
						//Make sure the positions are not the same as any other car agent
						if (msg.getContent().contains("my slot position is ")) {
						    Integer otherSlotPos = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(" ") + 1));
						    if(otherSlotPos == ds.get("slotPos")){
						        System.out.println("THE SAME POS");
						        addBehaviour(new InformWorld(ds.get("timeNeeded"),ds.get("timeTillUse")));
						        }
						}
						//if asked for positions return them
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
                        //if asked to remove itself do it
                        if (msg.getContent().contains("remove yourself")) {
                            System.out.println(super.myAgent.getLocalName()
                                    + ": MESSAGE RECEIVED: " + msg.getContent()
                                    + " ---- From: "
                                    + msg.getSender().getLocalName());
                            takeDown();
                        }
					}
				} else
					block();
			}
		});
	}

	//setting up gui functionality
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

	//toggle algorithms
	void changeAlgorithms() {
		addBehaviour(new ChangeAlgorithm());
	}

	//send information to the other car agents 
	void sendInfo() {
		if (altFlag == false){
		addBehaviour(new InformWorld(ds.get("timeNeeded"),
				ds.get("timeTillUse")));
		carSuperBehaviour.addSubBehaviour(new AskSlotValues());
		}
		else
		{
		addBehaviour(new InformWorldAlt(ds.get("timeNeeded"),
				ds.get("timeTillUse"),
				ds.get("slotValue")));
		carSuperBehaviour.addSubBehaviour(new AskSlotValues());
		}
	}
	
	//calculate the error for use in alternate algorithm
	Integer errorCalculation(int highSlotValue, int lowSlotValue){
		Integer error = 0;
		error = highSlotValue + lowSlotValue;
		return error;
	}
    //send information to the GUI 
	public void alertGui(String response) {
		myGui.alertResponse(response);
	}

    //send slot value to the GUI 
	public void alertGuiSlot(String response) {
		myGui.alertSlot(response);
	}

    //send slot position to the GUI 
	public void alertGuiPos(String response) {
		myGui.alertPos(response);
	}

    //send Time Needed to the GUI 
	public void alertTimeNeeded(String response) {
		myGui.alertNeeded(response);
	}

    //send Time Till Use to the GUI 
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
