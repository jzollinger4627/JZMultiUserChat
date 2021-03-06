package com.muc;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<String>();

    public ServerWorker(Server server, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if ("quit".equalsIgnoreCase(cmd) || "exit".equalsIgnoreCase(cmd) || "logoff".equalsIgnoreCase(cmd)) {
                    handleLogoff();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(tokens);
                } else if ("msg".equalsIgnoreCase(cmd)) {
                    handleMessage(tokens);
                } else if ("join".equalsIgnoreCase(cmd)) {
                    handleJoin(tokens);
                } else if ("leave".equalsIgnoreCase(cmd)) {
                    handleLeave(tokens);
                } else {
                    String msg = "Unknown Command: " + cmd + "\n";
                    send(msg);
                }
            }
        }
        System.out.println("Disconnected: " + clientSocket.getInetAddress().toString().substring(1)+":"+clientSocket.getPort());
        handleLogoff();
        inputStream.close();outputStream.close();clientSocket.close();
    }

    private void handleLeave(String[] tokens) throws IOException {
        if (tokens.length == 2) {
            String topic = tokens[1];
            topicSet.remove(topic);
        }else {
            outputStream.write("Invalid use of join: (leave #topic)\n".getBytes());
        }
    }

    public boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic);
    }


    private void handleJoin(String[] tokens) throws IOException {
        if (tokens.length == 2) {
            String topic = tokens[1];
            topicSet.add(topic);
        }else {
            outputStream.write("Invalid use of join: (join #topic)\n".getBytes());
        }
    }

    //format: "msg" "user" msg....
    //format: "msg" "#topic" msg....
    private void handleMessage(String[] tokens) throws IOException {
        if (tokens.length >= 3) {
            String sendTo = tokens[1];
            String msg = "";
            for (int i=2;i<tokens.length;i++) {
                msg = msg + tokens[i] + " ";
            }
            msg = msg + "\n";

            boolean isTopic = sendTo.charAt(0) == '#';

            int counter = 0;
            for (ServerWorker worker: server.getWorkerList()) {
                if (isTopic) {
                    if (worker.isMemberOfTopic(sendTo) && worker != this) {
                        String outMsg = "msg<"+sendTo+","+login+">: " + msg;
                        worker.send(outMsg);
                        counter++;
                    }
                }else {
                    if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                        String outMsg = "msg<"+login+">: " + msg;
                        worker.send(outMsg);
                        counter++;
                    }
                }

            }
            if (isTopic) {
                if (counter == 0) {
                    outputStream.write(("No one is on topic "+sendTo+"\n").getBytes());
                }
            }else {
                if (counter == 0) {
                    outputStream.write("User is offline\n".getBytes());
                }
            }
        }else {
            outputStream.write("Invalid use of msg: (msg User text)\n".getBytes());
        }
    }

    private void handleLogoff() throws IOException {
        server.removeWorker(this);

        String onlineMsg = "Offline " + login + "\n";
        for (ServerWorker worker: server.getWorkerList()) {
            if (worker != this) {
                worker.send(onlineMsg);
            }
        }
    }

    public String getLogin() {
        return login;
    }

    private void handleLogin(String[] tokens) throws IOException{
        if (tokens.length == 2) {
            String login = tokens[1];
            int counter = 0;
            for (ServerWorker worker: server.getWorkerList()) {
                if (worker.getLogin() != null) {
                    if (worker.getLogin().equalsIgnoreCase(login)) {
                        counter++;
                    }
                }
            }
            if (counter == 0) {
                String msg = "ok login\n";
                send(msg);
                this.login = login;
                System.out.println("User logged in successfully: " + login);

                String onlineMsg = "online " + login + "\n";
                List<ServerWorker> workerList = server.getWorkerList();

                for (ServerWorker worker: workerList) { //letting you know who's online
                    if (worker.getLogin() != null && worker != this) {
                        String msg2 = "online " + worker.getLogin() + "\n";
                        send(msg2);
                    }

                }

                for (ServerWorker worker: workerList) { //letting other know you're online
                    if (worker != this) {
                        worker.send(onlineMsg);
                    }
                }
            }else {
                String msg = "Name Taken Login \n";
                send(msg);
                System.err.println("Login failed for " + login);
            }
        }else {
            String msg = "Invalid Use of Login: (login Username) \n";
            send(msg);
        }
    }

    private void send(String msg) throws IOException {
        outputStream.write(msg.getBytes());

    }

}
