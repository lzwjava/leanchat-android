package com.avoscloud.leanchatlib.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import com.avoscloud.leanchatlib.R;

import java.util.Random;

/**
 * Created by lzw on 15/4/27.
 */
public class Utils {
  public static ProgressDialog showSpinnerDialog(Activity activity) {
    ProgressDialog dialog = new ProgressDialog(activity);
    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    dialog.setCancelable(true);
    dialog.setMessage(activity.getString(R.string.chat_utils_hardLoading));
    if (!activity.isFinishing()) {
      dialog.show();
    }
    return dialog;
  }

  public static String uuid() {
    //return UUID.randomUUID().toString().substring(0, 24);
    return myUUID();
  }

  public static String myUUID() {
    StringBuilder sb = new StringBuilder();
    int start = 48, end = 58;
    appendChar(sb, start, end);
    appendChar(sb, 65, 90);
    appendChar(sb, 97, 123);
    String charSet = sb.toString();
    StringBuilder sb1 = new StringBuilder();
    for (int i = 0; i < 24; i++) {
      int len = charSet.length();
      int pos = new Random().nextInt(len);
      sb1.append(charSet.charAt(pos));
    }
    return sb1.toString();
  }

  public static void appendChar(StringBuilder sb, int start, int end) {
    int i;
    for (i = start; i < end; i++) {
      sb.append((char) i);
    }
  }
}
