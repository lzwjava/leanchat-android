package com.avoscloud.leanchatlib.controller;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avoscloud.leanchatlib.model.ConversationType;
import com.avoscloud.leanchatlib.model.UserInfo;
import com.avoscloud.leanchatlib.utils.LogUtils;

import java.util.List;

/**
 * Created by lzw on 15/4/26.
 */
public class ConversationHelper {

  public static boolean isValidConversation(AVIMConversation conversation) {
    if (conversation == null) {
      return false;
    }
    if (conversation.getMembers() == null || conversation.getMembers().size() == 0) {
      return false;
    }
    Object type = conversation.getAttribute(ConversationType.TYPE_KEY);
    if (type == null) {
      return false;
    }

    int typeInt = (Integer) type;
    if (typeInt == ConversationType.Single.getValue()) {
      if (conversation.getMembers().size() != 2 ||
          conversation.getMembers().contains(ChatManager.getInstance().getSelfId()) == false) {
        return false;
      }
    } else if (typeInt == ConversationType.Group.getValue()) {

    } else {
      return false;
    }
    return true;
  }

  public static ConversationType typeOfConversation(AVIMConversation conversation) {
    if (isValidConversation(conversation)) {
      Object typeObject = conversation.getAttribute(ConversationType.TYPE_KEY);
      int typeInt = (Integer) typeObject;
      return ConversationType.fromInt(typeInt);
    } else {
      LogUtils.e("invalid conversation ");
      // 因为 Group 不需要取 otherId，检查没那么严格，避免导致崩溃
      return ConversationType.Group;
    }
  }

  /**
   * 获取单聊对话的另外一个人的 userId
   *
   * @param conversation
   * @return 如果非法对话，则为 selfId
   */
  public static String otherIdOfConversation(AVIMConversation conversation) {
    if (isValidConversation(conversation)) {
      if (typeOfConversation(conversation) == ConversationType.Single) {
        List<String> members = conversation.getMembers();
        if (members.size() == 2) {
          if (members.get(0).equals(ChatManager.getInstance().getSelfId())) {
            return members.get(1);
          } else {
            return members.get(0);
          }
        }
      }
    }
    // 尽管异常，返回可以使用的 userId
    return ChatManager.getInstance().getSelfId();
  }

  public static String nameOfConversation(AVIMConversation conversation) {
    if (isValidConversation(conversation)) {
      if (typeOfConversation(conversation) == ConversationType.Single) {
        String otherId = otherIdOfConversation(conversation);
        UserInfo user = ChatManager.getInstance().getChatManagerAdapter().getUserInfoById(otherId);
        if (user != null) {
          return user.getUsername();
        } else {
          LogUtils.e("use is null");
          return "对话";
        }
      } else {
        return conversation.getName();
      }
    } else {
      return "";
    }
  }

  public static String titleOfConversation(AVIMConversation conversation) {
    if (isValidConversation(conversation)) {
      if (typeOfConversation(conversation) == ConversationType.Single) {
        return nameOfConversation(conversation);
      } else {
        List<String> members = conversation.getMembers();
        return nameOfConversation(conversation) + " (" + members.size() + ")";
      }
    } else {
      return "";
    }
  }
}
