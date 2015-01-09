package com.avoscloud.chat.service;

import com.avos.avoscloud.*;
import com.avoscloud.chat.avobject.ChatGroup;
import com.avoscloud.chat.base.C;

import java.util.*;

/**
 * Created by lzw on 14-10-8.
 */
public class GroupService {
  public static final String GROUP_ID = "groupId";

  public static List<ChatGroup> findGroups() throws AVException {
    AVUser user = AVUser.getCurrentUser();
    AVQuery<ChatGroup> q = AVObject.getQuery(ChatGroup.class);
    q.whereEqualTo(ChatGroup.M, user.getObjectId());
    q.orderByDescending(C.UPDATED_AT);
    q.include(ChatGroup.OWNER);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    return q.find();
  }

  public static boolean isGroupOwner(ChatGroup chatGroup, AVUser user) {
    return chatGroup.getOwner().equals(user);
  }

  public static void inviteMembers(ChatGroup chatGroup, List<String> users) {
    Group group = getGroup(chatGroup);
    group.inviteMember(users);
  }

  public static Group getGroup(ChatGroup chatGroup) {
    Session session = ChatService.getSession();
    return session.getGroup(chatGroup.getObjectId());
  }

  public static void kickMember(ChatGroup chatGroup, AVUser member) {
    Group group = getGroup(chatGroup);
    group.kickMember(Arrays.asList(member.getObjectId()));
  }

  public static ChatGroup setNewChatGroupData(String groupId, String newGroupName) throws AVException {
    CloudService.saveChatGroup(groupId, AVUser.getCurrentUser().getObjectId(), newGroupName);
    ChatGroup chatGroup = ChatGroup.createWithoutData(ChatGroup.class, groupId);
    chatGroup.fetch("owner");
    return chatGroup;
  }

  public static void cacheChatGroups(List<String> ids) throws AVException {
    Set<String> uncachedIds = new HashSet<String>();
    for (String id : ids) {
      if (CacheService.lookupChatGroup(id) == null) {
        uncachedIds.add(id);
      }
    }
    findChatGroups(new ArrayList<String>(uncachedIds));
  }

  private static List<ChatGroup> findChatGroups(List<String> ids) throws AVException {
    if (ids.size() == 0) {
      return new ArrayList<ChatGroup>();
    }
    AVQuery<ChatGroup> q = AVObject.getQuery(ChatGroup.class);
    q.whereContainedIn(C.OBJECT_ID, ids);
    q.include(ChatGroup.OWNER);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    List<ChatGroup> chatGroups = q.find();
    CacheService.registerChatGroupsCache(chatGroups);
    return chatGroups;
  }

  public static void cacheChatGroupIfNone(String groupId) throws AVException {
    if (CacheService.lookupChatGroup(groupId) == null) {
      cacheChatGroups(Arrays.asList(groupId));
    }
  }
}
