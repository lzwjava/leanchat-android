package com.avoscloud.chat.base;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import com.avos.avoscloud.*;
import com.avoscloud.chat.avobject.*;
import com.avoscloud.chat.ui.activity.SplashActivity;
import com.avoscloud.chat.util.Logger;
import com.avoscloud.chat.util.PhotoUtil;
import com.baidu.mapapi.SDKInitializer;
import com.avoscloud.chat.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

/**
 * Created by lzw on 14-5-29.
 */
public class App extends Application {
  public static final String DB_NAME = "chat.db3";
  public static final int DB_VER = 4;
  public static boolean debug = true;
  public static App ctx;

  @Override
  public void onCreate() {
    super.onCreate();
    ctx = this;
    Utils.fixAsyncTaskBug();
    AVOSCloud.initialize(this, "x3o016bxnkpyee7e9pa5pre6efx2dadyerdlcez0wbzhw25g",
        "057x24cfdzhffnl3dzk14jh9xo2rq6w1hy1fdzt5tv46ym78");
    AVObject.registerSubclass(User.class);
    AVObject.registerSubclass(AddRequest.class);
    AVObject.registerSubclass(ChatGroup.class);
    AVObject.registerSubclass(UpdateInfo.class);

    AVInstallation.getCurrentInstallation().saveInBackground();
    PushService.setDefaultPushCallback(ctx, SplashActivity.class);
    AVOSCloud.setDebugLogEnabled(debug);
    if (App.debug) {
      Logger.level = Logger.VERBOSE;
    } else {
      Logger.level = Logger.NONE;
    }
    initImageLoader(ctx);
    initBaidu();
    openStrictMode();
  }

  public void openStrictMode() {
    if (App.debug) {
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
          .detectDiskReads()
          .detectDiskWrites()
          .detectNetwork()   // or .detectAll() for all detectable problems
          .penaltyLog()
          .build());
      StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
          .detectLeakedSqlLiteObjects()
          .detectLeakedClosableObjects()
          .penaltyLog()
              //.penaltyDeath()
          .build());
    }
  }

  private void initBaidu() {
    SDKInitializer.initialize(this);
  }

  public static App getInstance() {
    return ctx;
  }

  /**
   * 初始化ImageLoader
   */
  public static void initImageLoader(Context context) {
    File cacheDir = StorageUtils.getOwnCacheDirectory(context,
        "leanchat/Cache");
    ImageLoaderConfiguration config = PhotoUtil.getImageLoaderConfig(context, cacheDir);
    // Initialize ImageLoader with configuration.
    ImageLoader.getInstance().init(config);
  }

}
