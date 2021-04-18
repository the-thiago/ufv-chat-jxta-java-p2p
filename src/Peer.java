import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

public class Peer {

    private String message = null;
    private NetworkManager networkManager = null;
    private Vector<HashSet<PeerID>> peers;

    public Peer(String message) throws Exception {
        this.message = message;
        networkManager = new NetworkManager(
                NetworkManager.ConfigMode.RENDEZVOUS,
                "NetworkName",
                new File(new File(".cache"), "Chat").toURI()
        );
        NetworkConfigurator networkConfigurator = networkManager.getConfigurator();
        networkConfigurator.setTcpEnabled(true);
        networkConfigurator.setName("NetworkName");
        String peerName = "Peer " + new Random().nextInt(1000000);
        PeerID peerID = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, peerName.getBytes());
        networkConfigurator.setPeerID(peerID);

//        networkManager.startNetwork();
        peers = new Vector<HashSet<PeerID>>();
    }

    public static void main(String[] args) throws Exception {
        Peer newPeer = new Peer("olá");

    }

}
