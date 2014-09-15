package com.lzw.talk.entity;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVMessage;
import com.avos.avoscloud.AVUtils;

import java.util.HashMap;
import java.util.List;

/**
 * Created by lzw on 14-8-7.
 */
public class Msg {
  public static final int STATUS_SEND_START = 0;
  public static final int STATUS_SEND_RECEIVED = 1;
  //long timestamp;
  //String fromPeerId;
  //List<String> toPeerIds;
  String content;
  String objectId;
  AVMessage internalMessage;
  int status;

  public Msg() {
    internalMessage = new AVMessage();
  }

  public AVMessage getInternalMessage() {
    return internalMessage;
  }

  public void setInternalMessage(AVMessage internalMessage) {
    this.internalMessage = internalMessage;
  }

  public List<String> getToPeerIds() {
    return internalMessage.getToPeerIds();
  }

  public void setToPeerIds(List<String> toPeerIds) {
    internalMessage.setToPeerIds(toPeerIds);
  }

  public String getFromPeerId() {
    return internalMessage.getFromPeerId();
  }

  public void setFromPeerId(String fromPeerId) {
    internalMessage.setFromPeerId(fromPeerId);
  }

  public long getTimestamp() {
    return internalMessage.getTimestamp();
  }

  public void setTimestamp(long timestamp) {
    internalMessage.setTimestamp(timestamp);
  }

  public String getContent() {
    return content;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public static Msg fromAVMessage(AVMessage avMsg) {
    Msg msg = new Msg();
    msg.setInternalMessage(avMsg);
    if (!AVUtils.isBlankString(avMsg.getMessage())) {
      HashMap<String, Object> params = JSON.parseObject(avMsg.getMessage(), HashMap.class);
      msg.setObjectId((String) params.get("objectId"));
      msg.setContent((String) params.get("content"));
      msg.setStatus((Integer) params.get("status"));
    }
    return msg;
  }

  public AVMessage toAVMessage() {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("objectId", objectId);
    params.put("content", content);
    params.put("status", status);
    internalMessage.setMessage(JSON.toJSONString(params));
    return internalMessage;
  }

  @Override
  public String toString() {
    return "{content:" + getContent() + " objectId:" + getObjectId() + " status:" + getStatus() + " fromPeerId:" +
        getFromPeerId() +
        " toPeerIds:" + getToPeerIds() + " timestamp:" + getTimestamp()+"}";
  }
}
