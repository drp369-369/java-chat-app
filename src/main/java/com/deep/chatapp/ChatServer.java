package com.deep.chatapp;

import java.io.*;
import java.net.*;
import java.util.*;
import org.json.JSONObject;

public class ChatServer {
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        int port = 5000;
        System.out.println("🟢 Chat Server started on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Waiting for clients to connect...");

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, clients);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private List<ClientHandler> clients;
    private PrintWriter out;
    private BufferedReader in;
    private String name;

    public ClientHandler(Socket socket, List<ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(new JSONObject().put("type", "system").put("message", "Enter your name:").toString());
            name = in.readLine();
            broadcast(new JSONObject().put("type", "join").put("user", name).put("message", name + " joined the chat").toString());

            String input;
            while ((input = in.readLine()) != null) {
                JSONObject obj = new JSONObject(input);
                String msgType = obj.optString("type");

                if (msgType.equals("message")) {
                    String toUser = obj.optString("to", "");
                    String message = obj.getString("message");
                    if (!toUser.isEmpty()) {
                        sendPrivate(toUser, message);
                    } else {
                        broadcast(new JSONObject().put("type", "message").put("user", name).put("message", message).toString());
                    }
                } else if (msgType.equals("exit")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            broadcast(new JSONObject().put("type", "leave").put("user", name).put("message", name + " left the chat").toString());
            cleanup();
        }
    }

    private void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler c : clients) {
                c.out.println(message);
            }
        }
    }

    private void sendPrivate(String toUser, String message) {
        synchronized (clients) {
            for (ClientHandler c : clients) {
                if (toUser.equalsIgnoreCase(c.name)) {
                    c.out.println(new JSONObject().put("type", "private").put("from", name).put("message", message).toString());
                    out.println(new JSONObject().put("type", "private_ack").put("to", toUser).put("message", message).toString());
                    return;
                }
            }
            out.println(new JSONObject().put("type", "error").put("message", "User not found: " + toUser).toString());
        }
    }

    private void cleanup() {
        try {
            clients.remove(this);
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
