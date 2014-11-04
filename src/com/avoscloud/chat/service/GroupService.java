package com.avoscloud.chat.service;

import com.avos.avoscloud.*;
import com.avoscloud.chat.avobject.ChatGroup;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.base.C;
import com.avoscloud.chat.util.ChatUtils;

import java.util.Arrays;
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
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    return q.find();
  }

  public static boolean isGroupOwner(ChatGroup chatGroup, User user) {
    return chatGroup.getOwner().equals(user);
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

  public static void kickMember(ChatGroup chatGroup, User member) {
    Group group = getGroup(chatGroup);
    group.kickMember(Arrays.asList(member.getObjectId()));
  }

  public static ChatGroup setNewChatGroupData(String groupId, String newGroupName) throws AVException {
    CloudService.saveChatGroup(groupId,User.curUser().getObjectId(),newGroupName);
    ChatGroup chatGroup = ChatGroup.createWithoutData(ChatGroup.class, groupId);
    chatGroup.fetch("owner");
    return chatGroup;
  }

  public static void cacheChatGroups(List<String> ids) throws AVException {
    if (ids.size() == 0) {
      return;
    }
    findChatGroups(ids);
  }

  private static List<ChatGroup> findChatGroups(List<String> ids) throws AVException {
    AVQuery<ChatGroup> q = AVObject.getQuery(ChatGroup.class);
    q.whereContainedIn(C.OBJECT_ID, ids);
    q.include(ChatGroup.OWNER);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    List<ChatGroup> chatGroups = q.find();
    App.registerChatGroupsCache(chatGroups);
    return chatGroups;
  }

  public static void cacheChatGroupIfNone(String groupId) throws AVException {
    if (App.lookupChatGroup(groupId) == null) {
      cacheChatGroups(Arrays.asList(groupId));
    }
  }
}
