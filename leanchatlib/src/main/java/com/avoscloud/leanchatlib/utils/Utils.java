package com.avoscloud.leanchatlib.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import com.avoscloud.leanchatlib.R;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.Closeable;
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
    StringBuilder sb = new StringBuilder();
    int start = 48, end = 58;
    appendChar(sb, start, end);
    appendChar(sb, 65, 90);
    appendChar(sb, 97, 123);
    String charSet = sb.toString();
    StringBuilder sb1 = new StringBuilder();
    Random random = new Random();
    for (int i = 0; i < 24; i++) {
      int len = charSet.length();
      int pos = random.nextInt(len);
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

  private static DefaultHttpClient httpClient;

  private synchronized static DefaultHttpClient getDefaultHttpClient() {
    if (httpClient == null) {
      httpClient = new DefaultHttpClient();
    }
    return httpClient;
  }

  /**
   * 下载文件，若失败会将文件删除，以便下次重新下载
   * 暂时不校验 size，万一 size 跟实际文件的大小不一样，会导致每次重新下载
   *
   * @param url
   * @param toFile
   */
  public static void downloadFileIfNotExists(String url, File toFile) {
    if (!toFile.exists()) {
      FileOutputStream outputStream = null;
      InputStream inputStream = null;
      try {
        outputStream = new FileOutputStream(toFile);
        HttpGet get = new HttpGet(url);
        HttpResponse response = getDefaultHttpClient().execute(get);
        HttpEntity entity = response.getEntity();
        inputStream = entity.getContent();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, len);
        }
      } catch (IOException e) {
        if (toFile.exists()) {
          toFile.delete();
        }
      } finally {
        closeQuietly(inputStream);
        closeQuietly(outputStream);
      }
    }
  }

  public static void closeQuietly(Closeable closeable) {
    try {
      closeable.close();
    } catch (Exception e) {
    }
  }
}
