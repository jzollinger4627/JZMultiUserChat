package com.muc;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class UserListPain extends JPanel implements UserStatusListener {

    private final ChatClient client;
    private JList<String> userListUI;
    private DefaultListModel<String> userListModel;

    public UserListPain(ChatClient client) {
        this.client = client;
        this.client.addUserStatusLister(this);

        userListModel = new DefaultListModel<>();
        userListUI = new JList<>(userListModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(userListUI), BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost", 8818);

        UserListPain userListPain = new UserListPain(client);
        JFrame frame = new JFrame("User List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(400, 600));
        frame.setLocationRelativeTo(null);

        frame.getContentPane().add(userListPain, BorderLayout.CENTER);
        frame.setVisible(true);

        if (client.connect()) {
            try {
                client.login("guest", "guest");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void online(String login) {
        userListModel.addElement(login);
    }

    @Override
    public void offline(String login) {
        userListModel.removeElement(login);
    }
}
