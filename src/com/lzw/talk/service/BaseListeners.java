package com.lzw.talk.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lzw on 14-10-12.
 */
public class BaseListeners<T> {
  Map<String, T> innerListeners = new HashMap<String, T>();

  public void register(String id, T listener) {
    innerListeners.put(id, listener);
  }

  public void unregister(String id) {
    innerListeners.put(id, null);
  }

  public T get(String id) {
    return innerListeners.get(id);
  }
}
