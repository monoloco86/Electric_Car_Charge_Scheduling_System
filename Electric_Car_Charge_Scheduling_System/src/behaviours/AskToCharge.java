
package behaviours;

import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

public class AskToCharge extends SimpleBehaviour{
    
    private static final long serialVersionUID = -255480920216569207L;
    boolean finished = false;

    @Override
    public void action() {        
        ACLMessage message = super.myAgent.receive();
        if (message != null) {
            // Handle the message.
            if (message.getContent().contains("yes") && message.getContent().contains("enough") && message.getContent().contains("slots")) {
                finished = true;
                System.out.println(super.myAgent.getLocalName() + ": MESSAGE RECEIVED: "
                        + message.getContent() + " ---- From: "
                        + message.getSender().getLocalName());
                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("can i charge " + super.getParent().getDataStore().get("chargeNeeded").toString());
                super.myAgent.send(reply);                 
            }
        }
    }

    @Override
    public boolean done() {
        return finished;
    }

}
