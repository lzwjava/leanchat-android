package com.lzw.talk.service;

import java.util.List;

public interface StatusListner {
  public void onStatusOnline(List<String> peerIds);
}
