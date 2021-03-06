
package agents;

import util.MapUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import gui.TransformerGui;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import behaviours.AskSlotPositions;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

public class TransformerAgent extends GuiAgent {

    //initialise variables
    private static final long serialVersionUID = 797974564683033413L;

    transient protected TransformerGui myGui;

    static final int WAIT = -1;
    final static int EXIT_SIGNAL = 0;
    final static int UPDATE_SIGNAL = 67;
    final static int RAND_SIGNAL = 72;
    private int command = WAIT;

    private Integer energyLimit = new Integer(1000);
    private Integer energyPerCar = new Integer(100);
    private Integer currentEnergy = new Integer(0);
    private Integer slotInt;

    private String keyString;

    Map<String, Integer> map = new HashMap<String, Integer>();

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

        setQueueSize(0);

        // Instantiate the gui
        myGui = new TransformerGui(this, energyLimit);
        myGui.setVisible(true);

        super.addBehaviour(new CyclicBehaviour(this) {

            private static final long serialVersionUID = 3947683897113338999L;

            public void action() {

                ACLMessage msg = receive();
                if (msg != null) {
                    //figure out which cars to be charged or not and send a response
                    boolean inMap = false;
                    System.out.println(getLocalName() + " recieved: \""
                            + msg.getContent().toString() + "\" - from "
                            + msg.getSender().getLocalName());
                    if (msg.getContent().contains("my slot position is")) {
                        System.out.println(super.myAgent.getLocalName()
                                + ": MESSAGE RECEIVED: " + msg.getContent()
                                + " ---- From: "
                                + msg.getSender().getLocalName());

                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        slotInt = Integer.parseInt(msg.getContent().substring(
                                msg.getContent().lastIndexOf(" ") + 1));
                        
                        if (map.containsKey(msg.getSender().getLocalName().toString())) {
                            inMap = true;
                        }
                        
                        map.put(msg.getSender().getLocalName().toString(),
                                slotInt);

                        if (map.size() > 1)
                            map = MapUtil.sortByValueSmallest(map);

                        if ((energyPerCar + currentEnergy) > energyLimit && map.size() > 0) {
                            System.out.println("NOT ENOUGH ENERGY");

                            Entry<String, Integer> max = null;
                            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                                if (max == null || max.getValue() < entry.getValue()) {
                                    keyString = entry.getKey();
                                }
                            }

                            map.remove(keyString);
                            if (keyString == reply.getSender().getLocalName())
                                reply.setContent("sorry you will have to wait");
                            else {
                                //ask car to charge
                                reply.setContent("you are charging");
                                ServiceDescription serviceDescription = new ServiceDescription();
                                serviceDescription.setType("CarAgent");
                                DFAgentDescription agentDescription = new DFAgentDescription();
                                agentDescription
                                        .addServices(serviceDescription);

                                DFAgentDescription[] result = new DFAgentDescription[0];
                                try {
                                    result = DFService.search(super.myAgent,
                                            agentDescription);
                                } catch (FIPAException e) {
                                    e.printStackTrace();
                                }

                                //ask car to wait
                                if (result.length > 0) {

                                    ACLMessage removeMessage = new ACLMessage(
                                            ACLMessage.INFORM);
                                    for (DFAgentDescription agent : result) {
                                        if (agent.getName().getLocalName() == keyString)
                                            removeMessage.addReceiver(agent
                                                    .getName());
                                    }
                                    removeMessage
                                            .setContent("sorry you will have to wait");
                                    send(removeMessage);
                                }
                            }

                        } else {
                            if (!inMap) {
                                //ask car to charge
                                currentEnergy += energyPerCar;
                                reply.setContent("you are charging");
                                myGui.alertCurrent(currentEnergy);
                                System.out.println(msg.getSender().getLocalName() + " has a slot position of " + slotInt);
                                for (Map.Entry<String, Integer> entry : map
                                        .entrySet()) {
                                    System.out.println("LOOPING");
                                    System.out.println(entry.getKey() + ": "
                                            + entry.getValue());
                                }
                            }
                        }
                        System.out.println("UPDATING MAP");
                        alertGui(map);
                        super.myAgent.send(reply);
                    }
                    //remove car from queue
                    if (msg.getContent().contains("you can remove me")) {
                        System.out.println(msg.getSender().getLocalName()
                                .toString()
                                + " wants to be removed");

                        Map<String, Integer> map2 = new HashMap<String, Integer>(map);
                        for (Map.Entry<String, Integer> entry : map.entrySet()) {
                            if (entry.getKey().contains(
                                    msg.getSender().getLocalName().toString())) {
                                System.out.println("Removing " + entry.getKey());
                                map2.remove(entry.getKey());
                                currentEnergy -= energyPerCar;
                                myGui.alertCurrent(currentEnergy);
                            }
                        }
                        map=map2;
                        alertGui(map);
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("you have been removed");
                        super.myAgent.send(reply);
                    }
                } else
                    block();
            }
        });
    }

    //provide gui functionality
    protected void onGuiEvent(GuiEvent ge) {
        command = ge.getType();
        if (command == EXIT_SIGNAL) {
            alertGui("Bye!");
            doDelete();
            System.exit(EXIT_SIGNAL);
        } else if (command == UPDATE_SIGNAL) {
            System.out.println("UPDATING");
            sendInfo();
        } else if (command == RAND_SIGNAL) {
            System.out.println("RANDOMING");
            randCharge();
        }
    }

    //ask cars for information
    void sendInfo() {
        map.clear();
        addBehaviour(new AskSlotPositions());
    }

    //randomise charge limit
    void randCharge() {
        energyLimit = random.nextInt(2000);
        while (energyLimit < currentEnergy)
            energyLimit = random.nextInt(2000);
        System.out.println(getLocalName() + " has an energy limit of "
                + energyLimit);
        myGui.alertLimit(energyLimit);
    }

    //send map to the gui     
    public void alertGui(Object response) {
        myGui.alertResponse(response);
    }

    //send limit to the gui
    public void alertGuiLimit(Integer response) {
        myGui.alertLimit(response);
    }

    //send current energy used tothe gui 
    public void alertGuiCurrent(Integer response) {
        myGui.alertCurrent(response);
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
