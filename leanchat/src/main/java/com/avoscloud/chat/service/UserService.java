package com.avoscloud.chat.service;

import android.widget.ImageView;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.FollowCallback;
import com.avos.avoscloud.SaveCallback;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.base.Constant;
import com.avoscloud.chat.entity.avobject.User;
import com.avoscloud.chat.util.Logger;
import com.avoscloud.chat.util.Utils;
import com.avoscloud.leanchatlib.utils.PhotoUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lzw on 14-9-15.
 */
public class UserService {
  public static final int ORDER_UPDATED_AT = 1;
  public static final int ORDER_DISTANCE = 0;

  public static AVUser findUser(String id) throws AVException {
    AVQuery<AVUser> q = AVUser.getQuery(AVUser.class);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    return q.get(id);
  }

  public static void findFriendsWithCachePolicy(AVQuery.CachePolicy cachePolicy, FindCallback<AVUser> findCallback) {
    AVUser curUser = AVUser.getCurrentUser();
    AVQuery<AVUser> q = null;
    try {
      q = curUser.followeeQuery(AVUser.class);
    } catch (Exception e) {
      throw new NullPointerException();
    }
    q.setCachePolicy(cachePolicy);
    q.include("followee");
    q.findInBackground(findCallback);
  }

  public static List<AVUser> findFriends() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final List<AVUser> friends = new ArrayList<AVUser>();
    final AVException[] es = new AVException[1];
    findFriendsWithCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE, new FindCallback<AVUser>() {
      @Override
      public void done(List<AVUser> avUsers, AVException e) {
        if (e != null) {
          es[0] = e;
        } else {
          friends.addAll(avUsers);
          CacheService.registerUsers(avUsers);
        }
        latch.countDown();
      }
    });
    latch.await();
    if (es[0] != null) {
      throw es[0];
    } else {
      return friends;
    }
  }

  public static void displayAvatar(AVUser user, ImageView avatarView) {
    if (user != null) {
      String avatarUrl = User.getAvatarUrl(user);
      if (avatarUrl != null) {
        ImageLoader.getInstance().displayImage(avatarUrl, avatarView, PhotoUtils.avatarImageOptions);
      } else {
        avatarView.setImageBitmap(ColoredBitmapProvider.getInstance().createColoredBitmapByHashString(user.getObjectId()));
      }
    }
  }

  public static List<AVUser> searchUser(String searchName, int skip) throws AVException {
    AVQuery<AVUser> q = AVUser.getQuery(AVUser.class);
    q.whereContains(User.USERNAME, searchName);
    q.limit(Constant.PAGE_SIZE);
    q.skip(skip);
    AVUser user = AVUser.getCurrentUser();
    List<String> friendIds = new ArrayList<String>(CacheService.getFriendIds());
    friendIds.add(user.getObjectId());
    q.whereNotContainedIn(Constant.OBJECT_ID, friendIds);
    q.orderByDescending(Constant.UPDATED_AT);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    List<AVUser> users = q.find();
    CacheService.registerUsers(users);
    return users;
  }


  public static List<AVUser> findNearbyPeople(int orderType, int skip, int limit) throws AVException {
    PreferenceMap preferenceMap = PreferenceMap.getCurUserPrefDao(App.ctx);
    AVGeoPoint geoPoint = preferenceMap.getLocation();
    if (geoPoint == null) {
      Logger.i("geo point is null");
      return new ArrayList<>();
    }
    AVQuery<AVUser> q = AVObject.getQuery(AVUser.class);
    AVUser user = AVUser.getCurrentUser();
    q.whereNotEqualTo(Constant.OBJECT_ID, user.getObjectId());
    if (orderType == ORDER_DISTANCE) {
      q.whereNear(User.LOCATION, geoPoint);
    } else {
      q.orderByDescending(Constant.UPDATED_AT);
    }
    q.skip(skip);
    q.limit(limit);
    q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    List<AVUser> users = q.find();
    CacheService.registerUsers(users);
    return users;
  }

  public static AVUser signUp(String name, String password) throws AVException {
    AVUser user = new AVUser();
    user.setUsername(name);
    user.setPassword(password);
    user.signUp();
    return user;
  }

  public static void saveAvatar(String path) throws IOException, AVException {
    AVUser user = AVUser.getCurrentUser();
    final AVFile file = AVFile.withAbsoluteLocalPath(user.getUsername(), path);
    file.save();
    user.put(User.AVATAR, file);

    user.save();
    user.fetch();
  }

  public static void updateUserInfo() {
    AVUser user = AVUser.getCurrentUser();
    if (user != null) {
      AVInstallation installation = AVInstallation.getCurrentInstallation();
      if (installation != null) {
        user.put(User.INSTALLATION, installation);
        user.saveInBackground();
      }
    }
  }

  public static void updateUserLocation() {
    PreferenceMap preferenceMap = PreferenceMap.getCurUserPrefDao(App.ctx);
    AVGeoPoint lastLocation = preferenceMap.getLocation();
    if (lastLocation != null) {
      final AVUser user = AVUser.getCurrentUser();
      final AVGeoPoint location = user.getAVGeoPoint(User.LOCATION);
      if (location == null || !Utils.doubleEqual(location.getLatitude(), lastLocation.getLatitude())
          || !Utils.doubleEqual(location.getLongitude(), lastLocation.getLongitude())) {
        user.put(User.LOCATION, lastLocation);
        user.saveInBackground(new SaveCallback() {
          @Override
          public void done(AVException e) {
            if (e != null) {
              e.printStackTrace();
            } else {
              Logger.v("lastLocation save " + user.getAVGeoPoint(User.LOCATION));
            }
          }
        });
      }
    }
  }

  public static void addFriend(String friendId, final SaveCallback saveCallback) {
    AVUser user = AVUser.getCurrentUser();
    user.followInBackground(friendId, new FollowCallback() {
      @Override
      public void done(AVObject object, AVException e) {
        saveCallback.done(e);
      }
    });
  }

  public static void removeFriend(String friendId, final SaveCallback saveCallback) {
    AVUser user = AVUser.getCurrentUser();
    user.unfollowInBackground(friendId, new FollowCallback() {
      @Override
      public void done(AVObject object, AVException e) {
        saveCallback.done(e);
      }
    });
  }
}
