package com.avoscloud.chat.avobject;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-10-8.
 */
@AVClassName("AVOSRealtimeGroups")
public class ChatGroup extends AVObject {
  public static final String M = "m";
  public static final String NAME = "name";
  public static final String OWNER = "owner";
  //String name;
  //User owner;

  public List<String> getMembers() {
    List<String> list = getList(M);
    if (list == null) {
      list = new ArrayList<String>();
    }
    return list;
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

  public User getOwner() {
    return getAVUser(OWNER, User.class);
  }

  public void setOwner(User owner) {
    put(OWNER, owner);
  }

  public String getTitle() {
    List<String> members = getMembers();
    int len = members.size();
    return getName() + " (" + len + ")";
  }
}
