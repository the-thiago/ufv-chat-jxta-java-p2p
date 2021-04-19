package ufv.chat.jxta.p2p;

import net.jxta.document.AdvertisementFactory;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.NetPeerGroupFactory;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.util.JxtaBiDiPipe;

// set up a bidirectional and reliable communication
// between the peers using the JxtaServerPipe
public class ReliableChat {

    JxtaBiDiPipe biDiPipeServer;

    public ReliableChat() throws Exception {

//        biDiPipeServer = new JxtaServerPipe(
//                new NetPeerGroupFactory().getInterface(),
//                createPipeAdvertisement()
//        );
        biDiPipeServer = new JxtaBiDiPipe(
                new NetPeerGroupFactory().getInterface(),
                createPipeAdvertisement(),
                this::pipeMessageEvent);

//        biDiPipeServer.accept();


        // Creating the biderectional pipe in the edge peer?
        biDiPipeServer = new JxtaBiDiPipe(
                new NetPeerGroupFactory().getInterface(),
                ReliableChat.createPipeAdvertisement(),
                30000,
                this::pipeMessageEvent
        );
        if (biDiPipeServer.isBound()) {
            System.out.println("Log: Bidirecional pipe created!");
        }
    }

    public static PipeAdvertisement createPipeAdvertisement() {
        PipeAdvertisement pipeAdvertisement = (PipeAdvertisement) AdvertisementFactory
                .newAdvertisement(PipeAdvertisement.getAdvertisementType());
        PipeID pipeID = IDFactory
                .newPipeID(PeerGroupID.defaultNetPeerGroupID, "NetworkName".getBytes());
        return pipeAdvertisement;
    }

    public void pipeMessageEvent(PipeMsgEvent event) {
        Message receivedMessage = event.getMessage();
        MessageElement messageElement = receivedMessage
                .getMessageElement("Chatting", "Text");
        System.out.println(messageElement.toString());
    }

    public void sendMessage() throws Exception {
        Message message = new Message();
        StringMessageElement stringElement =
                new StringMessageElement("Text", "teste", null);
        message.addMessageElement("Chatting", stringElement);

        biDiPipeServer.sendMessage(message);
    }

}
