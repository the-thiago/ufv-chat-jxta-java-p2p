package ufv.chat.jxta.p2p;

import java.io.File;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

import java.util.HashSet;
import java.util.Random;
import javax.swing.JOptionPane;
import net.jxta.document.AdvertisementFactory;
import net.jxta.endpoint.MessageElement;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.PipeID;
import net.jxta.platform.Module;
import net.jxta.platform.ModuleClassID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.ModuleClassAdvertisement;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.ModuleSpecAdvertisement;

public class JxtaChat implements DiscoveryListener, PipeMsgListener {

    private static final String GROUP_NAME = "myP2Pchat";
    private static final String GROUP_DESC = "P2P chat";
    private static final PeerGroupID GROUP_ID = IDFactory.newPeerGroupID(
            PeerGroupID.defaultNetPeerGroupID, GROUP_NAME.getBytes()
    );
    private static final String UNICAST_NAME = "uniP2PChat";
    private static final String MULTICAST_NAME = "multiP2PChat";
    private static final String SERVICE_NAME = "P2PChat";

    private final NetworkManager manager;
    private PeerGroup netPeerGroup;
    private final ArrayList<HashSet<PeerID>> peers;
    private final MessageRelay messages;
    private final String peerName;
    private final PeerID peerID;
    private PeerGroup chatGroup;
    private PipeID unicastID;
    private PipeID multicastID;
    private PipeID serviceID;
    private PipeService pipeService;
    private DiscoveryService discovery;

    public JxtaChat(MessageRelay messages) throws Exception {
        this.peers = new ArrayList<>();
        this.messages = messages;

        this.peerName = "Peer " + new Random().nextInt(1000000);
        this.peerID = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, peerName.getBytes());

        this.manager = new NetworkManager(
                NetworkManager.ConfigMode.ADHOC,
                peerName,
                new File(new File(".cache"), "Chat").toURI()
        );

        int port = 9000 + new Random().nextInt(1000);

