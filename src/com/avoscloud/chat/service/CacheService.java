package com.avoscloud.chat.service;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.Group;
import com.avoscloud.chat.avobject.ChatGroup;
import com.avoscloud.chat.base.C;

import java.util.*;

/**
 * Created by lzw on 14/12/19.
 */
public class CacheService {
  private static Map<String, ChatGroup> chatGroupsCache = new HashMap<String, ChatGroup>();
  private static Map<String, AVUser> usersCache = new HashMap<String, AVUser>();
  private static List<String> friendIds = new ArrayList<String>();
  private static ChatGroup currentChatGroup;

  public static AVUser lookupUser(String userId) {
    return usersCache.get(userId);
  }

  public static void registerUserCache(String userId, AVUser user) {
    usersCache.put(userId, user);
  }

  public static void registerUserCache(AVUser user) {
    registerUserCache(user.getObjectId(), user);
  }

  public static void registerBatchUser(List<AVUser> users) {
    for (AVUser user : users) {
      registerUserCache(user);
    }
  }

  public static ChatGroup lookupChatGroup(String groupId) {
    return chatGroupsCache.get(groupId);
  }

  public static void registerChatGroupsCache(List<ChatGroup> chatGroups) {
    for (ChatGroup chatGroup : chatGroups) {
      registerChatGroup(chatGroup);
    }
  }

  public static void registerChatGroup(ChatGroup chatGroup) {
    chatGroupsCache.put(chatGroup.getObjectId(), chatGroup);
  }

  public static List<String> getFriendIds() {
    return friendIds;
  }

  public static void setFriendIds(List<String> friendIds) {
    CacheService.friendIds = Collections.unmodifiableList(friendIds);
  }

  public static ChatGroup getCurrentChatGroup() {
    return currentChatGroup;
  }

  public static void setCurrentChatGroup(ChatGroup currentChatGroup) {
    CacheService.currentChatGroup = currentChatGroup;
  }

  public static boolean isCurrentGroup(Group group) {
    if (getCurrentChatGroup() != null && getCurrentChatGroup().getObjectId().equals(group.getGroupId())) {
      return true;
    } else {
      return false;
    }
  }

  public static List<AVUser> cacheUserAndGet(List<String> ids) throws AVException {
    Set<String> uncachedIds = new HashSet<String>();
    for (String id : ids) {
      if (lookupUser(id) == null) {
        uncachedIds.add(id);
      }
    }
    findUsers(new ArrayList<String>(uncachedIds));
    List<AVUser> users = new ArrayList<AVUser>();
    for (String id : ids) {
      users.add(lookupUser(id));
    }
    return users;
  }

  public static List<AVUser> findUsers(List<String> userIds) throws AVException {
    if (userIds.size() <= 0) {
      return new ArrayList<AVUser>();
    }
    AVQuery<AVUser> q = AVUser.getQuery(AVUser.class);
    q.whereContainedIn(C.OBJECT_ID, userIds);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    List<AVUser> users = q.find();
    registerBatchUser(users);
    return users;
  }
}
