package com.lzw.talk.service;

import android.content.Context;
import android.content.Intent;
import com.avos.avoscloud.*;
import com.lzw.talk.avobject.ChatGroup;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;

import java.util.List;

/**
 * Created by lzw on 14-10-8.
 */
public class GroupService {
  public static final String GROUP_ID = "groupId";

  public static List<ChatGroup> findGroups() throws AVException {
    User user = User.curUser();
    AVQuery<ChatGroup> q = AVObject.getQuery(ChatGroup.class);
    q.whereEqualTo(ChatGroup.M, user.getObjectId());
    q.include(ChatGroup.OWNER);
    return q.find();
  }

  public static boolean isGroupOwner(ChatGroup chatGroup, User user) {
    return chatGroup.getOwner().equals(user);
  }

  public static void goChatGroupActivity(Context ctx, Class<?> clz, String groupId) {
    Intent intent = new Intent(ctx, clz);
    intent.putExtra(GROUP_ID, groupId);
    ctx.startActivity(intent);
  }

  public static ChatGroup getGroupByIntent(Intent intent) {
    String groupId = intent.getStringExtra(GROUP_ID);
    return App.lookupChatGroup(groupId);
  }

  public static void inviteMembers(ChatGroup chatGroup, List<User> users) {
    Group group = getGroup(chatGroup);
    List<String> userIds = UserService.transformIds(users);
    group.inviteMember(userIds);
  }

  public static Group getGroup(ChatGroup chatGroup) {
    Session session = ChatService.getSession();
    return session.getGroup(chatGroup.getObjectId());
  }

  public static void kickMembers(ChatGroup chatGroup, List<User> members) {
    Group group = getGroup(chatGroup);
    List<String> ids = UserService.transformIds(members);
    group.kickMember(ids);
  }
}
