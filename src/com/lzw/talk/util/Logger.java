package com.lzw.talk.util;

import android.util.Log;

/**
 * Created by lzw on 14-4-29.
 */
public class Logger {
  public static boolean open=true;

  public static void d(String s) {
    if(open){
      Log.d("lzw", s + "");
    }
  }

  public static void d(String format, Object... args) {
    if(open){
      d(String.format(format, args));
    }
  }
}
