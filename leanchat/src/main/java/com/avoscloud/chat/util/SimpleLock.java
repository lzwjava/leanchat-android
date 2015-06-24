package com.avoscloud.chat.util;

import java.util.concurrent.Semaphore;

/**
 * Created by lzw on 15/6/24.
 */
public class SimpleLock {
  private static volatile Semaphore mutex = new Semaphore(0);

  public static void reset() {
    mutex = new Semaphore(0);
  }

  public static void go() {
    mutex.release();
  }

  public static void lock() {
    try {
      mutex.acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
