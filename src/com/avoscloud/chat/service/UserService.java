package com.avoscloud.chat.service;

import android.widget.ImageView;
import com.avos.avoscloud.*;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.base.C;
import com.avoscloud.chat.util.Logger;
import com.avoscloud.chat.util.PhotoUtil;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lzw on 14-9-15.
 */
public class UserService {
  public static final int ORDER_UPDATED_AT = 1;
  public static final int ORDER_DISTANCE = 0;
  public static ImageLoader imageLoader = ImageLoader.getInstance();

  public static User findUser(String id) throws AVException {
    AVQuery<User> q = User.getQuery(User.class);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ONLY);
    return q.get(id);
  }

  public static List<User> findFriends() throws AVException {
    User curUser = User.curUser();
    AVRelation<User> relation = curUser.getRelation(User.FRIENDS);
    relation.setTargetClass("_User");
    AVQuery<User> query = relation.getQuery(User.class);
    query.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    List<User> users = query.find();
    return users;
  }

  public static void displayAvatar(String imageUrl, ImageView avatarView) {
    imageLoader.displayImage(imageUrl, avatarView, PhotoUtil.avatarImageOptions);
  }

  public static List<User> searchUser(String searchName, int skip) throws AVException {
    AVQuery<User> q = User.getQuery(User.class);
    q.whereContains(User.USERNAME, searchName);
    q.limit(C.PAGE_SIZE);
    q.skip(skip);
    User user = User.curUser();
    List<String> friendIds = new ArrayList<String>(CacheService.getFriendIds());
    friendIds.add(user.getObjectId());
    q.whereNotContainedIn(C.OBJECT_ID, friendIds);
    q.orderByDescending(C.UPDATED_AT);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    List<User> users = q.find();
    CacheService.registerBatchUser(users);
    return users;
  }


  public static List<User> findNearbyPeople(int skip, int orderType) throws AVException {
    PreferenceMap preferenceMap = PreferenceMap.getCurUserPrefDao(App.ctx);
    AVGeoPoint geoPoint = preferenceMap.getLocation();
    if (geoPoint == null) {
      Logger.i("geo point is null");
      return new ArrayList<User>();
    }
    AVQuery<User> q = AVObject.getQuery(User.class);
    User user = User.curUser();
    q.whereNotEqualTo(C.OBJECT_ID, user.getObjectId());
    if (orderType == ORDER_DISTANCE) {
      q.whereNear(User.LOCATION, geoPoint);
    } else {
      q.orderByDescending(C.UPDATED_AT);
    }
    q.skip(skip);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    q.limit(C.PAGE_SIZE);
    List<User> users = q.find();
    CacheService.registerBatchUser(users);
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

  public static void cacheUserIfNone(String userId) throws AVException {
    if (CacheService.lookupUser(userId) == null) {
      CacheService.registerUserCache(findUser(userId));
    }
  }

  public static void saveAvatar(String path) throws IOException, AVException {
    User user = User.curUser();
    final AVFile file = AVFile.withAbsoluteLocalPath(user.getUsername(), path);
    file.save();
    user.setAvatar(file);

    user.save();
    user.fetch();
  }

  public static void updateUserInfo() {
    User user = User.curUser();
    if (user != null) {
      AVInstallation installation = AVInstallation.getCurrentInstallation();
      if (installation != null) {
        user.setInstallation(installation);
        user.saveInBackground();
      }
    }
  }

  public static void updateUserLocation() {
    PreferenceMap preferenceMap = PreferenceMap.getCurUserPrefDao(App.ctx);
    AVGeoPoint lastLocation = preferenceMap.getLocation();
    if (lastLocation != null) {
      final User user = User.curUser();
      final AVGeoPoint location = user.getLocation();
      if (location == null || !Utils.doubleEqual(location.getLatitude(), lastLocation.getLatitude())
          || !Utils.doubleEqual(location.getLongitude(), lastLocation.getLongitude())) {
        user.setLocation(lastLocation);
        user.saveInBackground(new SaveCallback() {
          @Override
          public void done(AVException e) {
            if (e != null) {
              e.printStackTrace();
            } else {
              Logger.v("lastLocation save " + user.getLocation());
            }
          }
        });
      }
    }
  }
}
