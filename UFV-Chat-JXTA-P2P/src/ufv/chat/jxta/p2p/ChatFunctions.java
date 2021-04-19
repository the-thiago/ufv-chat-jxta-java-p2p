package ufv.chat.jxta.p2p;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.peergroup.NetPeerGroupFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.rendezvous.RendezVousService;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatFunctions  {
//
//    private String message = null;
//    private NetworkManager networkManager = null;
//    private Vector<HashSet<PeerID>> peers;
//
//    public ChatFunctions(String message) throws Exception {
//        this.message = message;
//        networkManager = new NetworkManager(
//                NetworkManager.ConfigMode.RENDEZVOUS,
//                "NetworkName",
//                new File(new File(".cache"), "Chat").toURI()
//        );
//        NetworkConfigurator networkConfigurator = networkManager.getConfigurator();
//        networkConfigurator.setTcpEnabled(true);
//        networkConfigurator.setName("NetworkName");
//        String peerName = "Peer " + new Random().nextInt(1000000);
//        PeerID peerID = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, peerName.getBytes());
//        networkConfigurator.setPeerID(peerID);
//
////        networkManager.startNetwork();
//        peers = new Vector<HashSet<PeerID>>();
//    }

    public static final String CHAT_NAME_TAG = "ChatMack";
    private static final long ADV_LIFETIME = 210 * 1000;
    private static final String MESSAGE_TAG = "ChatSenderMessage";
    private static final String SENDER_NAME = "ChatSenderName";
    private static final int WAITING_TIME = 5 * 1000;
    private DiscoveryService discovery;
    private PeerGroup group;
    private boolean isFlushed;
    private boolean loggedIn;
    private String nick;
    private PipeService pipeSvc;
    private RendezVousService rdv;
    private Thread thread;
    private PipeAdvertisement userAdv;

    public static void main(String[] args) throws Exception {
        Logger.getLogger("net.jxta").setLevel(Level.ALL);
        new ChatFunctions().startJxta();
    }

    private void startJxta() {
        System.out.println("Configuring jxta");
        try {
//            group = PeerGroupFactory.newNetPeerGroup();
            group = new NetPeerGroupFactory().getWeakInterface();
            System.out.println("NetPeerGroup iniciado com sucesso...");
        } catch (Exception e) {
            System.out.println("Erro ao iniciar netPeerGroup:" + e.getMessage());
            System.exit(-1);
        }
        discovery = group.getDiscoveryService();
        rdv = group.getRendezVousService();
        System.out.println("Rendezvous service recovered...");
        while (rdv.isConnectedToRendezVous()) {
            try {
                System.out.println("Waiting connection with Rendezvous...");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Connection with Rendezvous established, recovering pipe service...");
        pipeSvc = group.getPipeService();
    }

    public void sendMessage(String message) {

    }

    public boolean login(String nickname) {

        return false;
    }

    public void sendMessageToAll(String texto) {
        ArrayList<PipeAdvertisement> pipes
                = getAllPipeLocalAdvertisements();
        if (pipes != null) {
            Iterator<PipeAdvertisement> it = pipes.iterator();
            while (it.hasNext()) {
                PipeAdvertisement adv = it.next();
                OutputPipe pipeOut = null;
                try {
                    pipeOut = pipeSvc.createOutputPipe(adv, 100);
                } catch (Exception e) {
                    System.out.println("Error creating OutputPipe to " + adv.getName());
                }
                if (pipeOut != null) {
                    Message msg;
                    try {
                        msg = (Message) pipeSvc.createInputPipe(adv, (PipeMsgListener) this);
//                        inputStream = new ByteArrayInputStream();
                        StringMessageElement sme =
                                new StringMessageElement(SENDER_NAME, nick, null);
                        StringMessageElement sme2 =
                                new StringMessageElement(MESSAGE_TAG, texto, null);
                        msg.replaceMessageElement(sme);
                        msg.replaceMessageElement(sme2);
                        pipeOut.send(msg);
                    } catch (Exception e) {
                        System.out.println("Error sending message");
                    }
                }
            }
        }
    }

    private ArrayList<PipeAdvertisement> getAllPipeLocalAdvertisements() {
        ArrayList<PipeAdvertisement> pipes
                = new ArrayList<>();
        try {
            Enumeration enu = discovery.getLocalAdvertisements(
                    DiscoveryService.ADV,
                    "Name",
                    "*" + CHAT_NAME_TAG + ":*"
            );
            if (enu != null) {
                while (enu.hasMoreElements()) {
                    try {
                        PipeAdvertisement adv =
                                (PipeAdvertisement) enu.nextElement();
                        pipes.add(adv);
                    } catch (Exception e) {
                        System.out.println("Error");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error retrieving advertisements cache");
        }
        return pipes;
    }

}
