package com.avoscloud.chat.base;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avoscloud.chat.entity.avobject.AddRequest;
import com.avoscloud.chat.entity.avobject.UpdateInfo;
import com.avoscloud.chat.service.ChatManagerAdapterImpl;
import com.avoscloud.chat.service.ConversationManager;
import com.avoscloud.chat.service.PushManager;
import com.avoscloud.chat.util.Logger;
import com.avoscloud.chat.util.Utils;
import com.avoscloud.leanchatlib.controller.ChatManager;
import com.baidu.mapapi.SDKInitializer;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * Created by lzw on 14-5-29.
 */
public class App extends Application {
  public static boolean debug = true;
  public static App ctx;

  public static App getInstance() {
    return ctx;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    ctx = this;
    Utils.fixAsyncTaskBug();

    String publicId = "g7gz9oazvrubrauf5xjmzp3dl12edorywm0hy8fvlt6mjb1y";
    String publicKey = "01p70e67aet6dvkcaag9ajn5mff39s1d5jmpyakzhd851fhx";

    String testAppId = "xcalhck83o10dntwh8ft3z5kvv0xc25p6t3jqbe5zlkkdsib";
    String testAppKey = "m9fzwse7od89gvcnk1dmdq4huprjvghjtiug1u2zu073zn99";

    String appId = "x3o016bxnkpyee7e9pa5pre6efx2dadyerdlcez0wbzhw25g";
    String appKey = "057x24cfdzhffnl3dzk14jh9xo2rq6w1hy1fdzt5tv46ym78";

    AVOSCloud.initialize(this, appId, appKey);
    //AVOSCloud.initialize(this, publicId,publicKey);
    //AVOSCloud.initialize(this, testAppId, testAppKey);

    AVObject.registerSubclass(AddRequest.class);
    AVObject.registerSubclass(UpdateInfo.class);
    // 节省流量
    AVOSCloud.setLastModifyEnabled(true);

    PushManager.getInstance().init(ctx);
    AVOSCloud.setDebugLogEnabled(debug);
    AVAnalytics.enableCrashReport(this, !debug);
    initImageLoader(ctx);
    initBaiduMap();
    if (App.debug) {
      openStrictMode();
    }

    initChatManager();

    if (App.debug) {
      Logger.level = Logger.VERBOSE;
    } else {
      Logger.level = Logger.NONE;
    }
  }

  private void initChatManager() {
    final ChatManager chatManager = ChatManager.getInstance();
    chatManager.init(this);
    if (AVUser.getCurrentUser() != null) {
      chatManager.setupDatabaseWithSelfId(AVUser.getCurrentUser().getObjectId());
    }
    chatManager.setConversationEventHandler(ConversationManager.getEventHandler());
    ChatManagerAdapterImpl chatManagerAdapter = new ChatManagerAdapterImpl(App.ctx);
    chatManager.setChatManagerAdapter(chatManagerAdapter);
    ChatManager.setDebugEnabled(App.debug);
  }

  public void openStrictMode() {
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

  /**
   * 初始化ImageLoader
   */
  public static void initImageLoader(Context context) {
    ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
        context)
        .threadPoolSize(3).threadPriority(Thread.NORM_PRIORITY - 2)
            //.memoryCache(new WeakMemoryCache())
        .denyCacheImageMultipleSizesInMemory()
        .tasksProcessingOrder(QueueProcessingType.LIFO)
        .build();
    ImageLoader.getInstance().init(config);
  }

  private void initBaiduMap() {
    SDKInitializer.initialize(this);
  }
}
