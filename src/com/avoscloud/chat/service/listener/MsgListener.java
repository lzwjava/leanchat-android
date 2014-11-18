package com.avoscloud.chat.service.listener;

public interface MsgListener {
  public boolean onMessageUpdate(String otherId);// true的话 不再传递给下一个Listener
}
