package ru.klopodavka.client;

import com.sun.corba.se.spi.logging.CORBALogDomains;
import org.jetbrains.annotations.Contract;
import ru.klopodavka.network.TCPConnection;
import ru.klopodavka.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client extends JFrame implements ActionListener, TCPConnectionListener {

    private static final String IP_ADDRES = "localhost";
    private static final int PORT = 1793;
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;
    private static final int GAME_FIELD_WIDTH = 15;
    private static final int GAME_FIELD_HEIGHT = 10;

    private final JButton[][] gameButton;
    private final JTextField fieldInput = new JTextField();
    private TCPConnection connection;
    private final Color gameColor;
    private boolean flagStartGame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client();
            }
        });
    }

    private Client() {
        Random rand = new Random();
        Color initColor = Color.BLACK;
        while(initColor.equals(Color.BLACK) || initColor.equals(Color.WHITE)) {
            initColor = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
        }
        gameColor = initColor;
        flagStartGame = true;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(GAME_FIELD_HEIGHT, GAME_FIELD_WIDTH, 3, 3));
        gameButton = new JButton[GAME_FIELD_WIDTH][GAME_FIELD_HEIGHT];
        int k = 0;
        for(int y=0; y<GAME_FIELD_HEIGHT; y++){
            for(int x=0; x<GAME_FIELD_WIDTH; x++){
                gameButton[x][y] = new JButton(String.valueOf(k));
                gameButton[x][y].setBackground(Color.WHITE);
                gridPanel.add(gameButton[x][y]);
                k++;
            }
        }

        add(gridPanel, BorderLayout.CENTER);
        fieldInput.addActionListener(this);
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
        int num = Integer.parseInt(message);
        int[] coord = NumberToCoord(num);

        if (gameButton[coord[0]][coord[1]].getBackground().equals(Color.BLACK)) {
            JOptionPane.showMessageDialog(Client.this, "Вы не можете ходить " +
                    "по раздавленным клопам");
            return;
        }

        if (!flagStartGame) {
            if (!checkStep(coord[0], coord[1])) {
                JOptionPane.showMessageDialog(Client.this, "Вы не можете ходить вне " +
                        "своей линии подключения");
                return;
            }
        } else { flagStartGame = false; }

        String resultMessage;
        Color currentButton = gameButton[coord[0]][coord[1]].getBackground();
        if(!currentButton.equals(gameColor) && !currentButton.equals(Color.WHITE)) {
            gameButton[coord[0]][coord[1]].setBackground(Color.BLACK);
            resultMessage = "0:0:0 ";
        } else {
            gameButton[coord[0]][coord[1]].setBackground(gameColor);
            resultMessage = gameColor.getRed() + ":" + gameColor.getGreen() + ":" + gameColor.getBlue() + " ";
        }
        resultMessage += num;
        connection.sendString(resultMessage);
    }

    @Contract(pure = true)
    private boolean checkStep(int x, int y) {
        boolean flag = false;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if ((x+i > 0) && (x+i < GAME_FIELD_WIDTH) && (y+j > 0) && (y+j < GAME_FIELD_HEIGHT)) {
                    if(gameButton[x+i][y+j].getBackground().equals(gameColor)) {
                        flag = true;
                        break;
                    }
                }
            }
            if (flag) break;
        }
        return flag;
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMessage("Connection ready...");
    }

    @Override
    public void onRecieveString(TCPConnection tcpConnection, String value) {
        printGrid(value);
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
                System.out.println(message);
            }
        });
    }

    private synchronized void printGrid(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int[] res = getRegex(message);
                int[] coord = NumberToCoord(res[3]);
                Color tempColor = null;
                try {
                    tempColor = new Color(res[0], res[1], res[2]);
                    gameButton[coord[0]][coord[1]].setBackground(tempColor);
                } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("Not identified color");
                }

            }
        });
    }

    @Contract(pure = true)
    private int CoordToNumber(int x, int y) {
        return x + y * GAME_FIELD_WIDTH;
    }

    @Contract(pure = true)
    private int[] NumberToCoord(int num) {
        int coord[] = new int[2];
        int x = 0, y = 0, curr = 0;
        boolean flag = false;
        for(y=0; y<GAME_FIELD_HEIGHT; y++){
            for(x=0; x<GAME_FIELD_WIDTH; x++){
                if (curr == num) {
                    flag = true;
                    break;
                }
                curr++;
            }
            if (flag) break;
        }
        coord[0] = x; coord[1] = y;
        return coord;
    }

    private int[] getRegex(String message) {
        int[] result = new int[4];
        result[0] = result[1] = result[2] = result[3] = -1;
        String pattern = "([0-9]*):([0-9]*):([0-9]*) ([0-9]*)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(message);
        if (m.find( )) {
            for(int i = 0; i < 4; i++) {
                result[i] = Integer.parseInt(m.group(i+1));
            }
        }
        return result;
    }
}
