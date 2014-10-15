package com.avoscloud.chat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.ui.view.ViewHolder;
import com.avoscloud.chat.R;
import com.avoscloud.chat.service.UserService;

import java.util.List;

@SuppressLint("DefaultLocale")
public class UserFriendAdapter extends BaseAdapter implements SectionIndexer {
  private Context ct;
  private List<User> data;

  public UserFriendAdapter(Context ct, List<User> datas) {
    this.ct = ct;
    this.data = datas;
  }

  public void updateListView(List<User> list) {
    this.data = list;
    notifyDataSetChanged();
  }

  public void remove(User user) {
    this.data.remove(user);
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return data.size();
  }

  @Override
  public Object getItem(int position) {
    return data.get(position);
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      convertView = LayoutInflater.from(ct).inflate(
          R.layout.item_user_friend, null);
    }
    TextView alpha = ViewHolder.findViewById(convertView, R.id.alpha);
    TextView nameView = ViewHolder.findViewById(convertView, R.id.tv_friend_name);
    ImageView avatarView = ViewHolder.findViewById(convertView, R.id.img_friend_avatar);

    User friend = data.get(position);
    final String name = friend.getUsername();
    final String avatarUrl = friend.getAvatarUrl();

    UserService.displayAvatar(avatarUrl,avatarView);
    nameView.setText(name);

    int section = getSectionForPosition(position);
    if (position == getPositionForSection(section)) {
      alpha.setVisibility(View.VISIBLE);
      alpha.setText(friend.getSortLetters());
    } else {
      alpha.setVisibility(View.GONE);
    }

    return convertView;
  }

  public int getSectionForPosition(int position) {
    return data.get(position).getSortLetters().charAt(0);
  }


  @SuppressLint("DefaultLocale")
  public int getPositionForSection(int section) {
    for (int i = 0; i < getCount(); i++) {
      String sortStr = data.get(i).getSortLetters();
      char firstChar = sortStr.toUpperCase().charAt(0);
      if (firstChar == section) {
        return i;
      }
    }

    return -1;
  }

  @Override
  public Object[] getSections() {
    return null;
  }

}
