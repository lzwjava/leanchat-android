package com.avoscloud.leanchatlib.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import com.avoscloud.leanchatlib.R;
import com.avoscloud.leanchatlib.controller.ChatManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

  private static String getDebugInfo() {
    Throwable stack = new Throwable().fillInStackTrace();
    StackTraceElement[] trace = stack.getStackTrace();
    int n = 2;
    return trace[n].getClassName() + " " + trace[n].getMethodName() + "()" + ":" + trace[n].getLineNumber() + " ";
  }

  public static void log(String... s) {
    if (ChatManager.isDebugEnabled()) {
      String info = "";
      if (s.length > 0) {
        info = s[0];
      }
      Log.i(ChatManager.LOGTAG, getDebugInfo() + info);
    }
  }

  public static void logThrowable(Throwable tr) {
    if (ChatManager.isDebugEnabled()) {
      Log.i(ChatManager.LOGTAG, getDebugInfo(), tr);
    }
  }

  public static void downloadFileIfNotExists(String url, File toFile) throws IOException {
    if (!toFile.exists()) {
      toFile.createNewFile();
      FileOutputStream outputStream = new FileOutputStream(toFile);
      DefaultHttpClient client = new DefaultHttpClient();
      HttpGet get = new HttpGet(url);
      HttpResponse response = client.execute(get);
      HttpEntity entity = response.getEntity();
      InputStream stream = entity.getContent();
      InputStream inputStream = stream;
      byte[] buffer = new byte[1024];
      int len;
      while ((len = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, len);
      }
      outputStream.close();
      inputStream.close();
    }
  }
}
