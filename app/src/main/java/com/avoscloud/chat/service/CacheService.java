package com.avoscloud.chat.service;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMReservedMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMAudioMessage;
import com.avoscloud.chat.base.C;
import com.avoscloud.chat.service.chat.ConvManager;
import com.avoscloud.chat.service.chat.MsgUtils;
import com.avoscloud.chat.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by lzw on 14/12/19.
 */
public class CacheService {
  private static Map<String, AVIMConversation> cachedConvs = new HashMap<String, AVIMConversation>();
  private static Map<String, AVUser> cachedUsers = new HashMap<String, AVUser>();
  private static List<String> friendIds = new ArrayList<String>();
  private static AVIMConversation curConv;

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

  public static void registerConvIfNone(AVIMConversation conv) {
    if (lookupConv(conv.getConversationId()) == null) {
      registerConv(conv);
    }
  }

  public static List<String> getFriendIds() {
    return friendIds;
  }

  public static void setFriendIds(List<String> friendIds) {
    CacheService.friendIds = Collections.unmodifiableList(friendIds);
  }

  public static AVIMConversation getCurConv() {
    return curConv;
  }

  public static void setCurConv(AVIMConversation curConv) {
    CacheService.curConv = curConv;
  }

  public static boolean isCurConvid(String convid) {
    return curConv != null && curConv.getConversationId().equals(convid);
  }

  public static boolean isCurConv(AVIMConversation conv) {
    if (getCurConv() != null && getCurConv().getConversationId().equals(conv.getConversationId())) {
      return true;
    } else {
      return false;
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
      return new ArrayList<AVUser>();
    }
    AVQuery<AVUser> q = AVUser.getQuery(AVUser.class);
    q.whereContainedIn(C.OBJECT_ID, userIds);
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
    ConvManager.getInstance().findConvs(new ArrayList<String>(uncachedIds), new AVIMConversationQueryCallback() {
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

  public static void cacheMsgs(List<AVIMTypedMessage> msgs) throws IOException, AVException {
    Set<String> userIds = new HashSet<String>();
    for (AVIMTypedMessage msg : msgs) {
      AVIMReservedMessageType type = AVIMReservedMessageType.getAVIMReservedMessageType(msg.getMessageType());
      if (type == AVIMReservedMessageType.AudioMessageType) {
        File file = new File(MsgUtils.getFilePath(msg));
        if (!file.exists()) {
          AVIMAudioMessage audioMsg = (AVIMAudioMessage) msg;
          String url = audioMsg.getFileUrl();
          Utils.downloadFileIfNotExists(url, file);
        }
      }
      userIds.add(msg.getFrom());
    }
    cacheUsers(new ArrayList<String>(userIds));
  }

  public static void cacheUserIfNone(String userId) throws AVException {
    if (lookupUser(userId) == null) {
      registerUser(UserService.findUser(userId));
    }
  }
}
