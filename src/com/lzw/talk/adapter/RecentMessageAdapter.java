package com.lzw.talk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.lzw.talk.R;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.entity.Msg;
import com.lzw.talk.entity.RecentMsg;
import com.lzw.talk.service.EmotionService;
import com.lzw.talk.ui.view.ViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

public class RecentMessageAdapter extends BaseListAdpter<RecentMsg> {

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
    ImageView iv_recent_avatar = ViewHolder.findViewById(convertView, R.id.iv_recent_avatar);
    TextView tv_recent_name = ViewHolder.findViewById(convertView, R.id.tv_recent_name);
    TextView tv_recent_msg = ViewHolder.findViewById(convertView, R.id.tv_recent_msg);
    TextView tv_recent_time = ViewHolder.findViewById(convertView, R.id.tv_recent_time);
    TextView tv_recent_unread = ViewHolder.findViewById(convertView, R.id.tv_recent_unread);

    Msg msg = item.msg;
    User user = item.toUser;
    String avatar = user.getAvatarUrl();
    if (avatar != null && !avatar.equals("")) {
      ImageLoader.getInstance().displayImage(avatar, iv_recent_avatar);
    } else {
      iv_recent_avatar.setImageResource(R.drawable.default_user_avatar);
    }

    tv_recent_name.setText(user.getUsername());
    //tv_recent_time.setText(TimeUtils.getDate);
    int num = 0;//BmobDB.create(mContext).getUnreadCount(item.getTargetid());
    if (msg.getType() == Msg.TYPE_TEXT) {
      CharSequence spannableString = EmotionService.replace(ctx, msg.getContent());
      tv_recent_msg.setText(spannableString);
    } else if (msg.getType() == Msg.TYPE_IMAGE) {
      tv_recent_msg.setText("[" + App.ctx.getString(R.string.image) + "]");
    } else if (msg.getType() == Msg.TYPE_LOCATION) {
      String all = msg.getContent();
      if (all != null && !all.equals("")) {
        String address = all.split("&")[0];
        tv_recent_msg.setText("[" + App.ctx.getString(R.string.position) + "]" + address);
      }
    } else if (msg.getType() == Msg.TYPE_AUDIO) {
      tv_recent_msg.setText("[" + App.ctx.getString(R.string.audio) + "]");
    }

    if (num > 0) {
      tv_recent_unread.setVisibility(View.VISIBLE);
      tv_recent_unread.setText(num + "");
    } else {
      tv_recent_unread.setVisibility(View.GONE);
    }
    return convertView;
  }

}
