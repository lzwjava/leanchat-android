package com.avoscloud.chat.util;

import android.util.Log;

public class Logger {
  public static final int VERBOSE = 0;
  public static final int INFO = 1;
  public static final int DEBUG = 2;
  public static final int WARN = 3;
  public static final int ERROR = 4;
  public static final int NONE = 5;
  public static int level = VERBOSE;
  public static String tag = "lzw";

  public static void v(String s) {
    if (VERBOSE >= level) {
      Log.v(tag, s);
    }
  }

  public static void i(String s) {
    if (INFO >= level) {
      Log.i(tag, s);
    }
  }

  public static void w(String s) {
    if (WARN >= level) {
      Log.w(tag, s);
    }
  }

  public static void e(String s) {
    if (ERROR >= level) {
      Log.e(tag, s);
    }
  }

  public static void d(String s) {
    if (DEBUG >= level) {
      Log.d(tag, s);
    }
  }
}
