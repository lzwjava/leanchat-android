package com.avoscloud.chat.service;

import android.content.Context;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVPush;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.PushService;
import com.avoscloud.chat.ui.entry.EntrySplashActivity;

/**
 * Created by lzw on 15/6/11.
 */
public class PushManager {
  private static final String INSTALLATION_USER_ID = "userId";
  private static PushManager pushManager;
  private Context context;

  public synchronized static PushManager getInstance() {
    if (pushManager == null) {
      pushManager = new PushManager();
    }
    return pushManager;
  }

  public void init(Context context) {
    this.context = context;
    PushService.setDefaultPushCallback(context, EntrySplashActivity.class);
    updateInstallation();
  }

  private void updateInstallation() {
    AVInstallation installation = AVInstallation.getCurrentInstallation();
    AVUser user = AVUser.getCurrentUser();
    if (user != null) {
      String userId = user.getObjectId();
      if (userId.equals(installation.getString(INSTALLATION_USER_ID)) == false) {
        installation.put(INSTALLATION_USER_ID, user.getObjectId());
        installation.saveInBackground();
      }
    }
  }

  public void pushMessage(String userId, String message) {
    AVQuery query = AVInstallation.getQuery();
    query.whereEqualTo(INSTALLATION_USER_ID, userId);
    AVPush push = new AVPush();
    push.setQuery(query);
    push.setMessage(message);
    push.sendInBackground();
  }
}
