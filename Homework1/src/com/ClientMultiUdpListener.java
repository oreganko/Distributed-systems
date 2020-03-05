package com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ClientMultiUdpListener extends Thread {
    protected MulticastSocket socket = null;
    protected byte[] buf = new byte[1024];

    private InetAddress group;

    public void run() {
        try {
            group = InetAddress.getByName("230.0.0.0");
            socket = new MulticastSocket(9875);
            socket.joinGroup(group);
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(
                        packet.getData(), 0, packet.getLength());
                System.out.println(received);
            }
        }catch(IOException e) {
            e.printStackTrace();
            }
        finally {
            try {
                socket.leaveGroup(group);
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket.close();
        }

    }
}
