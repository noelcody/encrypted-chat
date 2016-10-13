package com.noelcody.encryptedchat.encryption;

import com.google.common.base.Throwables;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Encrypter {

  public String encrypt(String message, Key key) {
    return encrypt(message.getBytes(), key);
  }

  /**
   * Encrypt byte array and base64-encode so it can be safely sent as a String.
   */
  public String encrypt(byte[] message, Key key) {
    try {
      Cipher cipher = Cipher.getInstance(key.getAlgorithm());
      cipher.init(Cipher.ENCRYPT_MODE, key);
      byte[] encryptedMessage = cipher.doFinal(message);
      return Base64.getEncoder().encodeToString(encryptedMessage);
    } catch (IllegalBlockSizeException
        | BadPaddingException
        | NoSuchPaddingException
        | NoSuchAlgorithmException
        | InvalidKeyException e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Base64-decode and decrypt encoded String.
   */
  public byte[] decode(String encodedEncryptedMessage, Key key) {
    byte[] encryptedMessage = Base64.getDecoder().decode(encodedEncryptedMessage);
    try {
      Cipher cipher = Cipher.getInstance(key.getAlgorithm());
      cipher.init(Cipher.DECRYPT_MODE, key);
      return cipher.doFinal(encryptedMessage);
    } catch (IllegalBlockSizeException
        | BadPaddingException
        | NoSuchPaddingException
        | NoSuchAlgorithmException
        | InvalidKeyException e) {
      throw Throwables.propagate(e);
    }
  }
}
