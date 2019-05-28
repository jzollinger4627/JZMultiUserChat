package com.muc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginPane extends JPanel {

    private final ChatClient client;
    private UserListPane userListPane;

    private JTextField loginField = new JTextField();
    private JButton loginButton = new JButton("Login");

    public LoginPane(ChatClient client, UserListPane userListPane) {
        this.client = client;
        this.userListPane = userListPane;

        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        loginField.setPreferredSize(new Dimension(100, 25));
        add(loginField, g);
        g = new GridBagConstraints();
        g.gridy = 1;
        add(loginButton, g);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
    }

    private void doLogin() {
        userListPane.handleLoginExit(loginField.getText());
    }
}
