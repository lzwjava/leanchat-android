package com.avoscloud.chat.adapter;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.avos.avoscloud.AVUser;
import com.avoscloud.chat.R;
import com.avoscloud.chat.avobject.AddRequest;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.ui.view.ViewHolder;

import java.util.List;

public class NewFriendAdapter extends BaseListAdapter<AddRequest> {
  private Listener listener;

  public NewFriendAdapter(Context ctx) {
    super(ctx);
  }

  public NewFriendAdapter(Context context, List<AddRequest> list) {
    super(context, list);
  }

  public void setListener(Listener listener) {
    this.listener = listener;
  }

  @Override
  public View getView(int position, View conView, ViewGroup parent) {
    // TODO Auto-generated method stub
    if (conView == null) {
      conView = inflater.inflate(R.layout.contact_add_friend_item, null);
    }
    final AddRequest addRequest = datas.get(position);
    TextView nameView = ViewHolder.findViewById(conView, R.id.name);
    ImageView avatarView = ViewHolder.findViewById(conView, R.id.avatar);
    final Button addBtn = ViewHolder.findViewById(conView, R.id.add);
    View agreedView = ViewHolder.findViewById(conView, R.id.agreedView);

    AVUser from = addRequest.getFromUser();
    UserService.displayAvatar(from, avatarView);
    if (from != null) {
      nameView.setText(from.getUsername());
    }
    int status = addRequest.getStatus();
    if (status == AddRequest.STATUS_WAIT) {
      addBtn.setVisibility(View.VISIBLE);
      agreedView.setVisibility(View.GONE);
      addBtn.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View arg0) {
          // TODO Auto-generated method stub
          if (listener != null) {
            listener.onAgreeAddRequest(addRequest);
          }
        }
      });
    } else if (status == AddRequest.STATUS_DONE) {
      addBtn.setVisibility(View.GONE);
      agreedView.setVisibility(View.VISIBLE);
    }
    return conView;
  }

  public interface Listener {
    void onAgreeAddRequest(AddRequest addRequest);
  }
}