        NetworkConfigurator config = manager.getConfigurator();
        config.setTcpPort(port);
        config.setTcpEnabled(true);
        config.setTcpIncoming(true);
        config.setTcpOutgoing(true);
        config.setUseMulticast(true);
        config.setPeerID(peerID);
    }

    public static void main(String[] args) {
        MessageRelay messages = new MessageRelay();

        String name = (String) JOptionPane.showInputDialog(
                null,
                "Please enter a username",
                "Welcome to Chat P2P - UFV",
                JOptionPane.QUESTION_MESSAGE
        );

        if (name.isEmpty()) {
            name = "user" + new Random().nextInt(1000000);
        }
        messages.setUsername(name);

        try {
            JxtaChat chat = new JxtaChat(messages);
            chat.start();
            chat.fetchAdvertisements();
            chat.sendMessages();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.err.println("Error while starting Jxta!");
            return;
        }
        new ReceiveMessagesView(messages);
    }

    public void start() {
        try {
            netPeerGroup = manager.startNetwork();
        } catch (IOException | PeerGroupException e) {
            //System.out.println(e.getMessage());
        }

        ModuleImplAdvertisement mAdv;

        try {
            mAdv = netPeerGroup.getAllPurposePeerGroupImplAdvertisement();
            chatGroup = netPeerGroup.newGroup(GROUP_ID, mAdv, GROUP_NAME, GROUP_DESC);
        } catch (Exception e) {
            //System.err.println(e.getMessage());
        }

        if (Module.START_OK != chatGroup.startApp(new String[0])) {
            System.err.println("Cannot start child peergroup");
        }

        unicastID = IDFactory.newPipeID(chatGroup.getPeerGroupID(), UNICAST_NAME.getBytes());
        multicastID = IDFactory.newPipeID(chatGroup.getPeerGroupID(), MULTICAST_NAME.getBytes());

        pipeService = chatGroup.getPipeService();
        try {
            pipeService.createInputPipe(getAdvertisement(unicastID, false), this);
            pipeService.createInputPipe(getAdvertisement(multicastID, true), this);
        } catch (IOException e) {
            //System.out.println(e.getMessage());
        }

        discovery = chatGroup.getDiscoveryService();
        discovery.addDiscoveryListener(this);

        ModuleClassAdvertisement mcadv = (ModuleClassAdvertisement) AdvertisementFactory.newAdvertisement(ModuleClassAdvertisement.getAdvertisementType());
        mcadv.setName("P2PChat");
        mcadv.setDescription("P2PChat Module Advertisement");

        ModuleClassID mcID = IDFactory.newModuleClassID();
        mcadv.setModuleClassID(mcID);

        try {
            discovery.publish(mcadv);
        } catch (IOException e) {
            //System.out.println(e.getMessage());
        }
        discovery.remotePublish(mcadv);

        ModuleSpecAdvertisement mdadv = (ModuleSpecAdvertisement) AdvertisementFactory.newAdvertisement(ModuleSpecAdvertisement.getAdvertisementType());
        mdadv.setName("P2PChat");
        mdadv.setVersion("Version 1.0");
        mdadv.setCreator("sun.com");
        mdadv.setModuleSpecID(IDFactory.newModuleSpecID(mcID));
        mdadv.setSpecURI("http://www.jxta.org/Ex1");

        serviceID = (PipeID) IDFactory.newPipeID(chatGroup.getPeerGroupID(), SERVICE_NAME.getBytes());
        PipeAdvertisement pipeAdv = getAdvertisement(serviceID, false);
        mdadv.setPipeAdvertisement(pipeAdv);

        try {
            discovery.publish(mdadv);
            discovery.remotePublish(mdadv);
            pipeService.createInputPipe(pipeAdv, this);
        } catch (IOException e) {
            //System.out.println(e.getMessage());
        }
    }

    private void fetchAdvertisements() {
        new Thread("fetch advertisements thread") {
            @Override
            public void run() {
                while (true) {
                    discovery.getRemoteAdvertisements(null, DiscoveryService.ADV, "Name", "P2PChat", 1, null);
                    try {
                        sleep(10000);

                    } catch (InterruptedException e) {
                    }
                }
            }
        }.start();
    }

    public void sendMessages() {
        new Thread("Message sender thread") {
            @Override
            public void run() {
                while (true) {
                    // Create Message
                    PipeAdvertisement pipeAdv = getAdvertisement(unicastID, false);
                    ArrayList<String> msgs = messages.getOutgoingMessages();
                    if (!msgs.isEmpty()) {
                        MessageElement from = new StringMessageElement("from", messages.getUsername(), null);
                        try {
                            for (HashSet<PeerID> pids : peers) {
                                OutputPipe out = pipeService.createOutputPipe(pipeAdv, pids, 0);
                                for (String s : msgs) {
                                    Message msg = new Message();
                                    MessageElement body = new StringMessageElement("body", s, null);
                                    msg.addMessageElement(from);
                                    msg.addMessageElement(body);
                                    out.send(msg);
                                    System.out.println(from + "(you): " + body);
                                }
                            }
                        } catch (IOException e) {
                            //System.out.println(e.getMessage());
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //System.out.println(e.getMessage());
                    }
                }
            }
        }.start();
    }

    private static PipeAdvertisement getAdvertisement(PipeID id, boolean isMulticast) {
        PipeAdvertisement adv = (PipeAdvertisement) AdvertisementFactory.
                newAdvertisement(PipeAdvertisement.getAdvertisementType());
        adv.setPipeID(id);
        if (isMulticast) {
            adv.setType(PipeService.PropagateType);
        } else {
            adv.setType(PipeService.UnicastType);
        }
        adv.setName("P2PChatPipe");
        adv.setDescription("Pipe for p2p chat messages");
        return adv;
    }

    @Override
    public void pipeMsgEvent(PipeMsgEvent event) {
        System.out.println("Received!");

        Message msg = event.getMessage();
        Object user = msg.getMessageElement("username");
        if (user != null) {
            messages.addUser(user.toString());
        } else {
            String content[] = new String[2];
            content[0] = msg.getMessageElement("from").toString();
            content[1] = msg.getMessageElement("body").toString();
            messages.addIncomingMessage(content);
        }
    }

    @Override
    public void discoveryEvent(DiscoveryEvent event) { // Peer/pipe found
        String addr = "urn:jxta:" + event.getSource().toString().substring(7);
        PeerID peer;
        try {
            URI uri = new URI(addr);
            peer = (PeerID) IDFactory.fromURI(uri);
            HashSet<PeerID> pids = new HashSet<>();
            pids.add(peer);
            if (!peers.contains(pids)) {
                peers.add(pids);
                sendUsername(pids);
            }
        } catch (URISyntaxException e) {
            //System.out.println(e.getMessage());
        }
    }

    private void sendUsername(HashSet<PeerID> pids) {
        PipeAdvertisement pipeAdv = getAdvertisement(unicastID, false);
        try {
            OutputPipe out = pipeService.createOutputPipe(pipeAdv, pids, 10000);
            Message msg = new Message();
            MessageElement username = new StringMessageElement("username", messages.getUsername(), null);
            msg.addMessageElement(username);
            out.send(msg);
        } catch (IOException e) {
            //System.out.println(e.getMessage());
        }
    }

}
