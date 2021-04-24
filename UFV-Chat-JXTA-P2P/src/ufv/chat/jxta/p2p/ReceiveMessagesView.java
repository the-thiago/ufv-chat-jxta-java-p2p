package ufv.chat.jxta.p2p;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

public class ReceiveMessagesView implements Runnable, ActionListener, KeyListener, FocusListener {

    private MessageRelay messages;
    private final ArrayList<String> knownUsers;

    JTextField editText;
    JTextArea chatMessages;

    public ReceiveMessagesView(MessageRelay messages) {
        this.messages = messages;
        knownUsers = new ArrayList<>();
        setVisualInterface();
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (true) {
            ArrayList<String[]> msgs = messages.getIncomingMessages();
            if (!msgs.isEmpty()) {
                for (String s[] : msgs) {
                    System.out.println(s[0] + ": " + s[1]);
                    chatMessages.append(Util.getFormattedTime() + " - " + 
                            s[0] + ": " + s[1] + "\n");
                }
            }
            ArrayList<String> usernames = messages.getUsernames();
            for (String username : usernames) {
                if (!knownUsers.contains(username)) {
                    knownUsers.add(username);
                    System.out.println(username + " joined the chat!");
                    chatMessages.append(Util.getFormattedTime() + " - " + 
                            username + " joined the chat!\n");
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //System.out.println(e.getMessage());
            }
        }
    }

    private void setVisualInterface() {
        // Setting Connected Users Button
        JButton button = new JButton("Known Users");
        button.addActionListener(this);
        button.setPreferredSize(new Dimension(80, 80));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Setting Chat Messages Area
        chatMessages = new JTextArea(100, 10);
        chatMessages.setEditable(false);
        chatMessages.setFont(new Font("SansSerif", Font.PLAIN, 15));
        JScrollPane scrollableView = new JScrollPane(
                chatMessages,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollableView.setBorder(
                BorderFactory.createCompoundBorder(
                        new EmptyBorder(10, 0, 10, 0),
                        new EtchedBorder()
                )
        );

        // Setting Message Edit Text
        editText = new JTextField(300);
        editText.addKeyListener(this);
        editText.setText("Type something...");
        editText.addFocusListener(this);
        editText.setFont(new Font("SansSerif", Font.PLAIN, 15));

        // Setting Panel
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(button, BorderLayout.NORTH);
        panel.add(scrollableView, BorderLayout.CENTER);
        panel.add(editText, BorderLayout.SOUTH);

        // Setting Frame
        JFrame frame = new JFrame();
        frame.add(panel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Chat P2P - UFV");
        frame.pack();
        frame.setVisible(true);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String formattedMessage = "";
        for (String username : messages.getUsernames()) {
            formattedMessage += username + "\n";
        }
        if (formattedMessage.isEmpty()) {
            formattedMessage = "No known users!";
        }
        JOptionPane.showMessageDialog(
                null,
                formattedMessage,
                "Users Connected",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            String message = editText.getText();
            if (!message.isEmpty()) {
                messages.addOutgoingMessage(message);
                chatMessages.append(Util.getFormattedTime() + " - " + 
                        messages.getUsername() + "(you): " + message + "\n");
                editText.setText("");
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Useless to us, from KeyListener interface
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Useless to us, from KeyListener interface
    }

    @Override
    public void focusGained(FocusEvent e) {
        editText.setText("");
    }

    @Override
    public void focusLost(FocusEvent e) {
        editText.setText("Type something...");
    }

}
