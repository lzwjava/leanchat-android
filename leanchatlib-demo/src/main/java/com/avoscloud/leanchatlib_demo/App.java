package com.avoscloud.leanchatlib_demo;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import com.avos.avoscloud.AVOSCloud;
import com.avoscloud.leanchatlib.controller.ChatManager;
import com.avoscloud.leanchatlib.controller.ChatUserFactory;
import com.avoscloud.leanchatlib.model.ChatUser;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.List;

/**
 * Created by lzw on 15/4/27.
 */
public class App extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    AVOSCloud.initialize(this, "xcalhck83o10dntwh8ft3z5kvv0xc25p6t3jqbe5zlkkdsib",
        "m9fzwse7od89gvcnk1dmdq4huprjvghjtiug1u2zu073zn99");
    AVOSCloud.setDebugLogEnabled(true);
    final ChatManager chatManager = ChatManager.getInstance();
    chatManager.init(this);
    chatManager.setChatUserFactory(new ChatUserFactory() {
      @Override
      public ChatUser getChatUserById(String userId) {
        ChatUser chatUser = new ChatUser();
        chatUser.setUserId(userId);
        chatUser.setUsername(userId);
        chatUser.setAvatarUrl("http://ac-x3o016bx.clouddn.com/86O7RAPx2BtTW5zgZTPGNwH9RZD5vNDtPm1YbIcu");
        return chatUser;
      }

      @Override
      public void cacheUserByIdsInBackground(List<String> userIds) throws Exception {
      }

      @Override
      public boolean showNotificationWhenNewMessageCome(String selfId) {
        return true;
      }

      @Override
      public void configureNotification(Notification notification) {
        notification.defaults |= Notification.DEFAULT_ALL;
      }
    });
    initImageLoader(this);
  }

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
}
