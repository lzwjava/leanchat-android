package com.avoscloud.chat.service.listener;

import com.avos.avoscloud.Group;

import java.util.List;

/**
 * Created by lzw on 14-10-8.
 */
//GroupMemberEvent
public interface GroupEventListener {

  void onJoined(Group group);

  void onMemberJoin(Group group, List<String> joinedPeerIds);

  void onMemberLeft(Group group, List<String> leftPeerIds);

  void onQuit(Group group);
}
