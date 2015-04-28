package com.avoscloud.leanchatlib.controller;

import android.app.Notification;
import com.avoscloud.leanchatlib.model.UserInfo;

import java.util.List;

/**
 * Created by lzw on 15/4/26.
 */
public interface UserInfoFactory {
  public UserInfo getUserInfoById(String userId);

  public void cacheUserInfoByIdsInBackground(List<String> userIds) throws Exception;

  public boolean showNotificationWhenNewMessageCome(String selfId);

  public void configureNotification(Notification notification);
}
