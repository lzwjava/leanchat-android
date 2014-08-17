
package com.lzw.talk.entity;

public class ChatMsgEntity {

  private String name;

  private String date;

  private String text;
  Msg msg;

  private boolean isComMeg = true;
  private int length;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public boolean getMsgType() {
    return isComMeg;
  }

  public void setMsgType(boolean isComMsg) {
    isComMeg = isComMsg;
  }

  public boolean isText(){
    return text!=null;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public Msg getMsg() {
    return msg;
  }

  public void setMsg(Msg msg) {
    this.msg = msg;
  }
}
