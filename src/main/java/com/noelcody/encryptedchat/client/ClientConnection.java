package com.noelcody.encryptedchat.client;

import com.google.common.base.Throwables;

import com.noelcody.encryptedchat.encryption.Encrypter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.crypto.SecretKey;

public class ClientConnection {

  private final String screenname;
  private final String buddyScreenname;
  private final SecretKey symmetricKey;
  private final PrintWriter serverWriter;
  private final BufferedReader serverReader;
  private final Encrypter encrypter;

  public ClientConnection(String screenname,
                          String buddyScreenname,
                          SecretKey symmetricKey,
                          PrintWriter serverWriter,
                          BufferedReader serverReader,
                          Encrypter encrypter) {
    this.screenname = screenname;
    this.buddyScreenname = buddyScreenname;
    this.symmetricKey = symmetricKey;
    this.serverWriter = serverWriter;
    this.serverReader = serverReader;
    this.encrypter = encrypter;

    readMessages();
  }

  public void readMessages() {
    new Thread(() -> {
      while (true) {
        String encryptedMessage;
        try {
          encryptedMessage = serverReader.readLine();
        } catch (IOException e) {
          throw Throwables.propagate(e);
        }

        String decryptedMessage = new String(encrypter.decode(encryptedMessage, symmetricKey));
        System.out.println(
            String.format("[%s's console] %s: %s", screenname, buddyScreenname, decryptedMessage));
      }
    }).start();
  }

  public void sendChat(String message) {
    String encryptedMessage = encrypter.encrypt(message, symmetricKey);
    serverWriter.println(encryptedMessage);
  }
}
