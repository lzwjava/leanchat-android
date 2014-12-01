package com.avoscloud.chat.service;

import android.widget.ImageView;
import com.avos.avoscloud.*;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.base.C;
import com.avoscloud.chat.util.Logger;
import com.avoscloud.chat.util.PhotoUtil;
import com.avoscloud.chat.base.App;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-9-15.
 */
public class UserService {
  public static ImageLoader imageLoader = ImageLoader.getInstance();

  public static User findUser(String id) throws AVException {
    AVQuery<User> q = User.getQuery(User.class);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    return q.get(id);
  }

  public static List<User> findFriends(boolean useCache) throws AVException {
    User curUser = User.curUser();
    AVRelation<User> relation = curUser.getRelation(User.FRIENDS);
    relation.setTargetClass("_User");
    AVQuery<User> query = relation.getQuery(User.class);
    if (useCache) {
      query.setCachePolicy(AVQuery.CachePolicy.CACHE_ONLY);
    } else {
      query.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
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
    imageLoader.displayImage(imageUrl, avatarView, PhotoUtil.avatarImageOptions);
  }

  public static void cacheUser(List<String> uncachedIds) throws AVException {
    if (uncachedIds.size() == 0) {
      return;
    }
    findUsers(uncachedIds);
  }

  public static List<User> findUsers(List<String> userIds) throws AVException {
    if (userIds.size() <= 0) {
      return new ArrayList<User>();
    }
    AVQuery<User> q = User.getQuery(User.class);
    q.whereContainedIn(C.OBJECT_ID, userIds);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
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
    q.orderByDescending(C.UPDATED_AT);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
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
    PreferenceMap preferenceMap = PreferenceMap.getCurUserPrefDao(App.ctx);
    AVGeoPoint geoPoint = preferenceMap.getLocation();
    if (geoPoint == null) {
      Logger.i("geo point is null");
      return new ArrayList<User>();
    }
    AVQuery<User> q = AVObject.getQuery(User.class);
    User user = User.curUser();
    q.whereNotEqualTo(C.OBJECT_ID, user.getObjectId());
    q.whereNear(User.LOCATION, geoPoint);
    q.skip(skip);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    q.limit(C.PAGE_SIZE);
    List<User> users = q.find();
    App.registerBatchUserCache(users);
    return users;
  }

  public static void saveSex(User.Gender gender, SaveCallback saveCallback) {
    User user = User.curUser();
    user.setGender(gender);
    user.saveInBackground(saveCallback);
  }

  public static List<String> transformIds(List<? extends AVObject> objects) {
    List<String> ids = new ArrayList<String>();
    for (AVObject o : objects) {
      ids.add(o.getObjectId());
    }
    return ids;
  }

  public static User signUp(String name, String password) throws AVException {
    User user = new User();
    user.setUsername(name);
    user.setPassword(password);
    user.signUp();
    return user;
  }

  public static void saveAvatar(String path) throws IOException, AVException {
    User user = User.curUser();
    if (user.getLocation() == null) {
      PreferenceMap preferenceMap = new PreferenceMap(App.ctx, user.getObjectId());
      user.setLocation(preferenceMap.getLocation());
    }
    final AVFile file = AVFile.withAbsoluteLocalPath(user.getUsername(), path);
    file.save();
    user.setAvatar(file);

    user.save();
    user.fetch();
  }

  public static void cacheUserIfNone(String userId) throws AVException {
    if (App.lookupUser(userId) == null) {
      App.registerUserCache(findUser(userId));
    }
  }
}
