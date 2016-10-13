package com.noelcody.encryptedchat.model;

import com.google.common.base.Joiner;

import com.noelcody.encryptedchat.encryption.AsymmetricKeyMgr;

import java.security.PublicKey;

import io.norberg.automatter.AutoMatter;

@AutoMatter
public interface ConnectRequest {

  String DELIMITER = ":";

  int FROM_SCREENNAME_POSITION = 0;
  int TO_SCREENNAME_POSITION = 1;
  int PUBLIC_KEY_POSITION = 2;

  String fromScreenname();

  String toScreenname();

  PublicKey publicKey();

  default String serialize(AsymmetricKeyMgr asymmetricKeyMgr) {
    return Joiner
        .on(DELIMITER)
        .join(fromScreenname(), toScreenname(), asymmetricKeyMgr.publicKeyToString(publicKey()));
  }

  static ConnectRequest deserialize(String serializedRequest, AsymmetricKeyMgr asymmetricKeyMgr) {
    String[] requestParts = serializedRequest.split(DELIMITER);

    return new ConnectRequestBuilder()
        .fromScreenname(requestParts[FROM_SCREENNAME_POSITION])
        .toScreenname(requestParts[TO_SCREENNAME_POSITION])
        .publicKey(asymmetricKeyMgr.publicKeyFromString(requestParts[PUBLIC_KEY_POSITION]))
        .build();
  }

  static String getToScreenname(String serializedRequest) {
    return serializedRequest.split(DELIMITER)[TO_SCREENNAME_POSITION];
  }
}
