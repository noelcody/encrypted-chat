package com.noelcody.encryptedchat.model;

import java.io.PrintWriter;
import java.net.Socket;

import io.norberg.automatter.AutoMatter;

@AutoMatter
public interface ClientData {

  Socket socket();

  String screenname();

  PrintWriter printWriter();

}
