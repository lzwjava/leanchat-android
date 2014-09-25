package com.lzw.talk.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.lzw.talk.R;
import com.lzw.talk.adapter.UserAdapter;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.C;
import com.lzw.talk.receiver.MsgReceiver;
import com.lzw.talk.service.ChatService;
import com.lzw.talk.service.StatusListener;
import com.lzw.talk.util.NetAsyncTask;
import com.lzw.talk.ui.activity.ChatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-9-17.
 */
public class ContactFragment extends BaseFragment implements StatusListener, AdapterView.OnItemClickListener {
  ListView usersList;
  private UserAdapter userAdapter;
  List<User> users;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.contact_fragment, null);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    headerLayout.showTitle(R.string.contact);
    findView();
    setList();
    new GetDataTask(ctx).execute();
    MsgReceiver.registerStatusListener(this);
  }

  private void findView() {
    usersList = (ListView) ctx.findViewById(R.id.contactList);
  }

  private void setList() {
    userAdapter = new UserAdapter(ctx);
    usersList.setAdapter(userAdapter);
    usersList.setOnItemClickListener(this);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Intent intent = new Intent(ctx, ChatActivity.class);
    User user = (User) parent.getItemAtPosition(position);
    intent.putExtra(ChatActivity.CHAT_USER_ID, user.getObjectId());
    startActivity(intent);
  }

  @Override
  public void onStatusOnline(List<String> peerIds) {
    if (users == null) {
      return;
    }
    for (User user : users) {
      if (peerIds.contains(ChatService.getPeerId(user))) {
        user.put(C.ONLINE, true);
      } else {
        user.put(C.ONLINE, false);
      }
    }
    userAdapter.notifyDataSetChanged();
  }

  class GetDataTask extends NetAsyncTask {

    protected GetDataTask(Context cxt) {
      super(cxt);
    }

    @Override
    protected void doInBack() throws Exception {
      users = ChatService.findChatUsers();
    }

    @Override
    protected void onPost(boolean res) {
      if (res) {
        userAdapter.setUsers(users);
        ChatService.withUsersToWatch(users, true);
        onStatusOnline(new ArrayList<String>(MsgReceiver.onlines));
      }
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    MsgReceiver.unregisterSatutsListener();
  }
}
