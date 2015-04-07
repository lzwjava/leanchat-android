package com.avoscloud.chat.service;

import android.content.Context;
import com.avos.avoscloud.*;
import com.avoscloud.chat.R;
import com.avoscloud.chat.avobject.AddRequest;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.base.C;
import com.avoscloud.chat.util.SimpleNetTask;
import com.avoscloud.chat.util.Utils;

import java.util.List;

/**
 * Created by lzw on 14-9-27.
 */
public class AddRequestService {
  public static int countAddRequests() throws AVException {
    AVQuery<AddRequest> q = AVObject.getQuery(AddRequest.class);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    q.whereEqualTo(AddRequest.TO_USER, AVUser.getCurrentUser());
    try {
      return q.count();
    } catch (AVException e) {
      if (e.getCode() == AVException.CACHE_MISS) {
        return 0;
      } else {
        throw e;
      }
    }
  }

  public static List<AddRequest> findAddRequests() throws AVException {
    AVUser user = AVUser.getCurrentUser();
    AVQuery<AddRequest> q = AVObject.getQuery(AddRequest.class);
    q.include(AddRequest.FROM_USER);
    q.whereEqualTo(AddRequest.TO_USER, user);
    q.orderByDescending(C.CREATED_AT);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    return q.find();
  }

  public static boolean hasAddRequest() throws AVException {
    PreferenceMap preferenceMap = PreferenceMap.getMyPrefDao(App.ctx);
    int addRequestN = preferenceMap.getAddRequestN();
    int requestN = countAddRequests();
    if (requestN > addRequestN) {
      return true;
    } else {
      return false;
    }
  }

  public static void agreeAddRequest(final AddRequest addRequest, final SaveCallback saveCallback) {
    UserService.addFriend(addRequest.getFromUser().getObjectId(), new SaveCallback() {
      @Override
      public void done(AVException e) {
        if (e != null) {
          if (e.getCode() == AVException.DUPLICATE_VALUE) {
            addRequest.setStatus(AddRequest.STATUS_DONE);
            addRequest.saveInBackground(saveCallback);
          } else {
            saveCallback.done(e);
          }
        } else {
          addRequest.setStatus(AddRequest.STATUS_DONE);
          addRequest.saveInBackground(saveCallback);
        }
      }
    });
  }

  public static void createAddRequest(AVUser toUser) throws Exception {
    AVUser curUser = AVUser.getCurrentUser();
    AVQuery<AddRequest> q = AVObject.getQuery(AddRequest.class);
    q.whereEqualTo(AddRequest.FROM_USER, curUser);
    q.whereEqualTo(AddRequest.TO_USER, toUser);
    q.whereEqualTo(AddRequest.STATUS, AddRequest.STATUS_WAIT);
    int count = 0;
    try {
      count = q.count();
    } catch (AVException e) {
      e.printStackTrace();
      if (e.getCode() == AVException.OBJECT_NOT_FOUND) {
        count = 0;
      } else {
        throw e;
      }
    }
    if (count > 0) {
      throw new Exception(App.ctx.getString(R.string.alreadyCreateAddRequest));
    } else {
      AddRequest add = new AddRequest();
      add.setFromUser(curUser);
      add.setToUser(toUser);
      add.setStatus(AddRequest.STATUS_WAIT);
      add.save();
    }
  }

  public static void createAddRequestInBackground(Context ctx, final AVUser user) {
    new SimpleNetTask(ctx) {
      @Override
      protected void doInBack() throws Exception {
        AddRequestService.createAddRequest(user);
      }

      @Override
      protected void onSucceed() {
        Utils.toast(R.string.sendRequestSucceed);
      }
    }.execute();
  }
}
