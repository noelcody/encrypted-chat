package com.noelcody.encryptedchat.client;

import com.google.common.base.Throwables;
import com.google.common.net.HostAndPort;

import com.noelcody.encryptedchat.encryption.Encrypter;
import com.noelcody.encryptedchat.encryption.KeySerializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class Client {

  private final Encrypter encrypter;
  private final KeySerializer keySerializer;
  private final String screenname;

  private final PrivateKey privateKey;
  private final PublicKey publicKey;

  private final BufferedReader serverReader;
  private final PrintWriter serverWriter;

  private PublicKey buddyPublicKey;

  public Client(HostAndPort serverHostPort, Encrypter encrypter, KeySerializer keySerializer, String screenname)
      throws IOException {
    this.encrypter = encrypter;
    this.keySerializer = keySerializer;
    this.screenname = screenname;

    KeyPair keyPair = encrypter.generateKeys();
    this.privateKey = keyPair.getPrivate();
    this.publicKey = keyPair.getPublic();

    Socket serverSocket = new Socket(serverHostPort.getHostText(), serverHostPort.getPort());
    serverWriter = new PrintWriter(serverSocket.getOutputStream(), true);
    serverReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
  }

  public void logIn() {
    String loginMessage = screenname + ":" + keySerializer.keyToString(publicKey);
    writeToServer(loginMessage);
  }

  public void connectToBuddy(String buddyScreenname) throws IOException {
    writeToServer(buddyScreenname);
    String serializedBuddyPublicKey = serverReader.readLine();
    buddyPublicKey = keySerializer.keyFromString(serializedBuddyPublicKey);

    readBuddyMessages(buddyScreenname);
  }

  private void readBuddyMessages(String buddyScreenname) {
    new Thread(() -> {
      while (true) {
        String chatMessage;
        try {
          chatMessage = serverReader.readLine();
        } catch (IOException e) {
          throw Throwables.propagate(e);
        }

        byte[] decodedEncryptedMessage = Base64.getDecoder().decode(chatMessage);
        String decodedDecryptedMessage = encrypter.decrypt(decodedEncryptedMessage, privateKey);
        System.out.println(buddyScreenname + ": " + decodedDecryptedMessage);
      }
    }).start();
  }

  public void sendChat(String message) {
    byte[] encryptedMessage = encrypter.encrypt(message, buddyPublicKey);
    String encodedEncryptedMessage = Base64.getEncoder().encodeToString(encryptedMessage);
    writeToServer(encodedEncryptedMessage);
  }

  private void writeToServer(String message) {
    serverWriter.println(message);
  }
}
