package com.lzw.talk.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.lzw.talk.R;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.service.AddRequestService;
import com.lzw.talk.ui.view.ViewHolder;
import com.lzw.talk.util.NetAsyncTask;
import com.lzw.talk.util.PhotoUtil;
import com.lzw.talk.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

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
      conView = inflater.inflate(R.layout.item_add_friend, null);
    }
    final User contact = datas.get(position);
    TextView nameView = ViewHolder.findViewById(conView, R.id.name);
    ImageView avatarView = ViewHolder.findViewById(conView, R.id.avatar);
    Button addBtn = ViewHolder.findViewById(conView, R.id.add);
    String avatarUrl = contact.getAvatarUrl();
    if (!TextUtils.isEmpty(avatarUrl)) {
      ImageLoader.getInstance().displayImage(avatarUrl, avatarView, PhotoUtil.getImageLoaderOptions());
    } else {
      avatarView.setImageResource(R.drawable.default_avatar);
    }

    nameView.setText(contact.getUsername());
    addBtn.setText(R.string.add);
    addBtn.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        runAddFriendTask(ctx, contact);
      }
    });
    return conView;
  }

  public static void runAddFriendTask(Context ctx, final User friend) {
    new NetAsyncTask(ctx) {
      @Override
      protected void doInBack() throws Exception {
        AddRequestService.createAddRequest(friend);
      }

      @Override
      protected void onPost(Exception e) {
        if (e != null) {
          Utils.toast(App.ctx.getString(R.string.sendRequestFailed) + e.getMessage());
        } else {
          Utils.toast(R.string.sendRequestSucceed);
        }
      }
    }.execute();
  }
}
