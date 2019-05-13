package com.muc;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {

    private String IPAddress;
    private int port;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedIn;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<UserStatusListener>();

    public ChatClient(String IPAddress, int port) {
        this.IPAddress = IPAddress;
        this.port = port;
    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost", 8818);
        client.addUserStatusLister(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("ONLINE: " + login);
            }

            @Override
            public void offline(String login) {
                System.out.println("OFFLINE: " + login);
            }
        });
        if (!client.connect()) {
            System.err.println("Connect failed.");
        }else {
            System.out.println("Connect successfull");
            if (client.login("guest", "guest")) {
                System.out.println("Login successful");
            }else {
                System.err.println("Login failed");
            }
        }

    }

    private boolean login(String username, String password) throws IOException {
        String cmd = "login " + username + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String msg = bufferedIn.readLine();
        System.out.println("Response Line: " + msg);

        if ("ok login".equalsIgnoreCase(msg)) {
            startMessageReader();
            return true;
        }else {
            return false;
        }

    }

    private void startMessageReader() {
        Thread t = new Thread() {
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }

    private void readMessageLoop() {
        try {
            String line;
            while ((line = bufferedIn.readLine()) != null) {
                String[] tokens = line.split(" ");
                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if ("online".equalsIgnoreCase(cmd)) {
                        handleOnline(tokens);
                    } else if ("offline".equalsIgnoreCase(cmd)) {
                        handleOffline(tokens);
                    }
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            }catch (IOException ex) {
                e.printStackTrace();
            }
        }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener: userStatusListeners) {
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener: userStatusListeners) {
            listener.online(login);
        }
    }

    private boolean connect() {
        try {
            this.socket = new Socket(IPAddress, port);
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addUserStatusLister(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    public void removeUserStatusLister(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }
}
