package com.lzw.talk.entity;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

import java.util.List;

/**
 * Created by lzw on 14-10-8.
 */
@AVClassName("AVOSRealtimeGroups")
public class Group extends AVObject {
  public static final String M = "m";
  public static final String NAME = "name";
  //String name;

  public List<String> getMembers() {
    return getList(M);
  }

  public void setMembers(List<String> members) {
    put(M, members);
  }

  public String getName() {
    return getString(NAME);
  }

  public void setName(String name) {
    put(NAME, name);
  }
}
