package ufv.chat.jxta.p2p;

import net.jxta.discovery.DiscoveryService;

public class DiscoveryPipes  implements Runnable {

    private final DiscoveryService discoveryService;

    public DiscoveryPipes(DiscoveryService discoveryService) throws Exception {
//        discoveryService = new NetPeerGroupFactory().getInterface().getDiscoveryService();
//        discoveryService.getRemoteAdvertisements(
//                null,
//                DiscoveryService.ADV,
//                null,
//                null,
//                5,
//                (DiscoveryListener) this
//        );
        this.discoveryService = discoveryService;
        Thread thread = new Thread(this);
        thread.start();
        System.out.println("Log: Thread para discovery de pipes criada");
    }

//    @Override
//    public void discoveryEvent(DiscoveryEvent discoveryEvent) {
//        DiscoveryResponseMsg response = discoveryEvent.getResponse();
//    }

    @Override
    public void run() {
        while (true) {
            try {
                discoveryService.getRemoteAdvertisements(
                        null,
                        DiscoveryService.ADV,
                        null,
                        null,
                        100
                );
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
