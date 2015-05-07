package com.avoscloud.leanchatlib.controller;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;

import java.util.List;

public interface AVIMTypedMessagesArrayCallback {
  void done(List<AVIMTypedMessage> typedMessages, AVException e);
}