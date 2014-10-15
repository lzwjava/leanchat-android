package com.avoscloud.chat.service;

import java.util.List;

public interface StatusListener {
  public void onStatusOnline(List<String> peerIds);
}
