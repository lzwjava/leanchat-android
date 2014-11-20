package com.avoscloud.chat.adapter;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.avoscloud.chat.R;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.service.CloudService;
import com.avoscloud.chat.ui.view.ViewHolder;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.util.NetAsyncTask;
import com.avoscloud.chat.util.Utils;

import java.util.List;

public class AddFriendAdapter extends BaseListAdapter<User> {
  public AddFriendAdapter(Context context, List<User> list) {
    super(context, list);
    // TODO Auto-generated constructor stub
  }

  @Override
  public View getView(int position, View conView, ViewGroup parent) {
    // TODO Auto-generated method stub
    if (conView == null) {
      conView = inflater.inflate(R.layout.contact_add_friend_item, null);
    }
    final User contact = datas.get(position);
    TextView nameView = ViewHolder.findViewById(conView, R.id.name);
    ImageView avatarView = ViewHolder.findViewById(conView, R.id.avatar);
    Button addBtn = ViewHolder.findViewById(conView, R.id.add);
    String avatarUrl = contact.getAvatarUrl();
    UserService.displayAvatar(avatarUrl, avatarView);
    nameView.setText(contact.getUsername());
    addBtn.setText(R.string.add);
    addBtn.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        new NetAsyncTask(ctx) {
          @Override
          protected void doInBack() throws Exception {
            CloudService.tryCreateAddRequest(contact);
          }

          @Override
          protected void onPost(Exception e) {
            if (e != null) {
              Utils.toast(e.getMessage());
            } else {
              Utils.toast(R.string.sendRequestSucceed);
            }
          }
        }.execute();
      }
    });
    return conView;
  }

}
