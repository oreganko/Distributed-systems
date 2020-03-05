package com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UdpServerThread extends Thread {

    private DatagramSocket socket;
    private Server server;
    private byte[] receiveBuffer;
    private int port;
    private InetAddress address;

    public UdpServerThread(Server server, int port, InetAddress address) {
        try {
            this.port = port;
            this.address = address;
            receiveBuffer = new byte[3072];
            this.server = server;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) socket.close();
        }
    }


    public void run() {
        try {
            this.socket = new DatagramSocket(port);
            while (true) {

                DatagramPacket receivePacket =
                        new DatagramPacket(receiveBuffer, receiveBuffer.length);
                try {
                    socket.receive(receivePacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String msg = new String(receivePacket.getData());
                String nick = msg.split(":")[0];
                server.sendMessageWithUdp(msg, nick);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } finally {
            if(socket!= null && !socket.isClosed()) socket.close();
        }
    }

    public void sendMessageWithUdp(String message, int port) {

        byte[] sendBuffer = message.getBytes();
            try {
                DatagramPacket sendPacket =
                        new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName("localhost"), port);
                socket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

    }
}
