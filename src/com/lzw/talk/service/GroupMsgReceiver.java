package com.lzw.talk.service;

import android.content.Context;
import com.avos.avoscloud.AVGroupMessageReceiver;
import com.avos.avoscloud.AVMessage;
import com.avos.avoscloud.Group;

import java.util.List;

/**
 * Created by lzw on 14-10-8.
 */
public class GroupMsgReceiver extends AVGroupMessageReceiver {

  @Override
  public void onJoined(Context context, Group group) {

  }

  @Override
  public void onInvited(Context context, Group group, String byPeerId) {

  }

  @Override
  public void onKicked(Context context, Group group, String byPeerId) {

  }

  @Override
  public void onMessageSent(Context context, Group group, AVMessage message) {

  }

  @Override
  public void onMessageFailure(Context context, Group group, AVMessage message) {

  }

  @Override
  public void onMessage(Context context, Group group, AVMessage message) {

  }

  @Override
  public void onQuit(Context context, Group group) {

  }

  @Override
  public void onReject(Context context, Group group, String op, List<String> targetIds) {

  }

  @Override
  public void onMemberJoin(Context context, Group group, List<String> joinedPeerIds) {

  }

  @Override
  public void onMemberLeft(Context context, Group group, List<String> leftPeerIds) {

  }

  @Override
  public void onError(Context context, Group group, Throwable e) {

  }
}
