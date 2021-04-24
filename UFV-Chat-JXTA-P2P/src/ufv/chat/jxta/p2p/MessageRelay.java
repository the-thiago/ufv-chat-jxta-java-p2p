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

    public synchronized void addIncomingMessage(String[] message) {
        incomingMessages.add(message);
    }

    public synchronized ArrayList<String[]> getIncomingMessages() {
        ArrayList<String[]> incomingCopy = new ArrayList<>(incomingMessages);
        incomingMessages.clear();
        return incomingCopy;
    }

    public synchronized void addOutgoingMessage(String message) {
        outgoingMessages.add(message);
    }

    public synchronized ArrayList<String> getOutgoingMessages() {
        ArrayList<String> outgoingCopy = new ArrayList<>(outgoingMessages);
        outgoingMessages.clear();
        return outgoingCopy;
    }

    public synchronized void setUsername(String username) {
        this.username = username;
    }

    public synchronized String getUsername() {
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
