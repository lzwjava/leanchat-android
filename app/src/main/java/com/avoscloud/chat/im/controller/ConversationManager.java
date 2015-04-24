package com.avoscloud.chat.im.controller;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.*;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avoscloud.chat.base.C;
import com.avoscloud.chat.im.db.RoomsTable;
import com.avoscloud.chat.im.model.ConversationType;
import com.avoscloud.chat.im.model.Room;
import com.avoscloud.chat.service.CacheService;
import com.avoscloud.chat.util.Utils;
import de.greenrobot.event.EventBus;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lzw on 15/2/11.
 */
public class ConversationManager {
  private static ConversationManager conversationManager;
  private static AVIMConversationEventHandler convHandler = new AVIMConversationEventHandler() {
    @Override
    public void onMemberLeft(AVIMClient client, AVIMConversation conversation, List<String> members, String kickedBy) {
      Utils.toast(MessageUtils.nameByUserIds(members) + " left, kicked by " + MessageUtils.nameByUserId(kickedBy));
      getInstance().postConvChanged(conversation);
    }

    @Override
    public void onMemberJoined(AVIMClient client, AVIMConversation conversation, List<String> members, String invitedBy) {
      Utils.toast(MessageUtils.nameByUserIds(members) + " joined , invited by " + MessageUtils.nameByUserId(invitedBy));
      getInstance().postConvChanged(conversation);
    }

    @Override
    public void onKicked(AVIMClient client, AVIMConversation conversation, String kickedBy) {
      Utils.toast("you are kicked by " + MessageUtils.nameByUserId(kickedBy));
    }

    @Override
    public void onInvited(AVIMClient client, AVIMConversation conversation, String operator) {
      Utils.toast("you are invited by " + MessageUtils.nameByUserId(operator));
    }
  };
  private ChatManager chatManager;

  public ConversationManager() {
    chatManager = ChatManager.getInstance();
  }

  public static synchronized ConversationManager getInstance() {
    if (conversationManager == null) {
      conversationManager = new ConversationManager();
    }
    return conversationManager;
  }

  public static boolean isValidConv(AVIMConversation conv) {
    Object type = conv.getAttribute(ConversationType.TYPE_KEY);
    if (type == null) {
      return false;
    }
    int typeInt = (Integer) type;
    if (typeInt == ConversationType.Single.getValue() || typeInt == ConversationType.Group.getValue()) {
      ConversationType conversationType = ConversationType.fromInt(typeInt);
      if (conversationType == ConversationType.Group) {
        if (conv.getName() == null) {
          return false;
        }
      }
    } else {
      return false;
    }
    return true;
  }

  public static ConversationType typeOfConv(AVIMConversation conv) {
    try {
      Object typeObject = conv.getAttribute(ConversationType.TYPE_KEY);
      int typeInt = (Integer) typeObject;
      return ConversationType.fromInt(typeInt);
    } catch (NullPointerException e) {
      e.printStackTrace();
      return ConversationType.Group;
    }
  }

  public static String otherIdOfConv(AVIMConversation conv) {
    List<String> members = conv.getMembers();
    if (typeOfConv(conv) != ConversationType.Single || members.size() != 2) {
      throw new IllegalStateException("can't get other id, members=" + conv.getMembers());
    }
    String selfId = AVUser.getCurrentUser().getObjectId();
    if (members.get(0).equals(selfId)) {
      return members.get(1);
    } else {
      return members.get(0);
    }
  }

  public static String nameOfConv(AVIMConversation conv) {
    if (typeOfConv(conv) == ConversationType.Single) {
      String otherId = otherIdOfConv(conv);
      AVUser user = CacheService.lookupUser(otherId);
      return user.getUsername();
    } else {
      try {
        String name = conv.getName();
        return name;
      } catch (NullPointerException e) {
        e.printStackTrace();
        return "conv name";
      }
    }
  }

  public static String titleOfConv(AVIMConversation conv) {
    if (typeOfConv(conv) == ConversationType.Single) {
      return nameOfConv(conv);
    } else {
      List<String> members = conv.getMembers();
      return nameOfConv(conv) + " (" + members.size() + ")";
    }
  }

  public static AVIMConversationEventHandler getConvHandler() {
    return convHandler;
  }

  public List<Room> findAndCacheRooms() throws AVException, InterruptedException {
    RoomsTable roomsTable = RoomsTable.getCurrentUserInstance();
    List<Room> rooms = roomsTable.selectRooms();
    cacheAndFillRooms(rooms);
    return rooms;
  }

  public void cacheAndFillRooms(List<Room> rooms) throws AVException, InterruptedException {
    List<String> convids = new ArrayList<String>();
    for (Room room : rooms) {
      convids.add(room.getConvid());
    }
    final CountDownLatch latch = new CountDownLatch(1);
    final AVException[] es = new AVException[1];
    CacheService.cacheConvs(convids, new AVIMConversationCallback() {
      @Override
      public void done(AVException e) {
        es[0] = e;
        latch.countDown();
      }
    });
    latch.await();
    if (es[0] != null) {
      throw es[0];
    }
    List<String> userIds = new ArrayList<String>();
    for (Room room : rooms) {
      AVIMConversation conv = CacheService.lookupConv(room.getConvid());
      room.setConv(conv);
      if (typeOfConv(conv) == ConversationType.Single) {
        userIds.add(otherIdOfConv(conv));
      }
    }
    CacheService.cacheUsers(new ArrayList<String>(userIds));
  }

