package com.noelcody.encryptedchat.server;

import com.google.common.base.Throwables;

import com.noelcody.encryptedchat.model.ClientData;
import com.noelcody.encryptedchat.model.ClientDataBuilder;
import com.noelcody.encryptedchat.model.ConnectRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;

public class ClientHandler {

  private final ClientDirectory clientDirectory;
  private final Socket clientSocket;

  private final BufferedReader clientReader;
  private final PrintWriter clientWriter;

  public ClientHandler(ClientDirectory clientDirectory, Socket clientSocket) throws IOException {
    this.clientDirectory = clientDirectory;
    this.clientSocket = clientSocket;

    clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);
  }

  public void start() throws IOException {
    ClientData clientData = handleLogin();
    ClientData buddyData = handleConnectRequest(clientData);
    handleChat(buddyData);
  }

  private ClientData handleLogin() throws IOException {
    String screenname = clientReader.readLine();
    ClientData clientData = new ClientDataBuilder()
        .socket(clientSocket)
        .screenname(screenname)
        .printWriter(clientWriter)
        .build();
    clientDirectory.addClient(clientData);

    return clientData;
  }

  /**
   * Receive request to connect to other user (buddy). Create connection and forward request to buddy.
   */
  private ClientData handleConnectRequest(ClientData clientData) throws IOException {
    String connectRequestString = clientReader.readLine();
    String buddyScreenname = ConnectRequest.getToScreenname(connectRequestString);

    waitForLogin(buddyScreenname);

    ClientData buddyData = clientDirectory.getClient(buddyScreenname);
    buddyData.printWriter().println(connectRequestString);

    return buddyData;
  }

  /**
   * Pass messages from client to buddy.
   */
  private void handleChat(ClientData buddyData) throws IOException {
    while (true) {
      String chatMessage = clientReader.readLine();
      System.out.println(String.format("[server] passing message to %s: %s", buddyData.screenname(), chatMessage));
      buddyData.printWriter().println(chatMessage);
    }
  }

  private void waitForLogin(String buddyScreenname) {
    while (!clientDirectory.containsClient(buddyScreenname)) {
      try {
        Thread.sleep(Duration.ofSeconds(1).toMillis());
      } catch (InterruptedException e) {
        throw Throwables.propagate(e);
      }
    }
  }
}
