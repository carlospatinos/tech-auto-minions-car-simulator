package com.ple.tcpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
     private static final int PORT = 8000; // Choose any port not in use

    public static void main(String[] args) {
        System.out.println("Starting TCP Server on port " + PORT + "...");
        
        // Use try-with-resources to automatically close the ServerSocket
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening. Waiting for a client to connect...");

            // .accept() blocks execution until a client connects
            try (Socket clientSocket = serverSocket.accept()) {
                System.out.println("Client connected from: " + clientSocket.getRemoteSocketAddress());

                // Set up reader to receive messages from the client
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                // Set up writer to send messages to the client
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Read a line of text from the client
                String clientMessage = in.readLine();
                System.out.println("Received from client: " + clientMessage);

                // Send a reply back to the client
                out.println("Hello from the Java TCP Server! Message received successfully.");
            } 
            // The client socket closes automatically here due to try-with-resources
            System.out.println("Client connection closed.");
            
        } catch (IOException e) {
            System.err.println("Server exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

