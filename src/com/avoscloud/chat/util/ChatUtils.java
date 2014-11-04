package com.avoscloud.chat.util;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.avos.avoscloud.*;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.base.C;
import com.avoscloud.chat.service.PreferenceMap;
import com.avoscloud.chat.ui.view.ViewHolder;
import com.avoscloud.chat.R;
import com.avoscloud.chat.adapter.BaseListAdapter;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.ui.view.xlist.XListView;

import java.util.List;

/**
 * Created by lzw on 14-9-30.
 */

public class ChatUtils {
  public static void handleListResult(XListView listView, BaseListAdapter adapter, List datas) {
    if (Utils.isListNotEmpty(datas)) {
      adapter.addAll(datas);
      if (datas.size() == C.PAGE_SIZE) {
        listView.setPullLoadEnable(true);
      } else {
        listView.setPullLoadEnable(false);
      }
    } else {
      listView.setPullLoadEnable(false);
      if (adapter.getCount() == 0) {
        Utils.toast(R.string.noResult);
      } else {
        Utils.toast(R.string.dataLoadFinish);
      }
    }
  }

  public static void updateUserInfo() {
    User user = User.curUser();
    if (user != null) {
      user.fetchInBackground(User.FRIENDS, new GetCallback<AVObject>() {
        @Override
        public void done(AVObject avObject, AVException e) {
          if(e==null){
            User avUser = (User) avObject;
            App.registerUserCache(avUser);
          }
        }
      });
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

  public static void stopRefresh(XListView xListView) {
    if (xListView.getPullRefreshing()) {
      xListView.stopRefresh();
    }
  }

  public static void setUserView(View conView, User user) {
    ImageView avatarView = ViewHolder.findViewById(conView, R.id.avatar);
    TextView nameView = ViewHolder.findViewById(conView, R.id.username);
    UserService.displayAvatar(user.getAvatarUrl(), avatarView);
    nameView.setText(user.getUsername());
  }
}
