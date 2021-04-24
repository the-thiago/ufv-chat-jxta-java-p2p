package ufv.chat.jxta.p2p;

import java.util.ArrayList;

public class MessageRelay {

    private String username;
    private final ArrayList<String[]> incomingMessages;
    private final ArrayList<String> outgoingMessages;
    private final ArrayList<String> usernames;

    public MessageRelay() {
        incomingMessages = new ArrayList<>();
        outgoingMessages = new ArrayList<>();
        usernames = new ArrayList<>();
    }

    public synchronized void addIncomingMessage(String[] msg) {
        incomingMessages.add(msg);
    }

    public synchronized ArrayList<String[]> getIncomingMessages() {
        ArrayList<String[]> incomingCopy = new ArrayList<>(incomingMessages);
        incomingMessages.clear();
        return incomingCopy;
    }

    public synchronized void addOutgoingMessage(String msg) {
        outgoingMessages.add(msg);
    }

    public synchronized ArrayList<String> getOutgoingMessages() {
        ArrayList<String> outgoingCopy = new ArrayList<>(outgoingMessages);
        outgoingMessages.clear();
        return outgoingCopy;
    }

    public synchronized void setName(String name) {
        this.username = name;
    }

    public synchronized String getName() {
        return username;
    }

    public synchronized void addUser(String newUsername) {
        if (!usernames.contains(newUsername)) {
            usernames.add(newUsername);
        }
    }

    public synchronized ArrayList<String> getUsernames() {
        return usernames;
    }
}
