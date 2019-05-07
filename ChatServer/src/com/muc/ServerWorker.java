package com.muc;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;

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
                }else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(tokens);
                }else {
                    String msg = "Unknown Command: " + cmd + "\n";
                    send(msg);
                }
            }
        }
        System.out.println("Disconnected: " + clientSocket.getInetAddress().toString().substring(1)+":"+clientSocket.getLocalPort());
        inputStream.close();outputStream.close();clientSocket.close();
    }

    private void handleLogoff() throws IOException {
        ArrayList<ServerWorker> workerList = (ArrayList<ServerWorker>) server.getWorkerList();
        workerList.remove(this);
        String onlineMsg = "Offline " + login + "\n";
        server.setWorkerList(workerList);
        for (ServerWorker worker: workerList) {
            if (worker != this) {
                worker.send(onlineMsg);
            }
        }
    }

    public String getLogin() {
        return login;
    }

    private void handleLogin(String[] tokens) throws IOException{
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];
            if ((login.equals("guest") && password.equals("guest") || (login.equals("jacob") && password.equals("101806360")))) {
                String msg = "ok login \n";
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
                String msg = "Unknown Login \n";
                send(msg);
            }
        }else {
            String msg = "Invalid Use of Login: (login Username Password) \n";
            send(msg);
        }
    }

    private void send(String msg) throws IOException {
        outputStream.write(msg.getBytes());

    }

}
