
package behaviours;

import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

public class ReplyPower extends SimpleBehaviour {

    private static final long serialVersionUID = 6402450030258810914L;
    boolean finished = false;

    @Override
    public void action() {
        ACLMessage message = super.myAgent.receive();
        if (message != null) {
            // Handle the message.
            if (message.getContent().contains("do you have enough charge for")) {
                System.out.println(super.myAgent.getLocalName() + ": MESSAGE RECEIVED: "
                        + message.getContent() + " ---- From: "
                        + message.getSender().getLocalName());

                Double chargeNeeded = Double.parseDouble(message.getContent().substring(
                        message.getContent().lastIndexOf(" ") + 1));
                Double powerLimit = Double.parseDouble(super.getParent().getDataStore()
                        .get("powerLimit").toString());
                Double currentPower = Double.parseDouble(super.getParent().getDataStore()
                        .get("currentPower").toString());
                
                finished = true;

                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                if ((currentPower + chargeNeeded) < powerLimit)
                    reply.setContent("yes there is enough power");
                else
                    reply.setContent("sorry there is not enough power");
                super.myAgent.send(reply);
            }
        }
    }

    @Override
    public boolean done() {
        // TODO Auto-generated method stub
        return finished;
    }

}