  private void findAndCacheConv(String convid, final AVIMConversationCreatedCallback callback) {
    final AVIMConversation conv = CacheService.lookupConv(convid);
    if (conv == null) {
      AVIMConversationQuery q = chatManager.getQuery();
      q.whereEqualTo(C.OBJECT_ID, convid);
      q.findInBackground(new AVIMConversationQueryCallback() {
        @Override
        public void done(List<AVIMConversation> conversations, AVException e) {
          if (e != null) {
            callback.done(null, e);
          } else {
            if (conversations.size() > 0) {
              CacheService.registerConv(conversations.get(0));
              callback.done(conversations.get(0), null);
            } else {
              callback.done(null, null);
            }
          }
        }
      });
    } else {
      callback.done(conv, null);
    }
  }

  public void updateName(final AVIMConversation conv, String newName, final AVIMConversationCallback callback) {
    conv.setName(newName);
    conv.updateInfoInBackground(new AVIMConversationCallback() {
      @Override
      public void done(AVException e) {
        if (e != null) {
          callback.done(e);
        } else {
          conversationManager.postConvChanged(conv);
          callback.done(null);
        }
      }
    });
  }

  public void postConvChanged(AVIMConversation conv) {
    if (CacheService.getCurConv() != null && CacheService.getCurConv().getConversationId().equals(conv.getConversationId())) {
      CacheService.setCurConv(conv);
    }
    ConversationChangeEvent conversationChangeEvent = new ConversationChangeEvent(conv);
    EventBus.getDefault().post(conversationChangeEvent);
  }

  public void findGroupConvsIncludeMe(AVIMConversationQueryCallback callback) {
    AVUser user = AVUser.getCurrentUser();
    AVIMConversationQuery q = chatManager.getQuery();
    q.containsMembers(Arrays.asList(user.getObjectId()));
    q.whereEqualTo(ConversationType.ATTR_TYPE_KEY, ConversationType.Group.getValue());
    q.orderByDescending(C.UPDATED_AT);
    q.findInBackground(callback);
  }

  public void findConvs(List<String> ids, AVIMConversationQueryCallback callback) {
    if (ids.size() > 0) {
      AVIMConversationQuery q = chatManager.getQuery();
      q.whereContainsIn(C.OBJECT_ID, ids);
      q.findInBackground(callback);
    } else {
      callback.done(new ArrayList<AVIMConversation>(), null);
    }
  }

  public void fetchConv(String id, final AVIMConversationCreatedCallback callback) {
    AVIMConversationQuery q = chatManager.getQuery();
    q.whereEqualTo(C.OBJECT_ID, id);
    q.findInBackground(new AVIMConversationQueryCallback() {
      @Override
      public void done(List<AVIMConversation> conversations, AVException e) {
        if (e != null) {
          callback.done(null, e);
        } else {
          callback.done(conversations.get(0), null);
        }
      }
    });
  }

  public List<AVUser> findGroupMembers(AVIMConversation conv) throws AVException {
    List<AVUser> users = CacheService.findUsers(conv.getMembers());
    CacheService.registerUsers(users);
    return users;
  }

  public void fetchConvWithUserId(String userId, final AVIMConversationCreatedCallback callback) {
    AVUser me = AVUser.getCurrentUser();
    final List<String> members = new ArrayList<String>();
    members.add(userId);
    members.add(me.getObjectId());
    AVIMConversationQuery convQ = chatManager.getQuery();
    convQ.withMembers(members);
    convQ.whereEqualTo(ConversationType.ATTR_TYPE_KEY, ConversationType.Single.getValue());
    convQ.orderByDescending(C.UPDATED_AT);
    convQ.findInBackground(new AVIMConversationQueryCallback() {
      @Override
      public void done(List<AVIMConversation> conversations, AVException e) {
        if (e != null) {
          callback.done(null, e);
        } else {
          List<AVIMConversation> filterConvs = new ArrayList<AVIMConversation>();
          for (AVIMConversation conversation : conversations) {
            if (conversation.getAttribute(ConversationType.TYPE_KEY) != null) {
              filterConvs.add(conversation);
            }
          }
          if (filterConvs.size() > 0) {
            callback.done(filterConvs.get(0), null);
          } else {
            Map<String, Object> attrs = new HashMap<String, Object>();
            attrs.put(ConversationType.TYPE_KEY, ConversationType.Single.getValue());
            chatManager.getImClient().createConversation(members, attrs, callback);
          }
        }
      }
    });
  }

  public void createGroupConv(List<String> members, final AVIMConversationCreatedCallback callback) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(ConversationType.TYPE_KEY, ConversationType.Group.getValue());
    final String name = MessageUtils.nameByUserIds(members);
    map.put(ConversationType.NAME_KEY, name);
    chatManager.getImClient().createConversation(members, map, callback);
  }

  /**
   * msgId 、time 共同使用，防止某毫秒时刻有重复消息
   */
  public List<AVIMTypedMessage> queryHistoryMessage(AVIMConversation conv, String msgId, long time, int limit) throws Exception {
    final AVException[] es = new AVException[1];
    final List<AVIMMessage> msgs = new ArrayList<AVIMMessage>();
    final CountDownLatch latch = new CountDownLatch(1);
    conv.queryMessages(msgId, time, limit, new AVIMMessagesQueryCallback() {
      @Override
      public void done(List<AVIMMessage> avimMessages, AVException e) {
        if (e != null) {
          es[0] = e;
        } else {
          msgs.addAll(avimMessages);
        }
        latch.countDown();
      }
    });
    latch.await();
    if (es[0] != null) {
      throw es[0];
    } else {
      List<AVIMTypedMessage> resultMsgs = new ArrayList<AVIMTypedMessage>();
      for (AVIMMessage msg : msgs) {
        if (msg instanceof AVIMTypedMessage) {
          resultMsgs.add((AVIMTypedMessage) msg);
        }
      }
      return resultMsgs;
    }
  }
}
