package com.avoscloud.leanchatlib.controller;

import com.avos.avoscloud.im.v2.AVIMReservedMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.messages.AVIMLocationMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.avoscloud.leanchatlib.R;
import com.avoscloud.leanchatlib.model.UserInfo;
import com.avoscloud.leanchatlib.utils.PathUtils;

import java.util.List;

/**
 * Created by lzw on 15/2/13.
 */
public class MessageHelper {
  public static String getFilePath(AVIMTypedMessage msg) {
    return PathUtils.getChatFilePath(msg.getMessageId());
  }

  public static boolean fromMe(AVIMTypedMessage msg) {
    ChatManager chatManager = ChatManager.getInstance();
    String selfId = chatManager.getSelfId();
    return msg.getFrom().equals(selfId);
  }

  private static String bracket(String s) {
    return String.format("[%s]", s);
  }

  public static CharSequence outlineOfMsg(AVIMTypedMessage msg) {
    AVIMReservedMessageType type = AVIMReservedMessageType.getAVIMReservedMessageType(msg.getMessageType());
    switch (type) {
      case TextMessageType:
        return EmotionHelper.replace(ChatManager.getContext(), ((AVIMTextMessage) msg).getText());
      case ImageMessageType:
        return bracket(ChatManager.getContext().getString(R.string.chat_image));
      case LocationMessageType:
        AVIMLocationMessage locMsg = (AVIMLocationMessage) msg;
        String address = locMsg.getText();
        if (address == null) {
          address = "";
        }
        return bracket(ChatManager.getContext().getString(R.string.chat_position)) + address;
      case AudioMessageType:
        return bracket(ChatManager.getContext().getString(R.string.chat_audio));
    }
    return null;
  }

  public static String nameByUserIds(List<String> userIds) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < userIds.size(); i++) {
      String id = userIds.get(i);
      if (i != 0) {
        sb.append(",");
      }
      sb.append(nameByUserId(id));
    }
    return sb.toString();
  }

  public static String nameByUserId(String id) {
    UserInfo user = ChatManager.getInstance().getChatManagerAdapter().getUserInfoById(id);
    if (user != null) {
      return user.getUsername();
    } else {
      return id;
    }
  }
}
