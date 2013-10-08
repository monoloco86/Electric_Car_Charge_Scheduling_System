
package behaviours;

import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

public class ReplySlots extends SimpleBehaviour {

    private static final long serialVersionUID = -8948876678560026420L;
    boolean finished = false;

    @Override
    public void action() {
        ACLMessage message = super.myAgent.receive();
        if (message != null) {
            // Handle the message.
            if (message.getContent().contains("are there slots")) {
                finished = true;
                System.out.println(super.myAgent.getLocalName()
                        + ": MESSAGE RECEIVED: " + message.getContent()
                        + " ---- From: " + message.getSender().getLocalName());

                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                
                Integer carLimit = Integer.parseInt(super.getParent().getDataStore().get("carLimit").toString());
                Integer carsStored = Integer.parseInt(super.getParent().getDataStore().get("carsStored").toString());
                
                if(carsStored < carLimit)
                    reply.setContent("yes there's enough slots for " + super.getParent().getDataStore().get("carLimit").toString() + " cars");
                else
                    reply.setContent("Sorry no spots available");
                super.myAgent.send(reply);

            }
        }
    }

    @Override
    public boolean done() {
        return finished;
    }

}
