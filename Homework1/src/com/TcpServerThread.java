package com;

import java.net.*;
import java.io.*;

    public class TcpServerThread extends Thread {
        private Socket socket;
        private Server server;
        private Boolean registered = false;
        private String clientNick;
        private PrintWriter clientOut;

        public TcpServerThread(Socket socket, Server server) {
            super("Tcp Socket Thread");
            this.socket = socket;
            this.server = server;
        }

        public void run() {

            try (
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()));
            ) {
                String inputLine, outputLine;
                clientOut = out;

                while ((inputLine = in.readLine()) != null) {
                    if(!registered) {
                        if(server.registerNewClient(inputLine.trim(), this)) {
                            clientNick = inputLine.trim();
                            registered = true;
                            out.println("nick ok");
                        }
                        continue;
                    }
                    outputLine = inputLine;
                    server.sendMessageWithTCP(clientNick + ": " + outputLine, clientNick);
                }
                socket.close();
            } catch (SocketException e) {
                System.out.println(clientNick + " disconnected.");
                server.unregisterClient(clientNick);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessageWithTcp(String message) {
            clientOut.println(message);
        }

        public int getClientPort() {return socket.getPort();}
}
