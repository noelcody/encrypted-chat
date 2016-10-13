package com.noelcody.encryptedchat.encryption;

import com.google.common.base.Throwables;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Encrypter {

  private static final String RSA_ALGORITHM = "RSA";
  private static final int KEY_SIZE = 512;

  private final KeyPairGenerator keyGenerator;

  public Encrypter() {
    try {
      keyGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      throw Throwables.propagate(e);
    }

    keyGenerator.initialize(KEY_SIZE);
  }

  public KeyPair generateKeys() {
    return keyGenerator.generateKeyPair();
  }

  public byte[] encrypt(String message, PublicKey key) {
    try {
      Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, key);
      return cipher.doFinal(message.getBytes());
    } catch (IllegalBlockSizeException
        | BadPaddingException
        | NoSuchPaddingException
        | NoSuchAlgorithmException
        | InvalidKeyException e) {
      throw Throwables.propagate(e);
    }
  }

  public String decrypt(byte[] encryptedMessage, PrivateKey key) {
    try {
      Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, key);
      return new String(cipher.doFinal(encryptedMessage));
    } catch (IllegalBlockSizeException
        | BadPaddingException
        | NoSuchPaddingException
        | NoSuchAlgorithmException
        | InvalidKeyException e) {
      throw Throwables.propagate(e);
    }
  }
}
