package com.avoscloud.chat.ui.conversation;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avoscloud.chat.R;
import com.avoscloud.chat.entity.avobject.User;
import com.avoscloud.chat.service.CacheService;
import com.avoscloud.chat.service.ConversationManager;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.service.event.FinishEvent;
import com.avoscloud.chat.ui.chat.ChatRoomActivity;
import com.avoscloud.chat.ui.view.BaseCheckListAdapter;
import com.avoscloud.chat.util.Utils;
import com.avoscloud.leanchatlib.controller.ConversationHelper;
import com.avoscloud.leanchatlib.model.ConversationType;
import com.avoscloud.leanchatlib.view.ViewHolder;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-10-11.
 */
public class ConversationAddMembersActivity extends ConversationBaseActivity {
  public static final int OK = 0;
  private CheckListAdapter adapter;
  private ListView userList;
  private ConversationManager conversationManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.conversation_add_members_layout);
    findView();
    conversationManager = ConversationManager.getInstance();
    initList();
    initActionBar();
    setListData();
  }

  @Override
  protected void onConvChanged(AVIMConversation conv) {
  }

  private void setListData() {
    UserService.findFriendsWithCachePolicy(AVQuery.CachePolicy.CACHE_ELSE_NETWORK, new FindCallback<AVUser>() {
      @Override
      public void done(List<AVUser> users, AVException e) {
        List<String> userIds = new ArrayList<String>();
        for (AVUser user : users) {
          userIds.add(user.getObjectId());
        }
        userIds.removeAll(conv().getMembers());
        adapter.setDatas(userIds);
        adapter.notifyDataSetChanged();
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuItem add = menu.add(0, OK, 0, R.string.common_sure);
    alwaysShowMenuItem(add);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    int id = item.getItemId();
    if (id == OK) {
      addMembers();
    }
    return super.onMenuItemSelected(featureId, item);
  }

  private void addMembers() {
    final List<String> checkedUsers = adapter.getCheckedDatas();
    final ProgressDialog dialog = showSpinnerDialog();
    if (checkedUsers.size() == 0) {
      finish();
    } else {
      if (ConversationHelper.typeOfConversation(conv()) == ConversationType.Single) {
        List<String> members = new ArrayList<String>();
        members.addAll(checkedUsers);
        members.addAll(conv().getMembers());
        conversationManager.createGroupConversation(members, new AVIMConversationCreatedCallback() {
          @Override
          public void done(final AVIMConversation conversation, AVException e) {
            if (filterException(e)) {
              EventBus eventBus = EventBus.getDefault();
              FinishEvent finishEvent = new FinishEvent();
              eventBus.post(finishEvent);
              ChatRoomActivity.chatByConversation(ConversationAddMembersActivity.this, conversation);
            }
          }
        });
      } else {
        conv().addMembers(checkedUsers, new AVIMConversationCallback() {
          @Override
          public void done(AVException e) {
            dialog.dismiss();
            if (filterException(e)) {
              Utils.toast(R.string.conversation_inviteSucceed);
              conversationManager.postConvChanged(conv());
              finish();
            }
          }
        });
      }
    }
  }

  private void initList() {
    adapter = new CheckListAdapter(ctx, new ArrayList<String>());
    userList.setAdapter(adapter);
  }

  private void findView() {
    userList = (ListView) findViewById(R.id.userList);
  }

  public static class CheckListAdapter extends BaseCheckListAdapter<String> {

    public CheckListAdapter(Context ctx, List<String> datas) {
      super(ctx, datas);
    }

    @Override
    public View getView(final int position, View conView, ViewGroup parent) {
      if (conView == null) {
        conView = View.inflate(ctx, R.layout.conversation_add_members_item, null);
      }
      String userId = datas.get(position);
      AVUser user = CacheService.lookupUser(userId);
      ImageView avatarView = ViewHolder.findViewById(conView, R.id.avatar);
      TextView nameView = ViewHolder.findViewById(conView, R.id.username);
      UserService.displayAvatar(user, avatarView);
      nameView.setText(user.getUsername());
      CheckBox checkBox = ViewHolder.findViewById(conView, R.id.checkbox);
      setCheckBox(checkBox, position);
      checkBox.setOnCheckedChangeListener(new CheckListener(position));
      return conView;
    }
  }
}
