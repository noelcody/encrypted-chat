package com.noelcody.encryptedchat.client;

import com.google.common.base.Throwables;
import com.google.common.net.HostAndPort;

import com.noelcody.encryptedchat.encryption.AsymmetricKeyMgr;
import com.noelcody.encryptedchat.encryption.Encrypter;
import com.noelcody.encryptedchat.encryption.SymmetricKeyMgr;
import com.noelcody.encryptedchat.model.ConnectRequest;
import com.noelcody.encryptedchat.model.ConnectRequestBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

public class Client {

  private final String screenname;
  private final Encrypter encrypter;

  private final AsymmetricKeyMgr asymmetricKeyMgr;
  private final SymmetricKeyMgr symmetricKeyMgr;

  private final PrivateKey privateKey;
  private final PublicKey publicKey;

  private final BufferedReader serverReader;
  private final PrintWriter serverWriter;

  public Client(String screenname,
                Encrypter encrypter,
                AsymmetricKeyMgr asymmetricKeyMgr,
                SymmetricKeyMgr symmetricKeyMgr,
                HostAndPort serverHostPort) throws IOException {
    this.screenname = screenname;
    this.encrypter = encrypter;
    this.asymmetricKeyMgr = asymmetricKeyMgr;
    this.symmetricKeyMgr = symmetricKeyMgr;

    KeyPair keyPair = asymmetricKeyMgr.generateKeyPair();
    this.privateKey = keyPair.getPrivate();
    this.publicKey = keyPair.getPublic();

    Socket serverSocket = new Socket(serverHostPort.getHostText(), serverHostPort.getPort());
    serverWriter = new PrintWriter(serverSocket.getOutputStream(), true);
    serverReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
  }

  public void logIn() {
    writeToServer(screenname);
  }

  /**
   * Create connection initiated by chat buddy. Receive symmetric key encrypted with own public key.
   */
  public ClientConnection acceptConnection() throws IOException {
    ConnectRequest connectRequest = acceptConnectRequest();
    String buddyScreenname = connectRequest.fromScreenname();
    System.out.println(String.format("%s accepted connection from %s", screenname, buddyScreenname));

    sendConnectRequestTo(buddyScreenname);

    String encodedEncryptedSymmetricKey = serverReader.readLine();
    SecretKey symmetricKey = symmetricKeyMgr.decryptKey(encodedEncryptedSymmetricKey, encrypter, privateKey);

    System.out.println(String.format("%s received symmetric key from %s and is starting chat",
                                     screenname,
                                     buddyScreenname));
    return new ClientConnection(screenname, buddyScreenname, symmetricKey, serverWriter, serverReader, encrypter);
  }

  /**
   * Create connection initiated by self, accepting symmetric key from buddy. Send symmetric key encrypted with buddy's
   * public key.
   */
  public ClientConnection initiateConnection(String buddyScreenname) throws IOException {
    System.out.println(String.format("%s initiating connection to %s", screenname, buddyScreenname));
    sendConnectRequestTo(buddyScreenname);
    ConnectRequest connectRequest = acceptConnectRequest();
    System.out.println(String.format("%s accepted connection from %s", screenname, buddyScreenname));

    System.out.println(String.format("%s received asymmetric key from %s, is generating and sending symmetric key",
                                     screenname,
                                     buddyScreenname));
    SecretKey symmetricKey = symmetricKeyMgr.generateKey();
    String encryptedEncodedKey = symmetricKeyMgr.encryptKey(symmetricKey, encrypter, connectRequest.publicKey());
    writeToServer(encryptedEncodedKey);

    System.out.println(String.format("%s sent symmetric key to %s and is starting chat", screenname, buddyScreenname));
    return new ClientConnection(screenname,
                                connectRequest.fromScreenname(),
                                symmetricKey,
                                serverWriter,
                                serverReader,
                                encrypter);
  }

  private ConnectRequest acceptConnectRequest() {
    String buddyConnectRequestString;
    try {
      buddyConnectRequestString = serverReader.readLine();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
    return ConnectRequest.deserialize(buddyConnectRequestString, asymmetricKeyMgr);
  }

  private void sendConnectRequestTo(String buddyScreenname) {
    System.out.println(String.format("%s sending connect request to %s", screenname, buddyScreenname));
    String connectRequest = new ConnectRequestBuilder()
        .fromScreenname(screenname)
        .toScreenname(buddyScreenname)
        .publicKey(publicKey)
        .build()
        .serialize(asymmetricKeyMgr);
    serverWriter.println(connectRequest);
  }

  private void writeToServer(String message) {
    serverWriter.println(message);
  }
}
