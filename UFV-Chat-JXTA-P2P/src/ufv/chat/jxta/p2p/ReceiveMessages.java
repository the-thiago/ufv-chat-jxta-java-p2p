package ufv.chat.jxta.p2p;

import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ReceiveMessages implements Runnable, ActionListener {

    private MessageRelay messages;
    private final ArrayList<String> knownUsers;

    public ReceiveMessages(MessageRelay messages) {
        this.messages = messages;
        knownUsers = new ArrayList<>();

        JFrame frame = new JFrame();

        JButton button = new JButton("Connected Users");
        button.addActionListener(this);

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        panel.setLayout(new GridLayout(0, 1));
        panel.add(button);

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Chat P2P - UFV");
        frame.pack();
        frame.setVisible(true);

        new Thread(this).start();
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

    @Override
    public void actionPerformed(ActionEvent e) {
        String formattedMessage = "";
        for (String username : messages.getUsernames()) {
            formattedMessage = username + "\n";
        }
        if (formattedMessage.isEmpty()) {
            formattedMessage = "No user connected!";
        }
        JOptionPane.showMessageDialog(
                null,
                formattedMessage,
                "User Connected",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

}
