package com.noelcody.encryptedchat.server;

import com.google.common.base.Throwables;

import com.noelcody.encryptedchat.encryption.KeySerializer;
import com.noelcody.encryptedchat.model.ClientData;
import com.noelcody.encryptedchat.model.ClientDataBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PublicKey;

public class ClientHandler {

  private final ClientDirectory clientDirectory;
  private final KeySerializer keySerializer;
  private final Socket clientSocket;

  private final BufferedReader clientReader;
  private final PrintWriter clientWriter;

  public ClientHandler(ClientDirectory clientDirectory, KeySerializer keySerializer, Socket clientSocket) {
    this.clientDirectory = clientDirectory;
    this.keySerializer = keySerializer;
    this.clientSocket = clientSocket;

    try {
      clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  public void start() throws IOException {
    ClientData clientData = handleLoginRequest();
    ClientData buddyData = handleConnectRequest(clientData);
    handleChat(buddyData);
  }

  private ClientData handleLoginRequest() throws IOException {
    String loginMessage = clientReader.readLine();

    String screenname = loginMessage.split(":", 2)[0]; // TODO wrap this up
    String publicKeyString = loginMessage.split(":", 2)[1];
    PublicKey publicKey = keySerializer.keyFromString(publicKeyString);

    ClientData clientData = new ClientDataBuilder()
        .socket(clientSocket)
        .screenname(screenname)
        .publicKey(publicKey)
        .printWriter(clientWriter)
        .build();
    clientDirectory.addClient(clientData);

    return clientData;
  }

  /**
   * Receive request to connect to other user (buddy) via screenname. Send buddy's public key back to client.
   */
  private ClientData handleConnectRequest(ClientData clientData) throws IOException {
    String buddyScreenname = clientReader.readLine();
    waitUntilBuddyLogsIn(buddyScreenname);

    clientDirectory.addChatConnection(clientData.screenname(), buddyScreenname);

    ClientData buddyData = clientDirectory.getClient(buddyScreenname);
    String buddyPublicKey = keySerializer.keyToString(buddyData.publicKey());
    clientWriter.println(buddyPublicKey);

    return buddyData;
  }

  /**
   * Pass encrypted messages from client to buddy indefinitely.
   */
  private void handleChat(ClientData buddyData) throws IOException {
    while (true) {
      String chatMessage = clientReader.readLine();
      buddyData.printWriter().println(chatMessage);
    }
  }

  private void waitUntilBuddyLogsIn(String buddyScreenname) {
    while (!clientDirectory.containsClient(buddyScreenname)) {
      // wait
    }
  }
}
