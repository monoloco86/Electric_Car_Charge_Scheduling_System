
package behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class ReplySlotValues extends CyclicBehaviour {

    private static final long serialVersionUID = -8509919630320776207L;

    @Override
    public void action() {
        ACLMessage message = super.myAgent.receive();
        if (message != null) {
            // Handle the message.
            if (!message.getSender().equals(super.myAgent.getAID())) {
                if (message.getContent().contains("what are your slot values")) {
                    System.out.println(super.myAgent.getLocalName() + ": MESSAGE RECEIVED: "
                            + message.getContent() + " ---- From: "
                            + message.getSender().getLocalName());
                    
                    Integer slotValue = Integer.parseInt(super.getParent().getDataStore().get("slotValue").toString());
                    ACLMessage reply = message.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    if (slotValue != null)
                        reply.setContent("my slot value is " + slotValue);
                    else
                        reply.setContent("slotValue not set");
                    super.myAgent.send(reply);
                }
            }
        } else
            block();
    }
}
