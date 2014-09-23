package com.lzw.talk.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.lzw.talk.R;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.C;
import com.lzw.talk.service.UserService;
import com.lzw.talk.util.Logger;
import com.lzw.talk.view.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-8-7.
 */
public class UserAdapter extends BaseAdapter {
  Activity cxt;
  List<User> users = new ArrayList<User>();

  public UserAdapter(Activity cxt) {
    this.cxt = cxt;
  }

  public void setUsers(List<User> users) {
    this.users = users;
  }

  @Override
  public int getCount() {
    return users.size();
  }

  @Override
  public Object getItem(int position) {
    return users.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View conView, ViewGroup parent) {
    if (conView == null) {
      LayoutInflater inflater = LayoutInflater.from(cxt);
      conView = inflater.inflate(R.layout.chat_user_row, null, false);
    }
    TextView usernameView = ViewHolder.findViewById(conView, R.id.name);
    TextView onlineStatusView = ViewHolder.findViewById(conView, R.id.onlineStatus);
    ImageView avatarView = ViewHolder.findViewById(conView, R.id.userAvatar);
    User user = users.get(position);
    usernameView.setText(user.getUsername());
    setTextBasedOnFlag(user.getBoolean(C.ONLINE), onlineStatusView,
        R.string.status_online, R.string.status_offline);
    UserService.displayAvatar(user, avatarView);
    return conView;
  }

  public void setTextBasedOnFlag(boolean flag, TextView textView, int onStringId, int offStringId) {
    Logger.d("textView"+textView);
    textView.setText(flag ? onStringId : offStringId);
  }
}
