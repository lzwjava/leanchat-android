package com.avoscloud.chat.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by lzw on 14/11/20.
 */
public class Connectivity {
  public static NetworkInfo getNetworkInfo(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    return cm.getActiveNetworkInfo();
  }

  public static boolean isConnected(Context context) {
    NetworkInfo info = getNetworkInfo(context);
    return isConnected(info);
  }

  public static boolean isConnected(NetworkInfo info) {
    return info != null && info.isConnected();
  }

  public static boolean isConnectedWifi(Context context) {
    NetworkInfo info = getNetworkInfo(context);
    return isConnected(info) && info.getType() == ConnectivityManager.TYPE_WIFI;
  }

  public static boolean isConnectedMobile(Context context) {
    NetworkInfo info = getNetworkInfo(context);
    return isConnected(info) && info.getType() == ConnectivityManager.TYPE_MOBILE;
  }
}
