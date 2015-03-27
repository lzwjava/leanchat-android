package com.avoscloud.chat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.avos.avoscloud.AVUser;
import com.avoscloud.chat.R;
import com.avoscloud.chat.entity.ConvType;
import com.avoscloud.chat.entity.Room;
import com.avoscloud.chat.service.CacheService;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.service.chat.ConvManager;
import com.avoscloud.chat.service.chat.MsgUtils;
import com.avoscloud.chat.ui.view.ViewHolder;
import com.avoscloud.chat.util.TimeUtils;

import java.util.Date;

public class RoomAdapter extends BaseListAdapter<Room> {

  public RoomAdapter(Context context) {
    super(context);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // TODO Auto-generated method stub
    final Room room = datas.get(position);
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.conversation_item, parent, false);
    }
    ImageView recentAvatarView = ViewHolder.findViewById(convertView, R.id.iv_recent_avatar);
    TextView recentNameView = ViewHolder.findViewById(convertView, R.id.recent_time_text);
    TextView recentMsgView = ViewHolder.findViewById(convertView, R.id.recent_msg_text);
    TextView recentTimeView = ViewHolder.findViewById(convertView, R.id.recent_teim_text);
    TextView recentUnreadView = ViewHolder.findViewById(convertView, R.id.recent_unread);

    if (ConvManager.typeOfConv(room.getConv()) == ConvType.Single) {
      AVUser user = CacheService.lookupUser(ConvManager.otherIdOfConv(room.getConv()));
      UserService.displayAvatar(user, recentAvatarView);
    } else {
      recentAvatarView.setImageResource(R.drawable.group_icon);
    }

    recentNameView.setText(ConvManager.nameOfConv(room.getConv()));

    int num = room.getUnreadCount();
    if (num > 0) {
      recentUnreadView.setVisibility(View.VISIBLE);
      recentUnreadView.setText(num + "");
    } else {
      recentUnreadView.setVisibility(View.GONE);
    }

    if (room.getLastMsg() != null) {
      Date date = new Date(room.getLastMsg().getTimestamp());
      recentTimeView.setText(TimeUtils.getDate(date));
      recentMsgView.setText(MsgUtils.outlineOfMsg(room.getLastMsg()));
    }
    return convertView;
  }
}
