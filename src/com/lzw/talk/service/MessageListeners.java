package com.lzw.talk.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lzw on 14-10-9.
 */
public class MessageListeners {
  Map<String, MessageListener> messageListeners = new HashMap<String, MessageListener>();

  public void register(String id, MessageListener listener) {
    messageListeners.put(id, listener);
  }

  public void unregister(String id) {
    messageListeners.put(id, null);
  }

  public MessageListener get(String id) {
    return messageListeners.get(id);
  }
}
