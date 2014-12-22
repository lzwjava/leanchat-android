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
    new NetAsyncTask(App.ctx, false) {
      String uploadUrl;

      @Override
      protected void doInBack() throws Exception {
        uploadUrl = builder.uploadMsg(msg);
      }

      @Override
      protected void onPost(Exception e) {
        if (e != null) {
          e.printStackTrace();
          callback.onError(e);
          DBMsg.updateStatus(msg.getObjectId(), Msg.Status.SendFailed);
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

}
