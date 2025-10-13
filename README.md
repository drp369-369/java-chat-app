# 💬 Java Chat Application

A simple client-server chat application built with Java sockets and JSON messaging support with private message feature.

## Features
- Real-time messaging between multiple clients
- JSON-based communication
- Private messaging using `/pm <user> <message>`
- Graceful user join/exit messages
- Multi-threaded server for multiple clients

## Setup Instructions

1. Clone or unzip the project.
2. Navigate to folder and build with Maven:
   ```bash
   mvn compile
   ```
3. Run the server:
   ```bash
   mvn exec:java -Dexec.mainClass=com.deep.chatapp.ChatServer
   ```
4. Run multiple clients in new terminals:
   ```bash
   mvn exec:java -Dexec.mainClass=com.deep.chatapp.ChatClient
   ```
