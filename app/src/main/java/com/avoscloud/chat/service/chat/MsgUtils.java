package com.avoscloud.chat.service.chat;

import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMReservedMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.messages.AVIMLocationMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.avoscloud.chat.R;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.service.CacheService;
import com.avoscloud.chat.util.EmotionUtils;
import com.avoscloud.chat.util.PathUtils;

import java.util.List;

/**
 * Created by lzw on 15/2/13.
 */
public class MsgUtils {
  public static final String ADDRESS = "address";

  public static String getFilePath(AVIMTypedMessage msg) {
    return PathUtils.getChatFilePath(msg.getMessageId());
  }

  public static boolean fromMe(AVIMTypedMessage msg) {
    IM im = IM.getInstance();
    String selfId = im.getSelfId();
    return msg.getFrom().equals(selfId);
  }

  static String bracket(String s) {
    return String.format("[%s]", s);
  }

  public static CharSequence outlineOfMsg(AVIMTypedMessage msg) {
    AVIMReservedMessageType type = AVIMReservedMessageType.getAVIMReservedMessageType(msg.getMessageType());
    switch (type) {
      case TextMessageType:
        return EmotionUtils.replace(App.ctx, ((AVIMTextMessage) msg).getText());
      case ImageMessageType:
        return bracket(App.ctx.getString(R.string.image));
      case LocationMessageType:
        AVIMLocationMessage locMsg = (AVIMLocationMessage) msg;
        String address = locMsg.getText();
        if (address == null) {
          address = "";
        }
        return bracket(App.ctx.getString(R.string.position)) + address;
      case AudioMessageType:
        return bracket(App.ctx.getString(R.string.audio));
    }
    return null;
  }


  public static String getStatusDesc() {
/*    if (status == Status.SendStart) {
      return App.ctx.getString(R.string.sending);
    } else if (status == Status.SendReceived) {
      return App.ctx.getString(R.string.received);
    } else if (status == Status.SendSucceed) {
      return App.ctx.getString(R.string.sent);
    } else if (status == Status.SendFailed) {
      return App.ctx.getString(R.string.failed);
    } else if (status == Status.HaveRead) {
      return App.ctx.getString(R.string.haveRead);
    } else {
      throw new IllegalArgumentException("unknown status");
    }*/
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
    AVUser user = CacheService.lookupUser(id);
    if (user != null) {
      return user.getUsername();
    } else {
      return id;
    }
  }
}
