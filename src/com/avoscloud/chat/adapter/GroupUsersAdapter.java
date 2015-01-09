package com.avoscloud.chat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.avos.avoscloud.AVUser;
import com.avoscloud.chat.R;
import com.avoscloud.chat.util.ChatUtils;

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
    ChatUtils.setUserView(conView, user);
    return conView;
  }
}
