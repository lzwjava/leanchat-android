package com.lzw.talk.service;

import com.lzw.talk.entity.Msg;

public interface MsgListener {
  public String getListenerId();

  public void onMessage(Msg msg);

  public void onMessageFailure(Msg msg);

  public void onMessageSent(Msg msg);


}
