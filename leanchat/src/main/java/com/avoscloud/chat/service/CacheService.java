package com.avoscloud.chat.service;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avoscloud.chat.base.Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lzw on 14/12/19.
 */
public class CacheService {
  private static Map<String, AVIMConversation> cachedConvs = new ConcurrentHashMap<>();
  private static Map<String, AVUser> cachedUsers = new ConcurrentHashMap<>();
  private static volatile List<String> friendIds = new ArrayList<String>();
  private static volatile String currentConversationId;

  public static AVUser lookupUser(String userId) {
    return cachedUsers.get(userId);
  }

  public static void registerUser(AVUser user) {
    cachedUsers.put(user.getObjectId(), user);
  }

  public static void registerUsers(List<AVUser> users) {
    for (AVUser user : users) {
      registerUser(user);
    }
  }

  public static AVIMConversation lookupConv(String convid) {
    return cachedConvs.get(convid);
  }

  public static void registerConvs(List<AVIMConversation> convs) {
    for (AVIMConversation conv : convs) {
      registerConv(conv);
    }
  }

  public static void registerConv(AVIMConversation conv) {
    cachedConvs.put(conv.getConversationId(), conv);
  }


  public static List<String> getFriendIds() {
    return friendIds;
  }

  public static AVIMConversation getCurrentConversation() {
    if (currentConversationId == null) {
      return null;
    } else {
      return lookupConv(currentConversationId);
    }
  }

  public static void setCurrentConversation(AVIMConversation currentConversation) {
    if (currentConversation != null) {
      registerConv(currentConversation);
      CacheService.currentConversationId = currentConversation.getConversationId();
    } else {
      CacheService.currentConversationId = null;
    }
  }

  public static void cacheUsers(List<String> ids) throws AVException {
    Set<String> uncachedIds = new HashSet<String>();
    for (String id : ids) {
      if (lookupUser(id) == null) {
        uncachedIds.add(id);
      }
    }
    List<AVUser> foundUsers = findUsers(new ArrayList<String>(uncachedIds));
    registerUsers(foundUsers);
  }

  public static List<AVUser> findUsers(List<String> userIds) throws AVException {
    if (userIds.size() <= 0) {
      return Collections.EMPTY_LIST;
    }
    AVQuery<AVUser> q = AVUser.getQuery();
    q.whereContainedIn(Constant.OBJECT_ID, userIds);
    q.setLimit(1000);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    return q.find();
  }

  public static void cacheConvs(List<String> ids, final AVIMConversationCallback callback) throws AVException {
    Set<String> uncachedIds = new HashSet<String>();
    for (String id : ids) {
      if (lookupConv(id) == null) {
        uncachedIds.add(id);
      }
    }
    ConversationManager.getInstance().findConversationsByConversationIds(new ArrayList<String>(uncachedIds), new AVIMConversationQueryCallback() {
      @Override
      public void done(List<AVIMConversation> conversations, AVException e) {
        if (e != null) {
          callback.done(e);
        } else {
          registerConvs(conversations);
          callback.done(null);
        }
      }
    });
  }

  public static void cacheUserIfNone(String userId) throws AVException {
    if (lookupUser(userId) == null) {
      registerUser(UserService.findUser(userId));
    }
  }
}
