package com;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private DatagramSocket datagramSocket = null;
    private Socket tcpSocket = null;
    private int port;
    private int multiPort = 9875;
    private PrintWriter out;
    String nick;
    InetAddress group;

    private void start(int portNumber) {
        String userInput;
        try {
            tcpSocket = new Socket("localhost", portNumber);
            datagramSocket = new DatagramSocket(tcpSocket.getLocalPort());
            group = InetAddress.getByName("230.0.0.0");
            this.port = portNumber;

            //start TCP
            ClientTcpListener tcpListener = new ClientTcpListener(tcpSocket, this);
            out = new PrintWriter(tcpSocket.getOutputStream(), true);
            BufferedReader stdIn =
                    new BufferedReader(new InputStreamReader(System.in));
            this.nick = tcpListener.register(stdIn);
            tcpListener.start();

            //start UDP
            ClientUdpListener clientUdpListener = new ClientUdpListener(datagramSocket);
            clientUdpListener.start();

            //start MulticastUDP
            new ClientMultiUdpListener().start();


            while (true) {
                if(stdIn.ready()) {

                    //read user input
                    if ((userInput = stdIn.readLine()) != null) {

                        //send Unicast UDP
                        if(userInput.equals("U")) {
                            File file = new File("asciiart.txt");
                            BufferedReader br = new BufferedReader(new FileReader(file));
                            String art = "";
                            String tmpArt;
                            while ((tmpArt = br.readLine()) != null) {
                                if (art.length() + tmpArt.length() + nick.length() + 1 > 3072) {
                                    System.out.println("You cannot send message bigger than 3 072 B.");
                                }
                                art = art.concat(tmpArt).concat("\n");
                            }
                            if(art.length() > 0) {
                                sendWithUdp(nick + ":\n" + art);
                            }
                            continue;

                            //send Multicast UDP
                        } else if(userInput.equals("M")) {
                            File file = new File("asciiart2.txt");
                            BufferedReader br = new BufferedReader(new FileReader(file));
                            String art = "";
                            String tmpArt;
                            while ((tmpArt = br.readLine()) != null) {
                                if (art.length() + tmpArt.length() + nick.length() + 1 > 3072) {
                                    System.out.println("You cannot send message bigger than 3 072 B.");
                                }
                                art = art.concat(tmpArt).concat("\n");
                            }
                            if(art.length() > 0) {
                                sendWithMulticastUdp(nick + ":\n" + art);
                            }
                            continue;
                        }

                        //send with TCP
                        out.println(userInput);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (tcpSocket != null) {
                try {
                    tcpSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(datagramSocket != null) {
                datagramSocket.close();
            }
        }
    }

    private void sendWithUdp(String message) {
        byte[] sendBuffer = message.getBytes();

        try {
            DatagramPacket sendPacket =
                    new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName("localhost"), port);
            datagramSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendWithMulticastUdp(String message) {
        byte[] sendBuffer = message.getBytes();

        try {
            DatagramPacket sendPacket =
                    new DatagramPacket(sendBuffer, sendBuffer.length, group, multiPort);
            datagramSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendWithTcp(String message) {
        out.println(message);
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start(9876);
    }

}
