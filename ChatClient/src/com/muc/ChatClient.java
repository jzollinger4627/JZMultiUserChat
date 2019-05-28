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
    private ArrayList<MessageListener> messageListeners = new ArrayList<MessageListener>();

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
        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromLogin, String msg) {
                System.out.println("msg<"+ fromLogin + ">: " + msg);
            }
        });
        if (!client.connect()) {
            System.err.println("Connect failed.");
        }else {
            System.out.println("Connect successfull");
            if (client.login("guest")) {
                System.out.println("Login successful");

                client.msg("jacob", "Hello World!");
            }else {
                System.err.println("Login failed");
            }

            //client.logoff();
        }

    }

    public void msg(String sendTo, String msg) throws IOException {
        String cmd = "msg " + sendTo + " " + msg + "\n";
        serverOut.write(cmd.getBytes());
    }

    public void logoff() throws IOException {
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }

    public boolean login(String username) throws IOException {
        String cmd = "login " + username + "\n";
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
                    } else if (cmd.startsWith("msg")) {
                        handleMessage(tokens);
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

    private void handleMessage(String[] tokens) {
        String login = tokens[0].substring(tokens[0].indexOf("<")+1,tokens[0].indexOf(">"));
        String msg = "";
        System.out.println(tokens);
        for (int i=1;i<tokens.length;i++) {
            msg = msg + tokens[i] + " ";
        }
        for (MessageListener listener: messageListeners) {
            listener.onMessage(login, msg);
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

    public boolean connect() {
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

    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

}
