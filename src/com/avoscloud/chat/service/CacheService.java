package com.avoscloud.chat.service;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.Group;
import com.avoscloud.chat.avobject.ChatGroup;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.base.C;

import java.util.*;

/**
 * Created by lzw on 14/12/19.
 */
public class CacheService {
  private static Map<String, ChatGroup> chatGroupsCache = new HashMap<String, ChatGroup>();
  private static Map<String, User> usersCache = new HashMap<String, User>();
  private static List<String> friendIds = new ArrayList<String>();
  private static ChatGroup currentChatGroup;

  public static User lookupUser(String userId) {
    return usersCache.get(userId);
  }

  public static void registerUserCache(String userId, User user) {
    usersCache.put(userId, user);
  }

  public static void registerUserCache(User user) {
    registerUserCache(user.getObjectId(), user);
  }

  public static void registerBatchUser(List<User> users) {
    for (User user : users) {
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

  public static List<User> cacheUserAndGet(List<String> ids) throws AVException {
    Set<String> uncachedIds = new HashSet<String>();
    for (String id : ids) {
      if (lookupUser(id) == null) {
        uncachedIds.add(id);
      }
    }
    findUsers(new ArrayList<String>(uncachedIds));
    List<User> users = new ArrayList<User>();
    for (String id : ids) {
      users.add(lookupUser(id));
    }
    return users;
  }

  public static List<User> findUsers(List<String> userIds) throws AVException {
    if (userIds.size() <= 0) {
      return new ArrayList<User>();
    }
    AVQuery<User> q = User.getQuery(User.class);
    q.whereContainedIn(C.OBJECT_ID, userIds);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    List<User> users = q.find();
    registerBatchUser(users);
    return users;
  }
}
