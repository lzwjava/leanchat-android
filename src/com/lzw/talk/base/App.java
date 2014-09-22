package com.lzw.talk.base;

import android.app.Application;
import android.content.Context;
import com.avos.avoscloud.*;
import com.lzw.talk.avobject.User;
import com.lzw.talk.service.ChatService;
import com.lzw.talk.util.AVOSUtils;
import com.lzw.talk.util.Logger;
import com.lzw.talk.util.PhotoUtil;
import com.lzw.talk.util.Utils;
import com.lzw.talk.view.activity.LoginActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lzw on 14-5-29.
 */
public class App extends Application {
  public static final String DB_NAME = "chat.db3";
  public static final int DB_VER = 1;
  public static boolean debug = true;
  public static Context ctx;
  public static User chatUser;
  public static Session session;
  private static Map<String, User> usersCache = new HashMap<String, User>();

  @Override
  public void onCreate() {
    super.onCreate();
    ctx = this;
    Utils.fixAsyncTaskBug();
    AVOSCloud.initialize(this, "x3o016bxnkpyee7e9pa5pre6efx2dadyerdlcez0wbzhw25g",
        "057x24cfdzhffnl3dzk14jh9xo2rq6w1hy1fdzt5tv46ym78");
    User.registerSubclass(User.class);
    AVInstallation.getCurrentInstallation().saveInBackground();
    PushService.setDefaultPushCallback(ctx, LoginActivity.class);
    AVOSUtils.showInternalDebugLog();
    if (App.debug) {
      Logger.open = true;
    } else {
      Logger.open = false;
    }
    initImageLoader(ctx);
  }


  /**
   * 初始化ImageLoader
   */
  public static void initImageLoader(Context context) {
    File cacheDir = StorageUtils.getOwnCacheDirectory(context,
        "leanchat/Cache");
    ImageLoaderConfiguration config = PhotoUtil.getImageLoaderConfig(context, cacheDir);
    // Initialize ImageLoader with configuration.
    ImageLoader.getInstance().init(config);// 全局初始化此配置
  }

  public static User lookupUser(String userId) {
    return usersCache.get(userId);
  }

  public static void registerUserCache(String userId, User user) {
    usersCache.put(userId, user);
  }

  public static void registerUserCache(User user) {
    registerUserCache(user.getObjectId(), user);
  }

  public static void registerBatchUserCache(List<User> users) {
    for (User user : users) {
      registerUserCache(user);
    }
  }

  @Override
  public void onTerminate() {
    Session session= ChatService.getSession();
    session.close();
    super.onTerminate();
  }
}
