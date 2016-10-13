package com.noelcody.encryptedchat.encryption;

import com.google.common.base.Throwables;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SymmetricKeyMgr {

  private static final String SYMMETRIC_ALGORITHM = "AES";
  private static final int SYMMETRIC_KEY_SIZE = 128;

  public SecretKey generateKey() {
    KeyGenerator symmetricKeyGenerator;
    try {
      symmetricKeyGenerator = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      throw Throwables.propagate(e);
    }
    symmetricKeyGenerator.init(SYMMETRIC_KEY_SIZE);
    return symmetricKeyGenerator.generateKey();
  }

  public String encryptKey(SecretKey symmetricKey, Encrypter encrypter, Key encryptWithKey) {
    byte[] keyBytes = symmetricKey.getEncoded();
    return encrypter.encrypt(keyBytes, encryptWithKey);
  }

  public SecretKey decryptKey(String encryptedKey, Encrypter encrypter, Key decryptWithKey) {
    byte[] decryptedKey = encrypter.decode(encryptedKey, decryptWithKey);
    return new SecretKeySpec(decryptedKey, 0, decryptedKey.length, SYMMETRIC_ALGORITHM);
  }
}
