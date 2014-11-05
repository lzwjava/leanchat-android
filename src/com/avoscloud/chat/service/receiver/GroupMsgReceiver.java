package com.avoscloud.chat.service.receiver;

import android.content.Context;
import com.avos.avoscloud.AVGroupMessageReceiver;
import com.avos.avoscloud.AVMessage;
import com.avos.avoscloud.Group;
import com.avos.avoscloud.LogUtil;
import com.avoscloud.chat.service.ChatService;
import com.avoscloud.chat.service.listener.GroupEventListener;
import com.avoscloud.chat.service.listener.MsgListener;
import com.avoscloud.chat.util.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lzw on 14-10-8.
 */
public class GroupMsgReceiver extends AVGroupMessageReceiver {
  public static Set<GroupEventListener> listeners = new HashSet<GroupEventListener>();
  public static MsgListener msgListener;

  @Override
  public void onJoined(Context context, Group group) {
    for (GroupEventListener listener : listeners) {
      listener.onJoined(group);
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
    Logger.d(message.getMessage() + " sent " + message.getTimestamp());
    ChatService.onMessageSent(message, msgListener, group);
  }

  @Override
  public void onMessageFailure(Context context, Group group, AVMessage message) {
    Logger.d(message.getMessage() + " failure " + group);
  }

  @Override
  public void onMessage(Context context, Group group, AVMessage msg) {
    Logger.d(msg.getMessage() + " receiver " + group);
    ChatService.onMessage(context, msg, msgListener, group);
  }

  @Override
  public void onQuit(Context context, Group group) {
    for (GroupEventListener listener : listeners) {
      listener.onQuit(group);
    }
    Logger.d(group.getGroupId() + " quit");
  }

  @Override
  public void onReject(Context context, Group group, String op, List<String> targetIds) {
    Logger.d(op + ":" + targetIds + " rejected");
  }

  @Override
  public void onMemberJoin(Context context, Group group, List<String> joinedPeerIds) {
    for (GroupEventListener listener : listeners) {
      listener.onMemberJoin(group, joinedPeerIds);
    }
    Logger.d(joinedPeerIds + " join " + group.getGroupId());
  }

  @Override
  public void onMemberLeft(Context context, Group group, List<String> leftPeerIds) {
    for (GroupEventListener listener : listeners) {
      listener.onMemberLeft(group, leftPeerIds);
    }
    Logger.d(leftPeerIds + " left " + group.getGroupId());
  }

  @Override
  public void onError(Context context, Group group, Throwable e) {
    Logger.d("on error " + e.getMessage());
    ChatService.onMessageError(e, msgListener);
  }

  public static void registerMsgListener(MsgListener listener) {
    msgListener = listener;
  }

  public static void unregisterMsgListener() {
    msgListener = null;
  }

  public static void addListener(GroupEventListener listener) {
    listeners.add(listener);
  }

  public static void removeListener(GroupEventListener listener) {
    listeners.remove(listener);
  }
}
