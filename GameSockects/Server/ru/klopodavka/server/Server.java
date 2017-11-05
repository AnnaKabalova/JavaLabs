package ru.klopodavka.server;

import ru.klopodavka.network.TCPConnection;
import ru.klopodavka.network.TCPConnectionListener;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Random;

public class Server implements TCPConnectionListener {

    private final ArrayList<TCPConnection> connections = new ArrayList<>();
    private final Random rand = new Random();
    private final ArrayList<String> saveMessage = new ArrayList<>();

    public static void main(String[] args) {
        new Server();
    }

    private Server() {
        System.out.println("Server running...");
        try (ServerSocket serverSocket = new ServerSocket(1793)) {
            while(true) {
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e) {
                    System.out.println("TCPConnection exception: " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        int countMessage = saveMessage.size();
        for (int i = 0; i < countMessage; i++) {
            tcpConnection.sendString(saveMessage.get(i));
        }
        sendToAllConnections("Client connected: " + tcpConnection);
    }

    @Override
    public synchronized void onRecieveString(TCPConnection tcpConnection, String value) {
        saveMessage.add(value);
        sendToAllConnections(value);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        if (connections.size() == 0) saveMessage.clear();
        sendToAllConnections("Client disconnected: " + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    private void sendToAllConnections(String value) {
        System.out.println(value);
        final int count = connections.size();
        for (int i = 0; i < count; i++) {
            connections.get(i).sendString(value);
        }
    }
}
