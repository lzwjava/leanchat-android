package com.avoscloud.chat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.avos.avoscloud.AVUser;
import com.avoscloud.chat.R;
import com.avoscloud.chat.chat.adapter.BaseListAdapter;
import com.avoscloud.chat.entity.avobject.User;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.ui.view.ViewHolder;

import java.util.List;

/**
 * Created by lzw on 14-10-11.
 */
public class GroupUsersAdapter extends BaseListAdapter<AVUser> {
  public GroupUsersAdapter(Context ctx, List<AVUser> datas) {
    super(ctx, datas);
  }

  @Override
  public View getView(int position, View conView, ViewGroup parent) {
    if (conView == null) {
      conView = View.inflate(ctx, R.layout.group_user_item, null);
    }
    AVUser user = datas.get(position);
    ImageView avatarView = ViewHolder.findViewById(conView, R.id.avatar);
    TextView nameView = ViewHolder.findViewById(conView, R.id.username);
    UserService.displayAvatar(User.getAvatarUrl(user), avatarView);
    nameView.setText(user.getUsername());
    return conView;
  }
}
