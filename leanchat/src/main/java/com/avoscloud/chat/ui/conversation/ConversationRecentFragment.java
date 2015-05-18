package com.avoscloud.chat.ui.conversation;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.avos.avoscloud.AVUser;
import com.avoscloud.chat.R;
import com.avoscloud.chat.service.CacheService;
import com.avoscloud.chat.service.ConversationManager;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.ui.base_activity.BaseFragment;
import com.avoscloud.chat.ui.chat.ChatRoomActivity;
import com.avoscloud.chat.ui.view.BaseListAdapter;
import com.avoscloud.chat.ui.view.BaseListView;
import com.avoscloud.chat.util.TimeUtils;
import com.avoscloud.leanchatlib.controller.ChatManager;
import com.avoscloud.leanchatlib.controller.ConversationHelper;
import com.avoscloud.leanchatlib.controller.MessageHelper;
import com.avoscloud.leanchatlib.model.ConversationType;
import com.avoscloud.leanchatlib.model.MessageEvent;
import com.avoscloud.leanchatlib.model.Room;
import com.avoscloud.leanchatlib.view.ViewHolder;
import de.greenrobot.event.EventBus;

import java.util.Date;
import java.util.List;

/**
 * Created by lzw on 14-9-17.
 */
public class ConversationRecentFragment extends BaseFragment implements ChatManager.ConnectionListener {
  @InjectView(R.id.convList)
  BaseListView<Room> listView;
  @InjectView(R.id.im_client_state_view)
  View imClientStateView;
  private boolean hidden;
  private EventBus eventBus;
  private ConversationManager conversationManager;
  private ChatManager chatManager;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.message_fragment, container, false);
    ButterKnife.inject(this, view);
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    eventBus = EventBus.getDefault();
    conversationManager = ConversationManager.getInstance();
    chatManager = ChatManager.getInstance();
    chatManager.setConnectionListener(this);
    initView();
    onRefresh();
    onConnectionChanged(chatManager.isConnect());
  }

  private void onRefresh() {
    listView.onRefresh();
  }

  private void initView() {
    headerLayout.showTitle(R.string.conversation_messages);
    listView.init(new BaseListView.DataFactory<Room>() {
      @Override
      public List<Room> getDatasInBackground(int skip, int limit, List<Room> currentDatas) throws Exception {
        return conversationManager.findAndCacheRooms();
      }
    }, new RoomListAdapter(getActivity()));

    listView.setItemListener(new BaseListView.ItemListener<Room>() {
      @Override
      public void onItemSelected(Room item) {
        ChatRoomActivity.chatByConversation(getActivity(), item.getConversation());
      }
    });
    listView.setToastIfEmpty(false);
    listView.setPullLoadEnable(false);
  }

  @Override
  public void onHiddenChanged(boolean hidden) {
    super.onHiddenChanged(hidden);
    this.hidden = hidden;
    if (!hidden) {
      listView.refreshWithoutAnim();
    }
  }


  @Override
  public void onResume() {
    super.onResume();
    if (!hidden) {
      listView.refreshWithoutAnim();
    }
    eventBus.register(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    eventBus.unregister(this);
  }

  public void onEvent(MessageEvent event) {
    listView.refreshWithoutAnim();
  }

  @Override
  public void onConnectionChanged(boolean connect) {
    imClientStateView.setVisibility(connect ? View.GONE : View.VISIBLE);
  }

  public static class RoomListAdapter extends BaseListAdapter<Room> {

    public RoomListAdapter(Context context) {
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

      if (ConversationHelper.typeOfConversation(room.getConversation()) == ConversationType.Single) {
        AVUser user = CacheService.lookupUser(ConversationHelper.otherIdOfConversation(room.getConversation()));
        UserService.displayAvatar(user, recentAvatarView);
      } else {
        recentAvatarView.setImageBitmap(ConversationManager.getConversationIcon(room.getConversation()));
      }
      recentNameView.setText(ConversationHelper.nameOfConversation(room.getConversation()));

      int num = room.getUnreadCount();
      if (num > 0) {
        recentUnreadView.setVisibility(View.VISIBLE);
        recentUnreadView.setText(num + "");
      } else {
        recentUnreadView.setVisibility(View.GONE);
      }

      if (room.getLastMessage() != null) {
        Date date = new Date(room.getLastMessage().getTimestamp());
        recentTimeView.setText(TimeUtils.getDate(date));
        recentMsgView.setText(MessageHelper.outlineOfMsg(room.getLastMessage()));
      }
      return convertView;
    }
  }
}
