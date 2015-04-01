package com.avoscloud.chat.service.chat;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.messages.AVIMAudioMessage;
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage;
import com.avos.avoscloud.im.v2.messages.AVIMLocationMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.avoscloud.chat.db.MsgsTable;
import com.avoscloud.chat.util.Logger;
import com.avoscloud.chat.util.PathUtils;
import com.avoscloud.chat.util.PhotoUtils;
import com.avoscloud.chat.util.Utils;

import java.io.File;
import java.io.IOException;

/**
 * Created by lzw on 14/11/23.
 */
public class MsgAgent {
  private AVIMConversation conv;
  private MsgsTable msgsTable;
  private IM im;
  private SendCallback sendCallback;

  public MsgAgent(AVIMConversation conv) {
    this.conv = conv;
    msgsTable = MsgsTable.getCurrentUserInstance();
    im = IM.getInstance();
  }

  public void setSendCallback(SendCallback sendCallback) {
    this.sendCallback = sendCallback;
  }

  private void sendMsg(final AVIMTypedMessage msg, final String originPath, final SendCallback callback) {
    if (!im.isConnect()) {
      Logger.d("im not connect");
    }
    conv.sendMessage(msg, AVIMConversation.RECEIPT_MESSAGE_FLAG, new AVIMConversationCallback() {
      @Override
      public void done(AVException e) {
        if (e != null) {
          e.printStackTrace();
          msg.setMessageId(Utils.uuid());
          msg.setTimestamp(System.currentTimeMillis());
        }
        msgsTable.insertMsg(msg);

        if (e == null && originPath != null) {
          File tmpFile = new File(originPath);
          File newFile = new File(PathUtils.getChatFilePath(msg.getMessageId()));
          boolean result = tmpFile.renameTo(newFile);
          if (!result) {
            throw new IllegalStateException("move file failed, can't use local cache");
          }
        }
        if (callback != null) {
          if (e != null) {
            callback.onError(e);
          } else {
            callback.onSuccess(msg);
          }
        }
      }
    });
  }

  public void resendMsg(final AVIMTypedMessage msg, final SendCallback sendCallback) {
    final String tmpId = msg.getMessageId();
    conv.sendMessage(msg, AVIMConversation.RECEIPT_MESSAGE_FLAG, new AVIMConversationCallback() {
      @Override
      public void done(AVException e) {
        if (e != null) {
          sendCallback.onError(e);
        } else {
          msgsTable.updateFailedMsg(msg, tmpId);
          sendCallback.onSuccess(msg);
        }
      }
    });
  }

  public void sendText(String content) {
    AVIMTextMessage textMsg = new AVIMTextMessage();
    textMsg.setText(content);
    sendMsg(textMsg, null, sendCallback);
  }

  public void sendImage(String imagePath) {
    final String newPath = PathUtils.getChatFilePath(Utils.uuid());
    PhotoUtils.compressImage(imagePath, newPath);
    try {
      AVIMImageMessage imageMsg = new AVIMImageMessage(newPath);
      sendMsg(imageMsg, newPath, sendCallback);
    } catch (IOException e) {
      Utils.toastException(e);
      e.printStackTrace();
    }
  }

  public void sendLocation(double latitude, double longitude, String address) {
    AVIMLocationMessage locationMsg = new AVIMLocationMessage();
    AVGeoPoint geoPoint = new AVGeoPoint(latitude, longitude);
    locationMsg.setLocation(geoPoint);
    locationMsg.setText(address);
    sendMsg(locationMsg, null, sendCallback);
  }

  public void sendAudio(String audioPath) {
    try {
      AVIMAudioMessage audioMsg = new AVIMAudioMessage(audioPath);
      sendMsg(audioMsg, audioPath, sendCallback);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static interface SendCallback {

    void onError(Exception e);

    void onSuccess(AVIMTypedMessage msg);

  }
}
