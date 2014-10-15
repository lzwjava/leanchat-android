package com.avoscloud.chat.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    }
  }

  public static String convid(String myId, String otherId) {
    List<String> ids = new ArrayList<String>();
    ids.add(myId);
    ids.add(otherId);
    return convid(ids);
  }

  public static String convid(List<String> peerIds) {
    Collections.sort(peerIds);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < peerIds.size(); i++) {
      if (i != 0) {
        sb.append(":");
      }
      sb.append(peerIds.get(i));
    }
    return Utils.md5(sb.toString());
  }

}
