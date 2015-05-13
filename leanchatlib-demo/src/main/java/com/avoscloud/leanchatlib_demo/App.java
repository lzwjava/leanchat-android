package com.avoscloud.leanchatlib_demo;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avoscloud.leanchatlib.controller.ChatManager;
import com.avoscloud.leanchatlib.controller.ChatManagerAdapter;
import com.avoscloud.leanchatlib.model.UserInfo;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.List;

/**
 * Created by lzw on 15/4/27.
 */
public class App extends Application {
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

  @Override
  public void onCreate() {
    super.onCreate();
    AVOSCloud.initialize(this, "xcalhck83o10dntwh8ft3z5kvv0xc25p6t3jqbe5zlkkdsib",
        "m9fzwse7od89gvcnk1dmdq4huprjvghjtiug1u2zu073zn99");
    ChatManager.setDebugEnabled(true);// tag leanchatlib
    AVOSCloud.setDebugLogEnabled(true);  // set false when release
    final ChatManager chatManager = ChatManager.getInstance();
    chatManager.init(this);
    chatManager.setChatManagerAdapter(new ChatManagerAdapter() {
      @Override
      public UserInfo getUserInfoById(String userId) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(userId);
        userInfo.setAvatarUrl("http://ac-x3o016bx.clouddn.com/86O7RAPx2BtTW5zgZTPGNwH9RZD5vNDtPm1YbIcu");
        return userInfo;
      }

      @Override
      public void cacheUserInfoByIdsInBackground(List<String> userIds) throws Exception {
      }

      //关于这个方法请见 leanchat 应用中的 ChatManagerAdapterImpl.java
      @Override
      public void shouldShowNotification(Context context, String selfId, AVIMConversation conversation, AVIMTypedMessage message) {
        Toast.makeText(context, "收到了一条消息但并未打开相应的对话。可以触发系统通知。", Toast.LENGTH_LONG).show();
      }
    });
    initImageLoader(this);
  }
}
