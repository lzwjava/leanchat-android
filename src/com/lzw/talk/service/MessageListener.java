package com.lzw.talk.service;

import com.lzw.talk.entity.Msg;

public interface MessageListener {
  public void onMessage(Msg msg);

  public void onMessageFailure(Msg msg);

  public void onMessageSent(Msg msg);
}
