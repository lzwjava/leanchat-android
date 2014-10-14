package com.lzw.talk.service;

import android.widget.ImageView;
import com.avos.avoscloud.*;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.base.C;
import com.lzw.talk.util.Logger;
import com.lzw.talk.util.PhotoUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collection;
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
    AVQuery<User> q = User.getQuery(User.class);
    return q.get(id);
  }

  public static AVQuery<AVUser> getUserQuery(String username) {
    AVQuery<AVUser> q = AVObject.getQuery(AVUser.class);
    q.whereEqualTo(User.USERNAME, username);
    q.setLimit(1);
    return q;
  }

  public static List<User> findFriends(boolean useCache) throws AVException {
    User curUser = User.curUser();
    AVRelation<User> relation = curUser.getRelation(User.FRIENDS);
    AVQuery<User> query = relation.getQuery(User.class);
    if (useCache) {
      query.setCachePolicy(AVQuery.CachePolicy.CACHE_ONLY);
    }
    List<User> users = query.find();
    App.registerBatchUserCache(users);
    App app = App.getInstance();
    app.setFriends(users);
    return users;
  }

  public static List<User> findFriends() throws AVException {
    return findFriends(false);
  }

  public static void displayAvatar(String imageUrl, ImageView avatarView) {
    ImageLoader imageLoader = ImageLoader.getInstance();
    imageLoader.displayImage(imageUrl, avatarView, PhotoUtil.getAvatarImageOptions());
  }

  public static void cacheUser(List<String> uncachedIds) throws AVException {
    List<User> users = findUsers(uncachedIds);
  }

  public static List<User> findUsers(List<String> uncachedIds) throws AVException {
    if (uncachedIds.size() <= 0) {
      return new ArrayList<User>();
    }
    AVQuery<User> q = User.getQuery(User.class);
    q.whereContainedIn(C.OBJECT_ID, uncachedIds);
    List<User> users = q.find();
    App.registerBatchUserCache(users);
    return users;
  }

  public static void searchUser(String searchName, int skip, FindCallback<User> findCallback) {
    AVQuery<User> q = User.getQuery(User.class);
    q.whereContains(User.USERNAME, searchName);
    q.limit(C.PAGE_SIZE);
    q.skip(skip);
    User user = User.curUser();
    List<String> friendIds = getFriendIds();
    friendIds.add(user.getObjectId());
    q.whereNotContainedIn(C.OBJECT_ID, friendIds);
    q.findInBackground(findCallback);
  }

  private static List<String> getFriendIds() {
    List<User> friends = App.getInstance().getFriends();
    List<String> ids = new ArrayList<String>();
    for (User friend : friends) {
      ids.add(friend.getObjectId());
    }
    return ids;
  }

  public static List<User> findNearbyPeople(int skip) throws AVException {
    PrefDao prefDao = PrefDao.getCurUserPrefDao(App.ctx);
    AVGeoPoint geoPoint = prefDao.getLocation();
    if (geoPoint == null) {
      Logger.i("geo point is null");
      return new ArrayList<User>();
    }
    AVQuery<User> q = AVObject.getQuery(User.class);
    User user = User.curUser();
    q.whereNotEqualTo(C.OBJECT_ID, user.getObjectId());
    q.whereNear(User.LOCATION, geoPoint);
    q.skip(skip);
    q.limit(C.PAGE_SIZE);
    return q.find();
  }

  public static void saveSex(boolean isMale, SaveCallback saveCallback) {
    User user = User.curUser();
    user.setSex(isMale);
    user.saveInBackground(saveCallback);
  }

  public static List<String> transformIds(List<? extends AVObject> objects) {
    List<String> ids = new ArrayList<String>();
    for (AVObject o : objects) {
      ids.add(o.getObjectId());
    }
    return ids;
  }
}
