package com.lzw.talk.service;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.lzw.talk.avobject.AddRequest;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.base.C;

import java.util.List;

/**
 * Created by lzw on 14-9-27.
 */
public class AddRequestService {
  public static void createAddRequest(User toUser) throws AVException {
    User fromUser = User.curUser();
    assert fromUser != null && toUser != null;
    AddRequest addRequest = new AddRequest();
    addRequest.setStatus(AddRequest.STATUS_WAIT);
    addRequest.setFromUser(fromUser);
    addRequest.setToUser(toUser);
    addRequest.save();
  }

  public static int countAddRequests() throws AVException {
    AVQuery<AddRequest> q = AVObject.getQuery(AddRequest.class);
    q.whereEqualTo(AddRequest.TO_USER, User.curUser());
    return q.count();
  }

  public static List<AddRequest> findAddRequests() throws AVException {
    User user = User.curUser();
    AVQuery<AddRequest> q = AVObject.getQuery(AddRequest.class);
    q.include(AddRequest.FROM_USER);
    q.whereEqualTo(AddRequest.TO_USER, user);
    q.orderByDescending(C.CREATED_AT);
    return q.find();
  }

  public static boolean hasAddRequest() throws AVException {
    PrefDao prefDao = PrefDao.getMyPrefDao(App.ctx);
    int addRequestN = prefDao.getAddRequestN();
    int requestN = countAddRequests();
    if (requestN > addRequestN) {
      return true;
    } else {
      return false;
    }
  }
}
