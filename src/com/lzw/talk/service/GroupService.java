package com.lzw.talk.service;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.lzw.talk.entity.Group;

import java.util.List;

/**
 * Created by lzw on 14-10-8.
 */
public class GroupService {
  public static List<Group> findGroups() throws AVException {
    AVQuery<Group> q = AVObject.getQuery(Group.class);
    return q.find();
  }
}
