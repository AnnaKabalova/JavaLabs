package ru.klopodavka.network;

public interface TCPConnectionListener {
    void onConnectionReady(TCPConnection tcpConnection); // Start connection - ready
    void onRecieveString(TCPConnection tcpConnection, String value); // Connection recieve string
    void onDisconnect(TCPConnection tcpConnection); // Connection disconnect
    void onException(TCPConnection tcpConnection, Exception e); // Connection get exception
}
