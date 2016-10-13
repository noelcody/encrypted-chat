package com.noelcody.encryptedchat;

import com.google.common.base.Throwables;
import com.google.common.net.HostAndPort;

import com.noelcody.encryptedchat.client.Client;
import com.noelcody.encryptedchat.client.ClientConnection;
import com.noelcody.encryptedchat.encryption.AsymmetricKeyMgr;
import com.noelcody.encryptedchat.encryption.Encrypter;
import com.noelcody.encryptedchat.encryption.SymmetricKeyMgr;
import com.noelcody.encryptedchat.server.ClientDirectory;
import com.noelcody.encryptedchat.server.Server;

import java.io.IOException;
import java.net.InetAddress;

public class Runner {

  private static final int PORT = 9090;
  private static final String SCREENNAME_ALICE = "alice";
  private static final String SCREENNAME_BOB = "bob";

  public static void main(String[] args) throws IOException {
    ClientDirectory clientDirectory = new ClientDirectory();
    startServer(clientDirectory);

    AsymmetricKeyMgr asymmetricKeyMgr = new AsymmetricKeyMgr();
    SymmetricKeyMgr symmetricKeyMgr = new SymmetricKeyMgr();
    Encrypter encrypter = new Encrypter();

    HostAndPort serverHostPort = HostAndPort.fromParts(InetAddress.getByName(null).getHostAddress(), PORT);
    Client aliceClient = new Client(SCREENNAME_ALICE, encrypter, asymmetricKeyMgr, symmetricKeyMgr, serverHostPort);
    Client bobClient = new Client(SCREENNAME_BOB, encrypter, asymmetricKeyMgr, symmetricKeyMgr, serverHostPort);

    new Thread(() -> {
      aliceClient.logIn();
      try {
        ClientConnection connectionToBob = aliceClient.initiateConnection(SCREENNAME_BOB);
        connectionToBob.sendChat("hi");
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }).start();

    new Thread(() -> {
      bobClient.logIn();
      try {
        ClientConnection connectionToAlice = bobClient.acceptConnection();
        connectionToAlice.sendChat("hello");
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }).start();
  }

  private static void startServer(ClientDirectory clientDirectory) {
    new Thread(() -> {
      try {
        new Server(clientDirectory).start(PORT);
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }).start();
  }
}
