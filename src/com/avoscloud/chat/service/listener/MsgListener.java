package com.avoscloud.chat.service.listener;

import com.avoscloud.chat.entity.Msg;

public interface MsgListener {
  public String getListenerId();

  public void onMessage(Msg msg);

  public void onMessageFailure(Msg msg);

  public void onMessageSent(Msg msg);


}
