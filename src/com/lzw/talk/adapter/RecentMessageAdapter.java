package com.lzw.talk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.lzw.talk.R;
import com.lzw.talk.avobject.ChatGroup;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.entity.Msg;
import com.lzw.talk.entity.RecentMsg;
import com.lzw.talk.service.EmotionService;
import com.lzw.talk.ui.view.ViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

public class RecentMessageAdapter extends BaseListAdapter<RecentMsg> {

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
    final RecentMsg item = datas.get(position);
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.conversation_item, parent, false);
    }
    ImageView recentAvatarView = ViewHolder.findViewById(convertView, R.id.iv_recent_avatar);
    TextView recentNameView = ViewHolder.findViewById(convertView, R.id.recent_time_text);
    TextView recentMsgView = ViewHolder.findViewById(convertView, R.id.recent_msg_text);
    TextView recentTimeView = ViewHolder.findViewById(convertView, R.id.recent_teim_text);
    TextView recentUnreadView = ViewHolder.findViewById(convertView, R.id.recent_unread);

    Msg msg = item.msg;
    if (msg.isSingleChat()) {
      User user = item.toUser;
      String avatar = user.getAvatarUrl();
      if (avatar != null && !avatar.equals("")) {
        ImageLoader.getInstance().displayImage(avatar, recentAvatarView);
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
    if (msg.getType() == Msg.TYPE_TEXT) {
      CharSequence spannableString = EmotionService.replace(ctx, msg.getContent());
      recentMsgView.setText(spannableString);
    } else if (msg.getType() == Msg.TYPE_IMAGE) {
      recentMsgView.setText("[" + App.ctx.getString(R.string.image) + "]");
    } else if (msg.getType() == Msg.TYPE_LOCATION) {
      String all = msg.getContent();
      if (all != null && !all.equals("")) {
        String address = all.split("&")[0];
        recentMsgView.setText("[" + App.ctx.getString(R.string.position) + "]" + address);
      }
    } else if (msg.getType() == Msg.TYPE_AUDIO) {
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
