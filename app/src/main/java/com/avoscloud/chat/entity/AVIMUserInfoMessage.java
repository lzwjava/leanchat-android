package com.avoscloud.chat.entity;

import com.avos.avoscloud.im.v2.AVIMMessageField;
import com.avos.avoscloud.im.v2.AVIMMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;

import java.util.Map;

/**
 * Created by lzw on 15/4/14.
 */
@AVIMMessageType(type = 1)
public class AVIMUserInfoMessage extends AVIMTypedMessage {

  @AVIMMessageField(name = "_lcattrs")
  Map<String, Object> attrs;

  public Map<String, Object> getAttrs() {
    return attrs;
  }

  public void setAttrs(Map<String, Object> attrs) {
    this.attrs = attrs;
  }
}

