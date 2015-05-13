package com.avoscloud.leanchatlib.controller;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avoscloud.leanchatlib.model.ConversationType;
import com.avoscloud.leanchatlib.model.UserInfo;

import java.util.List;

/**
 * Created by lzw on 15/4/26.
 */
public class ConversationHelper {
  public static boolean isValidConversation(AVIMConversation conversation) {
    Object type = conversation.getAttribute(ConversationType.TYPE_KEY);
    if (type == null) {
      return false;
    }
    int typeInt = (Integer) type;
    if (typeInt == ConversationType.Single.getValue() || typeInt == ConversationType.Group.getValue()) {
      ConversationType conversationType = ConversationType.fromInt(typeInt);
      if (conversationType == ConversationType.Group) {
        if (conversation.getName() == null) {
          return false;
        }
      }
    } else {
      return false;
    }
    return true;
  }

  public static ConversationType typeOfConversation(AVIMConversation conversation) {
    try {
      Object typeObject = conversation.getAttribute(ConversationType.TYPE_KEY);
      int typeInt = (Integer) typeObject;
      return ConversationType.fromInt(typeInt);
    } catch (NullPointerException e) {
      e.printStackTrace();
      return ConversationType.Group;
    }
  }

  public static String otherIdOfConversation(AVIMConversation conversation) {
    List<String> members = conversation.getMembers();
    if (typeOfConversation(conversation) != ConversationType.Single || members.size() != 2) {
      throw new IllegalStateException("can't get other id, members=" + conversation.getMembers());
    }
    String selfId = ChatManager.getInstance().getSelfId();
    if (members.get(0).equals(selfId)) {
      return members.get(1);
    } else {
      return members.get(0);
    }
  }

  public static String nameOfConversation(AVIMConversation conversation) {
    if (conversation == null) {
      throw new NullPointerException("conversation is null");
    }
    if (typeOfConversation(conversation) == ConversationType.Single) {
      String otherId = otherIdOfConversation(conversation);
      UserInfo user = ChatManager.getInstance().getChatManagerAdapter().getUserInfoById(otherId);
      return user.getUsername();
    } else {
      return conversation.getName();
    }
  }

  public static String titleOfConversation(AVIMConversation conversation) {
    if (typeOfConversation(conversation) == ConversationType.Single) {
      return nameOfConversation(conversation);
    } else {
      List<String> members = conversation.getMembers();
      return nameOfConversation(conversation) + " (" + members.size() + ")";
    }
  }
}
