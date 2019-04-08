package com.example.videostreaming;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class SocketHandler {
    private static DatagramSocket socket;
    private static InetAddress inetAddress;

    public static synchronized DatagramSocket getSocket(){
        return socket;
    }

    public static synchronized void setSocket(DatagramSocket socket){
        SocketHandler.socket = socket;
    }

    public static InetAddress getInetAddress() {
        return inetAddress;
    }

    public static void setInetAddress(InetAddress inetAddress) {
        SocketHandler.inetAddress = inetAddress;
    }
}
