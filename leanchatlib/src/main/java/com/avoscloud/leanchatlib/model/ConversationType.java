package com.avoscloud.leanchatlib.model;

/**
 * 对话类型。因为两个人对话可能是单聊，也可能是群聊。所以需要额外的类型区分
 * Created by lzw on 14/11/18.
 */
public enum ConversationType {
  Single(0), Group(1);
  /**
   * 创建的时候直接设置 type 字段
   */
  public static final String TYPE_KEY = "type";
  /**
   * 查找对话的时候，要加前缀 attr. 其实type保存在conversation的attr中
   * 登录网站后台，_Conversation 表可看到
   */
  public static final String ATTR_TYPE_KEY = "attr.type";

  int value;

  ConversationType(int value) {
    this.value = value;
  }

  public static ConversationType fromInt(int i) {
    if (i < 2) {
      return values()[i];
    } else {
      return Group;
    }
  }

  public int getValue() {
    return value;
  }
}
