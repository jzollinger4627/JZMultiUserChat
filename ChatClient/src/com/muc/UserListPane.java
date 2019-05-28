package com.muc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class UserListPane extends JPanel implements UserStatusListener {

    private static JFrame frame;
    private static UserListPane userListPane;

    private final ChatClient client;
    private JList<String> userListUI;
    private DefaultListModel<String> userListModel;

    public UserListPane(ChatClient client) {
        client.addUserStatusLister(this);

        userListModel = new DefaultListModel<>();
        userListUI = new JList<>(userListModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(userListUI), BorderLayout.CENTER);

        userListUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    String login = userListUI.getSelectedValue();
                    MessagePane messagePane = new MessagePane(client, login);

                    JFrame frame = new JFrame("Messager:<" + login + ">");
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frame.setSize(new Dimension(500,500));
                    frame.setLocationRelativeTo(null);
                    frame.getContentPane().add(messagePane, BorderLayout.CENTER);
                    frame.setVisible(true);
                }
            }
        });
        this.client = client;
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost", 8818);
        client.connect();

        userListPane = new UserListPane(client);
        frame = new JFrame("User List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(400, 600));
        frame.setLocationRelativeTo(null);

        frame.setLayout(new BorderLayout());
        frame.getContentPane().add(new LoginPane(client, userListPane), BorderLayout.CENTER);
        frame.setVisible(true);


    }

    public void handleLoginExit(String login) {
        try {
            if (client.login(login)) {
                frame.dispose();
                frame = new JFrame("User List");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(new Dimension(400, 600));
                frame.setLocationRelativeTo(null);

                frame.setLayout(new BorderLayout());
                frame.getContentPane().add(userListPane);
                frame.setVisible(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
