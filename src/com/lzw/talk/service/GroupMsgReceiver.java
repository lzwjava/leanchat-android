package com.lzw.talk.service;

import android.content.Context;
import com.avos.avoscloud.AVGroupMessageReceiver;
import com.avos.avoscloud.AVMessage;
import com.avos.avoscloud.Group;
import com.avos.avoscloud.LogUtil;
import com.lzw.talk.util.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lzw on 14-10-8.
 */
public class GroupMsgReceiver extends AVGroupMessageReceiver {
  public static GroupListener groupListener;
  public static Map<String, MessageListener> messageListeners
      = new HashMap<String, MessageListener>();

  @Override
  public void onJoined(Context context, Group group) {
    if (groupListener != null) {
      groupListener.onJoined(group);
    }
  }

  @Override
  public void onInvited(Context context, Group group, String byPeerId) {
    Logger.d("you're invited to " + group.getGroupId() + " by " + byPeerId);
  }

  @Override
  public void onKicked(Context context, Group group, String byPeerId) {
    Logger.d("you're kicked from " + group.getGroupId() + " by " + byPeerId);
  }

  @Override
  public void onMessageSent(Context context, Group group, AVMessage message) {
    Logger.d(message.getMessage() + " sent");
  }

  @Override
  public void onMessageFailure(Context context, Group group, AVMessage message) {
    Logger.d(message.getMessage() + " failure");
  }

  @Override
  public void onMessage(Context context, Group group, AVMessage msg) {
    Logger.d(msg.getMessage() + " receiver");
  }

  @Override
  public void onQuit(Context context, Group group) {
    Logger.d(group.getGroupId() + " quit");
  }

  @Override
  public void onReject(Context context, Group group, String op, List<String> targetIds) {
    Logger.d(op + ":" + targetIds + " rejected");
  }

  @Override
  public void onMemberJoin(Context context, Group group, List<String> joinedPeerIds) {
    Logger.d(joinedPeerIds + " join " + group.getGroupId());
  }

  @Override
  public void onMemberLeft(Context context, Group group, List<String> leftPeerIds) {
    Logger.d(leftPeerIds + " left " + group.getGroupId());
  }

  @Override
  public void onError(Context context, Group group, Throwable e) {
    LogUtil.log.e("", (Exception) e);
  }

  public static void registerGroupListener(GroupListener listener) {
    groupListener = listener;
  }

  public static void unregisterGroupListener() {
    groupListener = null;
  }

  public static void registerMessageListener(String groupId, MessageListener listener) {
    messageListeners.put(groupId, listener);
  }

  public static void unregisterMessageListener(String groupId) {
    messageListeners.put(groupId, null);
  }
}
