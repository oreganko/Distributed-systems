package com;

import java.net.*;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    Map<String, TcpServerThread> clientsThreadsMap = new LinkedHashMap<>();
    Map<String, Integer> clientsUdpPortMap = new LinkedHashMap<>();
    Lock tcpMapLock = new ReentrantLock();
    Lock udpMapLock = new ReentrantLock();
    UdpServerThread udpServerThread;

    private void startServer(int portNumber) {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            this.udpServerThread = new UdpServerThread(this, portNumber, InetAddress.getByName("localhost"));
            udpServerThread.start();
            ExecutorService executor = Executors.newFixedThreadPool(5);
            System.out.println("SERVER READY");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(new TcpServerThread(clientSocket, this));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {

        int portNumber = 9876;
        Server server = new Server();
        server.startServer(portNumber);
    }

    public boolean registerNewClient(String nick, TcpServerThread thread) {
        tcpMapLock.lock();
        if(clientsThreadsMap.containsKey(nick)) {
            thread.sendMessageWithTcp("There already is a client with this nick. Try again");
            tcpMapLock.unlock();
            return false;
        }
        clientsThreadsMap.put(nick, thread);
        udpMapLock.lock();
        clientsUdpPortMap.put(nick, thread.getClientPort());
        udpMapLock.unlock();
        System.out.println("Client " + nick + " registered.");
        for(String client: clientsThreadsMap.keySet()) {
            if(!client.equals(nick)) {
                clientsThreadsMap.get(client).sendMessageWithTcp(nick + " joined chat! Welcome " + nick + "!");
            }
        }

        tcpMapLock.unlock();
        return true;
    }

    public void sendMessageWithTCP(String message, String nick) {
        tcpMapLock.lock();
        for(String client: clientsThreadsMap.keySet()) {
            if(!client.equals(nick)) {
                clientsThreadsMap.get(client).sendMessageWithTcp(message);
            }
        }
        tcpMapLock.unlock();
    }

    public void sendMessageWithUdp(String message, String nick) {
        udpMapLock.lock();
        for(String client: clientsUdpPortMap.keySet()) {
            if(!client.equals(nick)) {
                udpServerThread.sendMessageWithUdp(message, clientsUdpPortMap.get(client));
            }
        }
        udpMapLock.unlock();
    }


    public void unregisterClient(String nick) {
        tcpMapLock.lock();
        udpMapLock.lock();
        clientsThreadsMap.remove(nick);
        clientsUdpPortMap.remove(nick);
        udpMapLock.unlock();
        System.out.println(nick + " unregistered");
        for(TcpServerThread thread: clientsThreadsMap.values()) {
                thread.sendMessageWithTcp(nick + " disconnected. You cannot contact them anymore");
            }
        tcpMapLock.unlock();
    }
}
