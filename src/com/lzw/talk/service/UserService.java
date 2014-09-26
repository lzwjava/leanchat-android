package com.lzw.talk.service;

import android.text.TextUtils;
import android.widget.ImageView;
import com.avos.avoscloud.*;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.base.C;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by lzw on 14-9-15.
 */
public class UserService {
  public static AVUser getAVUser(String username) throws AVException {
    AVQuery<AVUser> q = getUserQuery(username);
    List<AVUser> users = q.find();
    if (users != null && users.isEmpty() == false) {
      return users.get(0);
    }
    return null;
  }

  public static User findUser(String id) throws AVException {
    AVQuery<User> q=User.getQuery(User.class);
    return q.get(id);
  }

  public static AVQuery<AVUser> getUserQuery(String username) {
    AVQuery<AVUser> q = AVObject.getQuery(AVUser.class);
    q.whereEqualTo(User.USERNAME, username);
    q.setLimit(1);
    return q;
  }

  public static void updateNickname(User curUser, String value, SaveCallback saveCallback) {
    if (TextUtils.isEmpty(value) == false) {
      curUser.setNickname(value);
      curUser.saveInBackground(saveCallback);
    }
  }

  public static List<User> findFriends() throws AVException {
    User curUser = User.curUser();
    AVRelation<User> relation = curUser.getRelation(User.FRIENDS);
    return relation.getQuery().find();
  }

  public static void displayAvatar(User user, ImageView avatarView) {
    ImageLoader imageLoader = ImageLoader.getInstance();
    if (user.getAvatar() != null) {
      imageLoader.displayImage(user.getAvatarUrl(), avatarView);
    } else {
      avatarView.setImageResource(com.lzw.talk.R.drawable.default_user_avatar);
    }
  }

  public static void cacheUser(List<String> uncachedId) throws AVException {
    AVQuery<User> q = User.getQuery(User.class);
    q.whereContainedIn(C.OBJECT_ID, uncachedId);
    List<User> users = q.find();
    App.registerBatchUserCache(users);
  }
}
