package com.muc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {

    private final int port;
    private ArrayList<ServerWorker> workerList = new ArrayList<ServerWorker>();

    public Server(int port) {
        this.port = port;
    }

    public List<ServerWorker> getWorkerList() {
        return workerList;
    }

    public void setWorkerList(ArrayList<ServerWorker> workerList) {
        this.workerList = workerList;
    }

    public void run() {

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while(true) {
                System.out.println("About to accept client connection");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accept connection from " + clientSocket);
                ServerWorker worker = new ServerWorker(this, clientSocket);
                workerList.add(worker);
                worker.start();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeWorker(ServerWorker serverWorker) {
        workerList.remove(serverWorker);
    }
}
