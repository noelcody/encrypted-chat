package com.noelcody.encryptedchat.encryption;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class KeySerializer {

  private static final String DELIMITER = ":";

  public PublicKey keyFromString(String serializedKey) {
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

  public String keyToString(PublicKey publicKey) {
    RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
    return Joiner.on(DELIMITER).join(
        rsaPublicKey.getModulus().toString(),
        rsaPublicKey.getPublicExponent().toString());
  }

}
