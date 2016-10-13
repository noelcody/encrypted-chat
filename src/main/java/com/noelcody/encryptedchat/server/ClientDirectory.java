package com.noelcody.encryptedchat.server;

import com.google.common.collect.Maps;

import com.noelcody.encryptedchat.model.ClientData;

import java.util.Map;

public class ClientDirectory {

  private final Map<String, ClientData> clients = Maps.newHashMap();

  public void addClient(ClientData clientData) {
    clients.put(clientData.screenname(), clientData);
  }

  public ClientData getClient(String screenname) {
    return clients.get(screenname);
  }

  public boolean containsClient(String screenname) {
    return clients.containsKey(screenname);
  }

}
