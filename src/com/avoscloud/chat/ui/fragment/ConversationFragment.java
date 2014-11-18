package com.avoscloud.chat.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.avoscloud.chat.adapter.RecentMessageAdapter;
import com.avoscloud.chat.entity.Conversation;
import com.avoscloud.chat.entity.RoomType;
import com.avoscloud.chat.service.ChatService;
import com.avoscloud.chat.service.listener.MsgListener;
import com.avoscloud.chat.service.receiver.GroupMsgReceiver;
import com.avoscloud.chat.service.receiver.MsgReceiver;
import com.avoscloud.chat.ui.activity.ChatActivity;
import com.avoscloud.chat.util.NetAsyncTask;
import com.avoscloud.chat.R;
import com.avoscloud.chat.util.Utils;

import java.util.List;

/**
 * Created by lzw on 14-9-17.
 */
public class ConversationFragment extends BaseFragment implements AdapterView.OnItemClickListener ,MsgListener{
  ListView listview;
  RecentMessageAdapter adapter;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.message_fragment, container,false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    initView();
    refresh();
  }

  private void initView() {
    headerLayout.showTitle(R.string.messages);
    listview = (ListView) getView().findViewById(R.id.convList);
    adapter = new RecentMessageAdapter(getActivity());
    listview.setAdapter(adapter);
    listview.setOnItemClickListener(this);
  }

  @Override
  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
    // TODO Auto-generated method stub
    Conversation recent = (Conversation) adapter.getItem(position);
    if (recent.msg.getRoomType()== RoomType.Single) {
      ChatActivity.goUserChat(getActivity(), recent.toUser.getObjectId());
    } else {
      ChatActivity.goGroupChat(getActivity(), recent.chatGroup.getObjectId());
    }
  }

  private boolean hidden;

  @Override
  public void onHiddenChanged(boolean hidden) {
    super.onHiddenChanged(hidden);
    this.hidden = hidden;
    if (!hidden) {
      refresh();
    }
  }

  public void refresh() {
    new GetDataTask(ctx, false).execute();
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!hidden) {
      refresh();
    }
    GroupMsgReceiver.addMsgListener(this);
    MsgReceiver.addMsgListener(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    MsgReceiver.removeMsgListener(this);
    GroupMsgReceiver.removeMsgListener(this);
  }

  @Override
  public boolean onMessageUpdate(String otherId) {
    refresh();
    return false;
  }

  class GetDataTask extends NetAsyncTask {
    List<Conversation> conversations;

    GetDataTask(Context cxt, boolean openDialog) {
      super(cxt, openDialog);
    }

    @Override
    protected void doInBack() throws Exception {
      conversations = ChatService.getConversationsAndCache();
    }

    @Override
    protected void onPost(Exception e) {
      if (e != null) {
        Utils.toast(ctx, R.string.pleaseCheckNetwork);
      } else {
        adapter.setDatas(conversations);
        adapter.notifyDataSetChanged();
      }
    }
  }
}
