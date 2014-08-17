package com.lzw.talk.base;

import android.app.Application;
import android.content.Context;
import com.avos.avoscloud.*;
import com.lzw.commons.Logger;
import com.lzw.commons.Utils;
import com.lzw.talk.activity.LoginActivity;

import java.lang.reflect.Method;

/**
 * Created by lzw on 14-5-29.
 */
public class App extends Application {
  public static final String DB_NAME = "chat.db3";
  public static final int DB_VER = 1;
  public static boolean debug = false;
  public static Context cxt;
  public static AVUser chatUser;
  public static Session session;

  @Override
  public void onCreate() {
    super.onCreate();
    cxt = this;
    Utils.fixAsyncTaskBug();
    AVOSCloud.initialize(this, "x3o016bxnkpyee7e9pa5pre6efx2dadyerdlcez0wbzhw25g",
        "057x24cfdzhffnl3dzk14jh9xo2rq6w1hy1fdzt5tv46ym78");
    AVAnalytics.start(this);
    AVAnalytics.enableCrashReport(this, !debug);
    AVInstallation.getCurrentInstallation().saveInBackground();
    PushService.setDefaultPushCallback(cxt, LoginActivity.class);
    if (App.debug) {
      showInternalDebugLog();
      Logger.open = true;
    }
  }

  public void showInternalDebugLog() {
    try {
      Class<?> clz = Class.forName("com.avos.avoscloud.AVOSCloud");
      Method startMethod = clz.getDeclaredMethod("showInternalDebugLog", Boolean.TYPE);
      startMethod.setAccessible(true);
      startMethod.invoke(null, true);
    } catch (Exception e) {
      e.printStackTrace();
      Logger.d("failed to show internal logs");
    }
  }
}
