package agents;

import gui.SummaryGui;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import behaviours.AskSlotValues;
import behaviours.GetInfo;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

import java.util.Comparator;

public class SummaryAgent extends GuiAgent {

    private static final long serialVersionUID = -37780069230983858L;

    transient protected SummaryGui myGui;
    
    static final int WAIT = -1;
    final static int EXIT_SIGNAL = 0;
    final static int UPDATE_SIGNAL = 65;
    final static int ALT_SIGNAL = 70;
    private int command = WAIT;
    Integer slotInt;

    SequentialBehaviour summarySuperBehaviour = new SequentialBehaviour();
    
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

        super.addBehaviour(summarySuperBehaviour);
        summarySuperBehaviour.addSubBehaviour(new GetInfo());

        // Instanciate the gui
        myGui = new SummaryGui(this);
        myGui.setVisible(true);

        super.addBehaviour(new CyclicBehaviour(this) {

            private static final long serialVersionUID = 1274196283439389278L;
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getContent().contains("my slot value is")) {
                        slotInt = Integer.parseInt(msg.getContent().substring(msg.getContent().lastIndexOf(" ") + 1));
                        System.out.println(msg.getSender().getLocalName() + " has a slot value of " + slotInt);
                        map.put(msg.getSender().getLocalName(), slotInt);
                        map = sortByValues(map);
                        for(Map.Entry<String, Integer> entry : map.entrySet()) {
                            System.out.println("LOOPING");
                            System.out.println(entry.getKey() + ": " + entry.getValue());
                        }
                        alertGui(map);
                    }
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
        else if (command == UPDATE_SIGNAL) {
            updateInfo();
        }
    }

    void updateInfo() {
        addBehaviour(new AskSlotValues());
    }

    public void alertGui(Object response) {
        myGui.alertResponse(response);
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
    
    public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator =  new Comparator<K>() {
            public int compare(K k1, K k2) {
                int compare = map.get(k2).compareTo(map.get(k1));
                if (compare == 0) return 1;
                else return compare;
            }
        };
        Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }    
}
