package com.avoscloud.chat.im.controller;

import android.app.Notification;
import com.avoscloud.chat.im.model.ChatUser;

import java.util.List;

/**
 * Created by lzw on 15/4/26.
 */
public interface ChatUserFactory {
  public ChatUser getChatUserById(String userId);

  public void cacheUserByIdsInBackground(List<String> userIds) throws Exception;

  public boolean showNotificationWhenNewMessageCome(String selfId);

  public void configureNotification(Notification notification);
}
