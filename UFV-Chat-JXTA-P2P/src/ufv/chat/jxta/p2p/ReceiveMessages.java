package ufv.chat.jxta.p2p;

import java.util.ArrayList;

public class ReceiveMessages implements Runnable {

    private MessageRelay messages;
    private final ArrayList<String> knownUsers;

    public ReceiveMessages(MessageRelay messages) {
        knownUsers = new ArrayList<>();
    }

    @Override
    public void run() {
        while (true) {
            ArrayList<String[]> msgs = messages.getIncomingMessages();
            if (!msgs.isEmpty()) {
                for (String s[] : msgs) {
                    System.out.println(s[0] + ": " + s[1]);
                }
            }
            ArrayList<String> usernames = messages.getUsernames();
            for (String username : usernames) {
                if (!knownUsers.contains(username)) {
                    knownUsers.add(username);
                    System.out.println(username + " joined the chat!");
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //System.out.println(e.getMessage());
            }
        }
    }

}
