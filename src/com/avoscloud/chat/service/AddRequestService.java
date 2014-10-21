package com.avoscloud.chat.service;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avoscloud.chat.avobject.AddRequest;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.base.C;
import com.avoscloud.chat.R;

import java.util.List;

/**
 * Created by lzw on 14-9-27.
 */
public class AddRequestService {
  public static void createAddRequest(User toUser) throws Exception {
    User fromUser = User.curUser();
    assert fromUser != null && toUser != null;
    if (!hadAddRequest(fromUser, toUser)) {
      AddRequest addRequest = new AddRequest();
      addRequest.setStatus(AddRequest.STATUS_WAIT);
      addRequest.setFromUser(fromUser);
      addRequest.setToUser(toUser);
      addRequest.save();
    } else {
      throw new Exception(App.ctx.getString(R.string.alreadyRequest));
    }
  }

  private static boolean hadAddRequest(User fromUser, User toUser) throws AVException {
    AVQuery<AddRequest> q = AVObject.getQuery(AddRequest.class);
    q.whereEqualTo(AddRequest.FROM_USER, fromUser);
    q.whereEqualTo(AddRequest.TO_USER, toUser);
    q.whereEqualTo(AddRequest.STATUS, AddRequest.STATUS_WAIT);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    return q.count() > 0;
  }

  public static int countAddRequests() throws AVException {
    AVQuery<AddRequest> q = AVObject.getQuery(AddRequest.class);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    q.whereEqualTo(AddRequest.TO_USER, User.curUser());
    return q.count();
  }

  public static List<AddRequest> findAddRequests() throws AVException {
    User user = User.curUser();
    AVQuery<AddRequest> q = AVObject.getQuery(AddRequest.class);
    q.include(AddRequest.FROM_USER);
    q.whereEqualTo(AddRequest.TO_USER, user);
    q.orderByDescending(C.CREATED_AT);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
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
