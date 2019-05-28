package com.muc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class MessagePane extends JPanel {

    private final String login;
    private final ChatClient client;

    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(listModel);
    private JTextField inputField = new JTextField();

    public MessagePane(ChatClient client, String login) {
        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromLogin, String msg) {
                if (fromLogin.equalsIgnoreCase(login)) {
                    String line = fromLogin + ": " + msg;
                    listModel.addElement(line);
                }
            }
        });
        this.login = login;
        this.client = client;

        setLayout(new BorderLayout());
        add(new JScrollPane(messageList), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((KeyEvent.VK_ENTER) == e.getKeyCode()) {
                    try {
                        String text = inputField.getText();
                        client.msg(login, text);
                        inputField.setText("");
                        listModel.addElement(": " + text);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }
}
