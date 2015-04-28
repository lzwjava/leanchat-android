package com.avoscloud.leanchatlib.controller;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avoscloud.leanchatlib.model.UserInfo;
import com.avoscloud.leanchatlib.model.ConversationType;

import java.util.List;

/**
 * Created by lzw on 15/4/26.
 */
public class ConversationHelper {
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
    String selfId = ChatManager.getInstance().getSelfId();
    if (members.get(0).equals(selfId)) {
      return members.get(1);
    } else {
      return members.get(0);
    }
  }

  public static String nameOfConv(AVIMConversation conv) {
    if (conv == null) {
      throw new NullPointerException("conversation is null");
    }
    if (typeOfConv(conv) == ConversationType.Single) {
      String otherId = otherIdOfConv(conv);
      UserInfo user = ChatManager.getInstance().getUserInfoFactory().getUserInfoById(otherId);
      return user.getUsername();
    } else {
      return conv.getName();
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
}
