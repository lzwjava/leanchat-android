package com.avoscloud.chat.service;

import com.avos.avoscloud.AVMessage;
import com.avos.avoscloud.Group;
import com.avos.avoscloud.Session;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.db.DBMsg;
import com.avoscloud.chat.entity.Msg;
import com.avoscloud.chat.entity.MsgBuilder;
import com.avoscloud.chat.entity.RoomType;
import com.avoscloud.chat.entity.SendCallback;
import com.avoscloud.chat.util.NetAsyncTask;

import java.io.IOException;

/**
 * Created by lzw on 14/11/23.
 */
public class MsgAgent {
  RoomType roomType;
  String toId;

  public MsgAgent(RoomType roomType, String toId) {
    this.roomType = roomType;
    this.toId = toId;
  }

  public interface MsgBuilderHelper {
    void specifyType(MsgBuilder msgBuilder);
  }

  public void createAndSendMsg(MsgBuilderHelper msgBuilderHelper, final SendCallback callback) {
    final MsgBuilder builder = new MsgBuilder();
    builder.target(roomType, toId);
    msgBuilderHelper.specifyType(builder);
    final Msg msg = builder.preBuild();
    DBMsg.insertMsg(msg);
    callback.onStart(msg);
    uploadAndSendMsg(msg, callback);
  }

  public void uploadAndSendMsg(final Msg msg, final SendCallback callback) {
    new NetAsyncTask(App.ctx, false) {
      String uploadUrl;

      @Override
      protected void doInBack() throws Exception {
        uploadUrl = MsgBuilder.uploadMsg(msg);
      }

      @Override
      protected void onPost(Exception e) {
        if (e != null) {
          e.printStackTrace();
          DBMsg.updateStatus(msg.getObjectId(), Msg.Status.SendFailed);
          callback.onError(e);
        } else {
          if (uploadUrl != null) {
            DBMsg.updateContent(msg.getObjectId(), uploadUrl);
          }
          sendMsg(msg);
          callback.onSuccess(msg);
        }
      }
    }.execute();
  }

  public Msg sendMsg(Msg msg) {
    AVMessage avMsg = msg.toAVMessage();
    Session session = ChatService.getSession();
    if (roomType == RoomType.Single) {
      session.sendMessage(avMsg);
    } else {
      Group group = session.getGroup(toId);
      group.sendMessage(avMsg);
    }
    return msg;
  }

  public static void resendMsg(Msg msg, SendCallback sendCallback) {
    String toId;
    if (msg.getRoomType() == RoomType.Group) {
      String groupId = msg.getConvid();
      toId = groupId;
    } else {
      toId = msg.getToPeerId();
      msg.setRequestReceipt(true);
    }
    DBMsg.updateStatus(msg.getObjectId(), Msg.Status.SendStart);
    sendCallback.onStart(msg);
    MsgAgent msgAgent = new MsgAgent(msg.getRoomType(), toId);
    msgAgent.uploadAndSendMsg(msg, sendCallback);
  }
}
