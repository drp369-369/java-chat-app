package com.deep.chatapp;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import org.json.JSONObject;

public class ChatClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 5000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner sc = new Scanner(System.in)) {

            System.out.print("Enter your name: ");
            String name = sc.nextLine();
            out.println(name);

            Thread listener = new Thread(() -> {
                try {
                    String serverMsg;
                    while ((serverMsg = in.readLine()) != null) {
                        JSONObject obj = new JSONObject(serverMsg);
                        String type = obj.getString("type");
                        String msg = obj.optString("message", "");
                        if (type.equals("message")) {
                            System.out.println(obj.getString("user") + ": " + msg);
                        } else if (type.equals("private")) {
                            System.out.println("[Private] " + obj.getString("from") + ": " + msg);
                        } else if (type.equals("join") || type.equals("leave")) {
                            System.out.println(msg);
                        } else if (type.equals("error")) {
                            System.out.println("⚠️  " + msg);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Disconnected.");
                }
            });

            listener.start();

            System.out.println("Type messages (use /pm <user> <msg> for private, /exit to quit)");
            while (true) {
                String input = sc.nextLine();
                if (input.equalsIgnoreCase("/exit")) {
                    out.println(new JSONObject().put("type", "exit").toString());
                    break;
                } else if (input.startsWith("/pm ")) {
                    String[] parts = input.split(" ", 3);
                    if (parts.length >= 3) {
                        out.println(new JSONObject().put("type", "message").put("to", parts[1]).put("message", parts[2]).toString());
                    }
                } else {
                    out.println(new JSONObject().put("type", "message").put("message", input).toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
