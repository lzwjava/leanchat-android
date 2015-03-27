package com.avoscloud.chat.util;

import android.util.Log;

public class Logger {
  public static final int VERBOSE = 0;
  public static int level = VERBOSE;
  public static final int INFO = 1;
  public static final int DEBUG = 2;
  public static final int WARN = 3;
  public static final int ERROR = 4;
  public static final int NONE = 5;
  public static String tag = "lzw";

  private static String getDebugInfo() {
    Throwable stack = new Throwable().fillInStackTrace();
    StackTraceElement[] trace = stack.getStackTrace();
    int n = 2;
    return trace[n].getClassName() + " " + trace[n].getMethodName() + "()" + ":" + trace[n].getLineNumber() + " ";
  }

  public static void v(String s) {
    if (VERBOSE >= level) {
      Log.v(tag, getDebugInfo() + s);
    }
  }

  public static void i(String s) {
    if (INFO >= level) {
      Log.i(tag, getDebugInfo() + s);
    }
  }

  public static void w(String s) {
    if (WARN >= level) {
      Log.w(tag, getDebugInfo() + s);
    }
  }

  public static void e(String s) {
    if (ERROR >= level) {
      Log.e(tag, getDebugInfo() + s);
    }
  }

  public static void d(String s) {
    if (DEBUG >= level) {
      Log.d(tag, getDebugInfo() + s);
    }
  }

}
