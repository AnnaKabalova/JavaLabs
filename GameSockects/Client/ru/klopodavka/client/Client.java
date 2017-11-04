package ru.klopodavka.client;

import ru.klopodavka.network.TCPConnection;
import ru.klopodavka.network.TCPConnectionListener;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Client extends JFrame implements ActionListener, TCPConnectionListener {

    private static final String IP_ADDRES = "localhost";
    private static final int PORT = 1793;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int GAME_FIELD_WIDTH = 15;
    private static final int GAME_FIELD_HEIGHT = 10;

//    private final JTextArea log = new JTextArea();
    private final JPanel gridPanel = new JPanel();
    private final JButton[][] gameButton;
    private final JTextField fieldNickName = new JTextField("Login");
    private final JTextField fieldInput = new JTextField();
    private TCPConnection connection;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client();
            }
        });
    }

    private Client() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        gridPanel.setLayout(new GridLayout(GAME_FIELD_HEIGHT, GAME_FIELD_WIDTH, 3, 3));
        gameButton = new JButton[GAME_FIELD_WIDTH][GAME_FIELD_HEIGHT];
        for(int y=0; y<GAME_FIELD_HEIGHT; y++){
            for(int x=0; x<GAME_FIELD_WIDTH; x++){
                gameButton[x][y]=new JButton();
                gridPanel.add(gameButton[x][y]);
            }
        }

        add(gridPanel, BorderLayout.CENTER);

//        log.setEditable(false);
//        log.setLineWrap(true);
//        add(log, BorderLayout.CENTER);

        fieldInput.addActionListener(this);
        add(fieldNickName, BorderLayout.NORTH);
        add(fieldInput, BorderLayout.SOUTH);

        setVisible(true);
        try {
            connection = new TCPConnection(this, IP_ADDRES, PORT);
        } catch (IOException e) {
            printMessage("Connection exception: " + e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String message = fieldInput.getText();
        if (message.equals("")) return;
        fieldInput.setText(null);
        connection.sendString(fieldNickName.getText() + ": " + message);
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMessage("Connection ready...");
    }

    @Override
    public void onRecieveString(TCPConnection tcpConnection, String value) {
        printMessage(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMessage("Connection close");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMessage("Connection exception: " + e);
    }

    private synchronized void printMessage(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
//                log.append(message + "\n");
//                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }
}
