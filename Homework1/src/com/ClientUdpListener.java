package com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ClientUdpListener extends Thread{

    DatagramSocket socket;


    public ClientUdpListener(DatagramSocket socket) {
        this.socket = socket;
    }

    public void run() {
        while (!socket.isClosed()) {
            byte[] receiveBuffer = new byte[3072];
            DatagramPacket receivePacket =
                    new DatagramPacket(receiveBuffer, receiveBuffer.length);
            try {
                socket.receive(receivePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String msg = new String(receivePacket.getData());
            System.out.println(msg);
        }
    }
}
