package com.noelcody.encryptedchat.server;

import com.google.common.base.Throwables;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

  private final ClientDirectory clientDirectory;

  public Server(ClientDirectory clientDirectory) {
    this.clientDirectory = clientDirectory;
  }

  public void start(int port) throws IOException {
    ServerSocket listener = new ServerSocket(port);
    acceptConnections(listener);
  }

  private void acceptConnections(ServerSocket listener) throws IOException {
    while (true) {
      Socket socket = listener.accept();
      new Thread(() -> {
        try {
          new ClientHandler(clientDirectory, socket).start();
        } catch (IOException e) {
          throw Throwables.propagate(e);
        }
      }).start();
    }
  }
}
