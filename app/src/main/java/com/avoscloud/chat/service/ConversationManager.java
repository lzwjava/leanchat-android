package com.avoscloud.chat.service;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.*;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avoscloud.chat.base.C;
import com.avoscloud.chat.util.Utils;
import com.avoscloud.leanchatlib.controller.ChatManager;
import com.avoscloud.leanchatlib.controller.ConversationHelper;
import com.avoscloud.leanchatlib.controller.MessageHelper;
import com.avoscloud.leanchatlib.model.ConversationType;
import com.avoscloud.leanchatlib.model.Room;
import de.greenrobot.event.EventBus;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lzw on 15/2/11.
 */
public class ConversationManager {
  private static ConversationManager conversationManager;
  private static AVIMConversationEventHandler conversationHandler = new AVIMConversationEventHandler() {
    @Override
    public void onMemberLeft(AVIMClient client, AVIMConversation conversation, List<String> members, String kickedBy) {
      Utils.toast(MessageHelper.nameByUserIds(members) + " left, kicked by " + MessageHelper.nameByUserId(kickedBy));
      getInstance().postConvChanged(conversation);
    }

    @Override
    public void onMemberJoined(AVIMClient client, AVIMConversation conversation, List<String> members, String invitedBy) {
      Utils.toast(MessageHelper.nameByUserIds(members) + " joined , invited by " + MessageHelper.nameByUserId(invitedBy));
      getInstance().postConvChanged(conversation);
    }

    @Override
    public void onKicked(AVIMClient client, AVIMConversation conversation, String kickedBy) {
      Utils.toast("you are kicked by " + MessageHelper.nameByUserId(kickedBy));
    }

    @Override
    public void onInvited(AVIMClient client, AVIMConversation conversation, String operator) {
      Utils.toast("you are invited by " + MessageHelper.nameByUserId(operator));
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

  public static AVIMConversationEventHandler getConversationHandler() {
    return conversationHandler;
  }

  public List<Room> findAndCacheRooms() throws AVException, InterruptedException {
    List<Room> rooms = chatManager.findRecentRooms();
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
      if (ConversationHelper.typeOfConv(conv) == ConversationType.Single) {
        userIds.add(ConversationHelper.otherIdOfConv(conv));
      }
    }
    CacheService.cacheUsers(new ArrayList<String>(userIds));
  }

  public void updateName(final AVIMConversation conv, String newName, final AVIMConversationCallback callback) {
    conv.setName(newName);
    conv.updateInfoInBackground(new AVIMConversationCallback() {
      @Override
      public void done(AVException e) {
        if (e != null) {
          callback.done(e);
        } else {
          postConvChanged(conv);
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

  public void findGroupConversationsIncludeMe(AVIMConversationQueryCallback callback) {
    AVIMConversationQuery q = chatManager.getQuery();
    q.containsMembers(Arrays.asList(chatManager.getSelfId()));
    q.whereEqualTo(ConversationType.ATTR_TYPE_KEY, ConversationType.Group.getValue());
    q.orderByDescending(C.UPDATED_AT);
    q.findInBackground(callback);
  }

  public void findConversationsByConversationIds(List<String> ids, AVIMConversationQueryCallback callback) {
    if (ids.size() > 0) {
      AVIMConversationQuery q = chatManager.getQuery();
      q.whereContainsIn(C.OBJECT_ID, ids);
      q.findInBackground(callback);
    } else {
      callback.done(new ArrayList<AVIMConversation>(), null);
    }
  }

  public void createGroupConversation(List<String> members, final AVIMConversationCreatedCallback callback) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(ConversationType.TYPE_KEY, ConversationType.Group.getValue());
    final String name = MessageHelper.nameByUserIds(members);
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
