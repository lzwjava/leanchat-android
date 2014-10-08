package com.lzw.talk.service;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.lzw.talk.avobject.ChatGroup;
import com.lzw.talk.avobject.User;

import java.util.List;

/**
 * Created by lzw on 14-10-8.
 */
public class GroupService {
  public static List<ChatGroup> findGroups() throws AVException {
    User user = User.curUser();
    AVQuery<ChatGroup> q = AVObject.getQuery(ChatGroup.class);
    q.whereEqualTo(ChatGroup.M, user.getObjectId());
    return q.find();
  }
}
