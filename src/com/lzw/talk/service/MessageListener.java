package com.lzw.talk.service;

import com.lzw.talk.entity.Msg;

public interface MessageListener {

  public void onMessage(Msg msg);
}
