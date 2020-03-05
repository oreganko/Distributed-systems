package com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientTcpListener extends Thread {
    private Socket socket;
    private BufferedReader in;
    private Client client;

    public ClientTcpListener(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;
        try {
            in = new BufferedReader(new
                    InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String response;
            while (!socket.isClosed()) {
                if ((response = in.readLine()) != null)
                    System.out.println(response);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String register(BufferedReader stdIn) {
        String nick;
        String response;

        System.out.println("Enter your chat nick!");
        try {
            nick = stdIn.readLine();
            client.sendWithTcp(nick);
            while ((response = in.readLine()) != null) {
                if (response.equals("nick ok"))
                    break;
                else {
                    System.out.println(response);
                    nick = stdIn.readLine();
                    client.sendWithTcp(nick);
                }
            }
            System.out.println("WELCOME " + nick + "!!!");
            return nick;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
