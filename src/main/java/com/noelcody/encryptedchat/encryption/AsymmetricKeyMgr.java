package com.noelcody.encryptedchat.encryption;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class AsymmetricKeyMgr {

  private static final String ASYMMETRIC_ALGORITHM = "RSA";
  private static final int ASYMMETRIC_KEY_SIZE = 512;
  private static final String DELIMITER = "_";

  public KeyPair generateKeyPair() {
    KeyPairGenerator asymmetricKeyGenerator;
    try {
      asymmetricKeyGenerator = KeyPairGenerator.getInstance(ASYMMETRIC_ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      throw Throwables.propagate(e);
    }
    asymmetricKeyGenerator.initialize(ASYMMETRIC_KEY_SIZE);
    return asymmetricKeyGenerator.generateKeyPair();
  }

  public PublicKey publicKeyFromString(String serializedKey) {
    String[] Parts = serializedKey.split(DELIMITER, 2);
    RSAPublicKeySpec Spec = new RSAPublicKeySpec(
        new BigInteger(Parts[0]),
        new BigInteger(Parts[1]));
    try {
      return KeyFactory.getInstance("RSA").generatePublic(Spec);
    } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
      throw Throwables.propagate(e);
    }
  }

  public String publicKeyToString(PublicKey publicKey) {
    RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
    return Joiner.on(DELIMITER).join(
        rsaPublicKey.getModulus().toString(),
        rsaPublicKey.getPublicExponent().toString());
  }
}
