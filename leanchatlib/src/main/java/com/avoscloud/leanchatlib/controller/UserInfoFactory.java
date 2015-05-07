package com.avoscloud.leanchatlib.controller;

import android.app.Notification;
import com.avoscloud.leanchatlib.model.UserInfo;

import java.util.List;

/**
 * Created by lzw on 15/4/26.
 */
public interface UserInfoFactory {
  UserInfo getUserInfoById(String userId);

  void cacheUserInfoByIdsInBackground(List<String> userIds) throws Exception;

  boolean showNotificationWhenNewMessageCome(String selfId);

  void configureNotification(Notification notification);
}
