package com.avoscloud.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.entity.Conversation;
import com.avoscloud.chat.entity.RoomType;
import com.avoscloud.chat.util.EmotionUtils;
import com.avoscloud.chat.ui.view.ViewHolder;
import com.avoscloud.chat.R;
import com.avoscloud.chat.avobject.ChatGroup;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.entity.Msg;
import com.avoscloud.chat.util.PhotoUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

public class RecentMessageAdapter extends BaseListAdapter<Conversation> {

  private LayoutInflater inflater;
  private Context ctx;

  public RecentMessageAdapter(Context context) {
    super(context);
    this.ctx = context;
    inflater = LayoutInflater.from(context);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // TODO Auto-generated method stub
    final Conversation item = datas.get(position);
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.conversation_item, parent, false);
    }
    ImageView recentAvatarView = ViewHolder.findViewById(convertView, R.id.iv_recent_avatar);
    TextView recentNameView = ViewHolder.findViewById(convertView, R.id.recent_time_text);
    TextView recentMsgView = ViewHolder.findViewById(convertView, R.id.recent_msg_text);
    TextView recentTimeView = ViewHolder.findViewById(convertView, R.id.recent_teim_text);
    TextView recentUnreadView = ViewHolder.findViewById(convertView, R.id.recent_unread);

    Msg msg = item.msg;
    if (msg.getRoomType()== RoomType.Single) {
      User user = item.toUser;
      String avatar = user.getAvatarUrl();
      if (avatar != null && !avatar.equals("")) {
        ImageLoader.getInstance().displayImage(avatar, recentAvatarView, PhotoUtil.avatarImageOptions);
      } else {
        recentAvatarView.setImageResource(R.drawable.default_user_avatar);
      }
      recentNameView.setText(user.getUsername());
    } else {
      ChatGroup chatGroup = item.chatGroup;
      recentNameView.setText(chatGroup.getTitle());
      recentAvatarView.setImageResource(R.drawable.group_icon);
    }

    //recentTimeView.setText(TimeUtils.getDate);
    int num = 0;//unread count
    if (msg.getType() == Msg.Type.Text) {
      CharSequence spannableString = EmotionUtils.replace(ctx,msg.getContent());
      recentMsgView.setText(spannableString);
    } else if (msg.getType() == Msg.Type.Image) {
      recentMsgView.setText("[" + App.ctx.getString(R.string.image) + "]");
    } else if (msg.getType() == Msg.Type.Location) {
      String all = msg.getContent();
      if (all != null && !all.equals("")) {
        String address = all.split("&")[0];
        recentMsgView.setText("[" + App.ctx.getString(R.string.position) + "]" + address);
      }
    } else if (msg.getType() == Msg.Type.Audio) {
      recentMsgView.setText("[" + App.ctx.getString(R.string.audio) + "]");
    }

    if (num > 0) {
      recentUnreadView.setVisibility(View.VISIBLE);
      recentUnreadView.setText(num + "");
    } else {
      recentUnreadView.setVisibility(View.GONE);
    }
    return convertView;
  }
}
