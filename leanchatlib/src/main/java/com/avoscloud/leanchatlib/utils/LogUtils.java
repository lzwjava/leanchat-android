package com.avoscloud.leanchatlib.utils;

import android.util.Log;
import com.avoscloud.leanchatlib.controller.ChatManager;

/**
 * Created by lzw on 15/7/14.
 */
public class LogUtils {
  public static final String LOGTAG = "leanchatlib";

  private static String getDebugInfo() {
    Throwable stack = new Throwable().fillInStackTrace();
    StackTraceElement[] trace = stack.getStackTrace();
    int n = 2;
    return trace[n].getClassName() + " " + trace[n].getMethodName() + "()" + ":" + trace[n].getLineNumber() + " ";
  }

  private static String getLogInfoByArray(String[] infos) {
    StringBuilder sb = new StringBuilder();
    for (String info : infos) {
      sb.append(info);
      sb.append(" ");
    }
    return sb.toString();
  }

  public static void i(String... s) {
    if (ChatManager.isDebugEnabled()) {
      Log.i(LOGTAG, getDebugInfo() + getLogInfoByArray(s));
    }
  }

  public static void e(String... s) {
    if (ChatManager.isDebugEnabled()) {
      Log.e(LOGTAG, getDebugInfo() + getLogInfoByArray(s));
    }
  }

  public static void d(String... s) {
    if (ChatManager.isDebugEnabled()) {
      Log.d(LOGTAG, getDebugInfo() + getLogInfoByArray(s));
    }
  }

  public static void v(String... s) {
    if (ChatManager.isDebugEnabled()) {
      Log.v(LOGTAG, getDebugInfo() + getLogInfoByArray(s));
    }
  }

  public static void w(String... s) {
    if (ChatManager.isDebugEnabled()) {
      Log.w(LOGTAG, getDebugInfo() + getLogInfoByArray(s));
    }
  }

  public static void logThrowable(Throwable tr) {
    if (ChatManager.isDebugEnabled()) {
      Log.v(LOGTAG, getDebugInfo(), tr);
    }
  }
}
