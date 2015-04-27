package com.avoscloud.leanchatlib.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lzw on 15/4/26.
 */
public class DownloadUtils {
  public static InputStream inputStreamFromUrl(String url) throws IOException {
    DefaultHttpClient client = new DefaultHttpClient();
    HttpGet get = new HttpGet(url);
    HttpResponse response = client.execute(get);
    HttpEntity entity = response.getEntity();
    InputStream stream = entity.getContent();
    return stream;
  }

  public static void downloadFileIfNotExists(String url, File toFile) throws IOException {
    if (!toFile.exists()) {
      downloadFile(url, toFile);
    }
  }

  public static void downloadFile(String url, File toFile) throws IOException {
    toFile.createNewFile();
    FileOutputStream outputStream = new FileOutputStream(toFile);
    InputStream inputStream = inputStreamFromUrl(url);
    byte[] buffer = new byte[1024];
    int len;
    while ((len = inputStream.read(buffer)) != -1) {
      outputStream.write(buffer, 0, len);
    }
    outputStream.close();
    inputStream.close();
  }
}
