package com.avoscloud.chat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.ui.view.ViewHolder;
import com.avoscloud.chat.util.ChatUtils;
import com.avoscloud.chat.R;

import java.util.List;

/**
 * Created by lzw on 14-10-11.
 */
public class GroupAddMembersAdapter extends BaseCheckListAdapter<User> {

  public GroupAddMembersAdapter(Context ctx, List<User> datas) {
    super(ctx, datas);
  }

  @Override
  public View getView(final int position, View conView, ViewGroup parent) {
    if (conView == null) {
      conView = View.inflate(ctx, R.layout.group_add_members_item, null);
    }
    User user = datas.get(position);
    ChatUtils.setUserView(conView, user);
    CheckBox checkBox = ViewHolder.findViewById(conView, R.id.checkbox);
    setCheckBox(checkBox, position);
    checkBox.setOnCheckedChangeListener(new CheckListener(position));
    return conView;
  }
}
