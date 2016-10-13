package com.noelcody.encryptedchat.server;

import com.google.common.base.Throwables;

import com.noelcody.encryptedchat.encryption.KeySerializer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

  private final KeySerializer keySerializer;
  private final ClientDirectory clientDirectory;

  public Server(KeySerializer keySerializer, ClientDirectory clientDirectory) {
    this.keySerializer = keySerializer;
    this.clientDirectory = clientDirectory;
  }

  public void start(int port) {
    try {
      ServerSocket listener = new ServerSocket(port);
      acceptConnections(listener);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private void acceptConnections(ServerSocket listener) throws IOException {
    while (true) {
      Socket socket = listener.accept();
      new Thread(() -> {
        try {
          new ClientHandler(clientDirectory, keySerializer, socket).start();
        } catch (IOException e) {
          throw Throwables.propagate(e);
        }
      }).start();
    }
  }
}
