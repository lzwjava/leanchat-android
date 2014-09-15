package com.lzw.talk.util;

import java.lang.reflect.Method;

/**
 * Created by lzw on 14-9-15.
 */
public class AVOSUtils {
  public static void showInternalDebugLog() {
    try {
      Class<?> clz = Class.forName("com.avos.avoscloud.AVOSCloud");
      Method startMethod = clz.getDeclaredMethod("showInternalDebugLog", Boolean.TYPE);
      startMethod.setAccessible(true);
      startMethod.invoke(null, true);
    } catch (Exception e) {
      e.printStackTrace();
      Logger.d("failed to show internal logs");
    }
  }
}
