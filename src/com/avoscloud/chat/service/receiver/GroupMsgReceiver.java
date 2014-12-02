package com.avoscloud.chat.service.receiver;

import android.content.Context;
import com.avos.avoscloud.AVGroupMessageReceiver;
import com.avos.avoscloud.AVMessage;
import com.avos.avoscloud.Group;
import com.avoscloud.chat.service.ChatService;
import com.avoscloud.chat.service.listener.GroupEventListener;
import com.avoscloud.chat.service.listener.MsgListener;
import com.avoscloud.chat.util.AVOSUtils;
import com.avoscloud.chat.util.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lzw on 14-10-8.
 */
public class GroupMsgReceiver extends AVGroupMessageReceiver {
  public static Set<GroupEventListener> groupListeners = new HashSet<GroupEventListener>();
  public static Set<MsgListener> msgListeners = new HashSet<MsgListener>();

  @Override
  public void onJoined(Context context, Group group) {
    for (GroupEventListener listener : groupListeners) {
      listener.onJoined(group);
    }
  }

  @Override
  public void onInviteToGroup(Context context, Group group, String byPeerId) {
    Logger.d("onInviteToGroup " + byPeerId + " groupId=" + group.getGroupId());
  }

  @Override
  public void onInvited(Context context, Group group, List<String> invitedPeers) {
    Logger.d("onInvited " + invitedPeers + " groupId=" + group.getGroupId());
  }

  @Override
  public void onKicked(Context context, Group group, List<String> kickedPeers) {
    Logger.d("kick " + group.getGroupId()  +" ids="+kickedPeers);
  }

  @Override
  public void onMessageSent(Context context, Group group, AVMessage message) {
    Logger.d("onMessageSent");
    AVOSUtils.logAVMessage(message);
    ChatService.onMessageSent(message, msgListeners, group);
  }

  @Override
  public void onMessageFailure(Context context, Group group, AVMessage message) {
    Logger.d("onMessageFailure");
    AVOSUtils.logAVMessage(message);
    ChatService.onMessageFailure(message,msgListeners,group);
  }

  @Override
  public void onMessage(Context context, Group group, AVMessage msg) {
    Logger.d("onMessage");
    AVOSUtils.logAVMessage(msg);
    ChatService.onMessage(context, msg, msgListeners, group);
  }

  @Override
  public void onQuit(Context context, Group group) {
    for (GroupEventListener listener : groupListeners) {
      listener.onQuit(group);
    }
    Logger.d(group.getGroupId() + " quit");
  }

  //签名被拒或聊天室满1000人被拒
  @Override
  public void onReject(Context context, Group group, String op, List<String> targetIds) {
    Logger.d(op + ":" + targetIds + " rejected");
  }

  @Override
  public void onMemberJoin(Context context, Group group, List<String> joinedPeerIds) {
    for (GroupEventListener listener : groupListeners) {
      listener.onMemberJoin(group, joinedPeerIds);
    }
    Logger.d(joinedPeerIds + " join " + group.getGroupId());
  }

  @Override
  public void onMemberLeft(Context context, Group group, List<String> leftPeerIds) {
    for (GroupEventListener listener : groupListeners) {
      listener.onMemberLeft(group, leftPeerIds);
    }
    Logger.d(leftPeerIds + " left " + group.getGroupId());
  }

  @Override
  public void onError(Context context, Group group, Throwable e) {
    //Utils.toast(context, e.getMessage());
    e.printStackTrace();
    //ChatService.onMessageError(e, msgListeners);
  }

  public static void addMsgListener(MsgListener listener) {
    msgListeners.add(listener);
  }

  public static void removeMsgListener(MsgListener listener) {
    msgListeners.remove(listener);
  }

  public static void addListener(GroupEventListener listener) {
    groupListeners.add(listener);
  }

  public static void removeListener(GroupEventListener listener) {
    groupListeners.remove(listener);
  }
}
