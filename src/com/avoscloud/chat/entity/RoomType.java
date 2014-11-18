package com.avoscloud.chat.entity;

/**
* Created by lzw on 14/11/18.
*/
public enum RoomType {
  Single(0), Group(1);

  int value;
  RoomType(int value){
    this.value=value;
  }

  public int getValue(){
    return value;
  }

  public static RoomType fromInt(int i){
    return values()[i];
  }
}
