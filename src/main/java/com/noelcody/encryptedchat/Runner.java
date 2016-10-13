package com.noelcody.encryptedchat;

import com.google.common.net.HostAndPort;

import com.noelcody.encryptedchat.client.Client;
import com.noelcody.encryptedchat.encryption.Encrypter;
import com.noelcody.encryptedchat.encryption.KeySerializer;
import com.noelcody.encryptedchat.server.ClientDirectory;
import com.noelcody.encryptedchat.server.Server;

import java.io.IOException;
import java.net.InetAddress;

public class Runner {

  private static final int PORT = 9090;
  private static final String SCREENNAME_ALICE = "alice";
  private static final String SCREENNAME_BOB = "bob";

  public static void main(String[] args) throws IOException {
    KeySerializer keySerializer = new KeySerializer();
    Encrypter encrypter = new Encrypter();
    ClientDirectory clientDirectory = new ClientDirectory();

    startServer(keySerializer, clientDirectory);

    HostAndPort serverHostPort = HostAndPort.fromParts(InetAddress.getByName(null).getHostAddress(), PORT);
    Client aliceClient = new Client(serverHostPort, encrypter, keySerializer, SCREENNAME_ALICE);
    Client bobClient = new Client(serverHostPort, encrypter, keySerializer, SCREENNAME_BOB);

    aliceClient.logIn();
    bobClient.logIn();

    aliceClient.connectToBuddy(SCREENNAME_BOB);
    bobClient.connectToBuddy(SCREENNAME_ALICE);

    aliceClient.sendChat("hello");
    bobClient.sendChat("world");
  }

  private static void startServer(KeySerializer keySerializer, ClientDirectory clientDirectory) {
    new Thread(() -> new Server(keySerializer, clientDirectory).start(PORT)).start();
  }
}
