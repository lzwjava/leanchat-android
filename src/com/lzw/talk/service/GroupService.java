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
    return true;
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

  public static void addMembers(ChatGroup chatGroup, List<User> users) {
    Session session = ChatService.getSession();
    Group group = session.getGroup(chatGroup.getObjectId());
    List<String> userIds = UserService.transformIds(users);
    group.inviteMember(userIds);
  }
}
