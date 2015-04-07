package com.avoscloud.chat.service.chat;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.*;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avoscloud.chat.base.C;
import com.avoscloud.chat.db.RoomsTable;
import com.avoscloud.chat.entity.ConvType;
import com.avoscloud.chat.entity.Room;
import com.avoscloud.chat.service.CacheService;
import com.avoscloud.chat.service.event.ConvChangeEvent;
import com.avoscloud.chat.util.Utils;
import de.greenrobot.event.EventBus;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lzw on 15/2/11.
 */
public class ConvManager {
  private static ConvManager convManager;
  private static AVIMConversationEventHandler convHandler = new AVIMConversationEventHandler() {
    @Override
    public void onMemberLeft(AVIMClient client, AVIMConversation conversation, List<String> members, String kickedBy) {
      Utils.toast(MsgUtils.nameByUserIds(members) + " left, kicked by " + MsgUtils.nameByUserId(kickedBy));
      getInstance().postConvChanged(conversation);
    }

    @Override
    public void onMemberJoined(AVIMClient client, AVIMConversation conversation, List<String> members, String invitedBy) {
      Utils.toast(MsgUtils.nameByUserIds(members) + " joined , invited by " + MsgUtils.nameByUserId(invitedBy));
      getInstance().postConvChanged(conversation);
    }

    @Override
    public void onKicked(AVIMClient client, AVIMConversation conversation, String kickedBy) {
      Utils.toast("you are kicked by " + MsgUtils.nameByUserId(kickedBy));
    }

    @Override
    public void onInvited(AVIMClient client, AVIMConversation conversation, String operator) {
      Utils.toast("you are invited by " + MsgUtils.nameByUserId(operator));
    }
  };
  private IM im;

  public ConvManager() {
    im = IM.getInstance();
  }

  public static synchronized ConvManager getInstance() {
    if (convManager == null) {
      convManager = new ConvManager();
    }
    return convManager;
  }

  public static boolean isValidConv(AVIMConversation conv) {
    Object type = conv.getAttribute(ConvType.TYPE_KEY);
    if (type == null) {
      return false;
    }
    int typeInt = (Integer) type;
    if (typeInt == ConvType.Single.getValue() || typeInt == ConvType.Group.getValue()) {
      ConvType convType = ConvType.fromInt(typeInt);
      if (convType == ConvType.Group) {
        if (conv.getName() == null) {
          return false;
        }
      }
    } else {
      return false;
    }
    return true;
  }

  public static ConvType typeOfConv(AVIMConversation conv) {
    try {
      Object typeObject = conv.getAttribute(ConvType.TYPE_KEY);
      int typeInt = (Integer) typeObject;
      return ConvType.fromInt(typeInt);
    } catch (NullPointerException e) {
      e.printStackTrace();
      return ConvType.Group;
    }
  }

  public static String otherIdOfConv(AVIMConversation conv) {
    List<String> members = conv.getMembers();
    if (typeOfConv(conv) != ConvType.Single || members.size() != 2) {
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
    if (typeOfConv(conv) == ConvType.Single) {
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
    if (typeOfConv(conv) == ConvType.Single) {
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
      if (typeOfConv(conv) == ConvType.Single) {
        userIds.add(otherIdOfConv(conv));
      }
    }
    CacheService.cacheUsers(new ArrayList<String>(userIds));
  }

  private void findAndCacheConv(String convid, final AVIMConversationCreatedCallback callback) {
    final AVIMConversation conv = CacheService.lookupConv(convid);
    if (conv == null) {
      AVIMConversationQuery q = im.getQuery();
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
          convManager.postConvChanged(conv);
          callback.done(null);
        }
      }
    });
  }

  public void postConvChanged(AVIMConversation conv) {
    if (CacheService.getCurConv() != null && CacheService.getCurConv().getConversationId().equals(conv.getConversationId())) {
      CacheService.setCurConv(conv);
    }
    ConvChangeEvent convChangeEvent = new ConvChangeEvent(conv);
    EventBus.getDefault().post(convChangeEvent);
  }

  public void findGroupConvsIncludeMe(AVIMConversationQueryCallback callback) {
    AVUser user = AVUser.getCurrentUser();
    AVIMConversationQuery q = im.getQuery();
    q.containsMembers(Arrays.asList(user.getObjectId()));
    q.whereEqualTo(ConvType.ATTR_TYPE_KEY, ConvType.Group.getValue());
    q.orderByDescending(C.UPDATED_AT);
    q.findInBackground(callback);
  }

  public void findConvs(List<String> ids, AVIMConversationQueryCallback callback) {
    if (ids.size() > 0) {
      AVIMConversationQuery q = im.getQuery();
      q.whereContainsIn(C.OBJECT_ID, ids);
      q.findInBackground(callback);
    } else {
      callback.done(new ArrayList<AVIMConversation>(), null);
    }
  }

  public void fetchConv(String id, final AVIMConversationCreatedCallback callback) {
    AVIMConversationQuery q = im.getQuery();
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
    AVIMConversationQuery convQ = im.getQuery();
    convQ.withMembers(members);
    convQ.whereEqualTo(ConvType.ATTR_TYPE_KEY, ConvType.Single.getValue());
    convQ.orderByDescending(C.UPDATED_AT);
    convQ.findInBackground(new AVIMConversationQueryCallback() {
      @Override
      public void done(List<AVIMConversation> conversations, AVException e) {
        if (e != null) {
          callback.done(null, e);
        } else {
          List<AVIMConversation> filterConvs = new ArrayList<AVIMConversation>();
          for (AVIMConversation conversation : conversations) {
            if (conversation.getAttribute(ConvType.TYPE_KEY) != null) {
              filterConvs.add(conversation);
            }
          }
          if (filterConvs.size() > 0) {
            callback.done(filterConvs.get(0), null);
          } else {
            Map<String, Object> attrs = new HashMap<String, Object>();
            attrs.put(ConvType.TYPE_KEY, ConvType.Single.getValue());
            im.getImClient().createConversation(members, attrs, callback);
          }
        }
      }
    });
  }

  public void createGroupConv(List<String> members, final AVIMConversationCreatedCallback callback) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(ConvType.TYPE_KEY, ConvType.Group.getValue());
    final String name = MsgUtils.nameByUserIds(members);
    map.put(ConvType.NAME_KEY, name);
    im.getImClient().createConversation(members, map, callback);
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
