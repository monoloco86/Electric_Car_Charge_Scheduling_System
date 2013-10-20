
package agents;

import gui.CarGui;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.ParallelBehaviour;
import behaviours.GetInfo;
import behaviours.GetInfoAlt;
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

    Behaviour getInfo = new GetInfo();
    Behaviour getInfoAlt = new GetInfoAlt();

    ParallelBehaviour carSuperBehaviour = new ParallelBehaviour();

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
        /*
         * Random randomTime = new Random(); Random randomCharge = new Random();
         * Double timeTillUse = new Double(1000 * randomTime.nextDouble());
         * Double chargeNeeded = new Double(500 * randomCharge.nextDouble());
         */

        Integer slotValue = new Integer(0);
        Integer timeNeeded = new Integer(10000);
        Integer timeTillUse = new Integer(10000);

        ds.put("slotValue", slotValue);
        ds.put("timeNeeded", timeNeeded);
        ds.put("timeTillUse", timeTillUse);

        carSuperBehaviour.setDataStore(ds);
        super.addBehaviour(carSuperBehaviour);
        carSuperBehaviour.addSubBehaviour(getInfo);

        carSuperBehaviour.addSubBehaviour(new CyclicBehaviour(this) {

            private static final long serialVersionUID = 908550917934326392L;

            @Override
            public void action() {

                if (startFlag) {
                    Integer newTimeNeeded = Integer.parseInt(ds.get("timeNeeded").toString()) - 1;
                    Integer newTimeTillUse = Integer.parseInt(ds.get("timeTillUse").toString()) - 1;
                    ds.put("timeNeeded", newTimeNeeded);
                    ds.put("timeTillUse", newTimeTillUse);
                    if ((newTimeNeeded == 0) || (newTimeTillUse == 0)) {
                        ServiceDescription serviceDescription = new ServiceDescription();
                        serviceDescription.setType("TransformerAgent");
                        DFAgentDescription agentDescription = new DFAgentDescription();
                        agentDescription.addServices(serviceDescription);

                        DFAgentDescription[] result = new DFAgentDescription[0];
                        try {
                            result = DFService.search(super.myAgent, agentDescription);
                        } catch (FIPAException e) {
                            e.printStackTrace();
                        }

                        if (result.length > 0) {

                            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                            for (DFAgentDescription agent : result) {
                                message.addReceiver(agent.getName());
                            }
                            message.setContent("you can remove me");
                            startFlag = false;
                            super.myAgent.send(message);
                        }
                    }
                    System.out.println("I WOKE UP");
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
                    if (!msg.getSender().equals(getAID())) {
                        if (msg.getContent().contains("you are charging")){
                            startFlag = true;
                            System.out.println(getLocalName() + " is charging");
                        }
                        else if (msg.getContent().contains("sorry you will have to wait"))
                            System.out.println("not enough charge for " + getLocalName());
                        else if (msg.getContent().contains("what are your slot values")) {
                            System.out.println(super.myAgent.getLocalName()
                                    + ": MESSAGE RECEIVED: "
                                    + msg.getContent() + " ---- From: "
                                    + msg.getSender().getLocalName());

                            Integer slotValue = Integer.parseInt(ds.get("slotValue").toString());
                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.INFORM);
                            if (slotValue != null) {
                                reply.setContent("my slot value is " + slotValue);
                                System.out.println(getLocalName() + ": my slot value is "
                                        + slotValue);
                            }
                            else {
                                reply.setContent("slotValue not set");
                                System.out.println(getLocalName() + ": slotValue not set");
                            }
                            super.myAgent.send(reply);
                        }
                    }
                    else if (msg.getContent().contains("update gui slotvalue"))
                        alertGui(ds.get("slotValue").toString());
                }
                else
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
        }
        else if (command == STORE_SIGNAL) {
            System.out.println("STORING");
            ds.put("timeNeeded", (Integer) ge.getParameter(0));
            ds.put("timeTillUse", (Integer) ge.getParameter(1));
            System.out.println(getLocalName() + ": TIMENEEDED: " + ds.get("timeNeeded"));
            System.out.println(getLocalName() + ": TIMETILLUSE: " + ds.get("timeTillUse"));
        }
        else if (command == UPDATE_SIGNAL) {
            System.out.println("UPDATING");
            sendInfo();
        }
        else if (command == ALT_SIGNAL) {
            System.out.println("CHANGING ALGORITHM");
            changeAlgorithms();
        }
    }

    void changeAlgorithms() {
        if (carSuperBehaviour.getChildren().toString().toLowerCase().contains("getinfoalt")) {
            carSuperBehaviour.removeSubBehaviour(getInfoAlt);
            carSuperBehaviour.addSubBehaviour(getInfo);
        }
        else {
            carSuperBehaviour.removeSubBehaviour(getInfo);
            carSuperBehaviour.addSubBehaviour(getInfoAlt);
        }
    }

    void sendInfo() {
        addBehaviour(new InformWorld(ds.get("timeNeeded"), ds.get("timeTillUse")));
    }

    public void alertGui(String response) {
        myGui.alertResponse(response);
    }

    public void alertTimeNeeded(String response) {
        myGui.alertNeeded(response);
    }

    public void alertTimeTillUse(String response) {
        myGui.alertUse(response);
    }

    void resetStatusGui() {
        myGui.resetStatus();
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
