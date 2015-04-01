package com.avoscloud.chat.ui.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avoscloud.chat.R;
import com.avoscloud.chat.adapter.GroupUsersAdapter;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.db.RoomsTable;
import com.avoscloud.chat.entity.ConvType;
import com.avoscloud.chat.service.chat.ConvManager;
import com.avoscloud.chat.ui.view.ExpandGridView;
import com.avoscloud.chat.util.SimpleNetTask;
import com.avoscloud.chat.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lzw on 14-10-11.
 */
public class ConvDetailActivity extends ConvBaseActivity implements AdapterView.OnItemClickListener,
    AdapterView.OnItemLongClickListener {
  private static final int ADD_MEMBERS = 0;
  private static final int INTENT_NAME = 0;
  private static List<AVUser> members = new ArrayList<AVUser>();
  @InjectView(R.id.usersGrid)
  ExpandGridView usersGrid;

  @InjectView(R.id.name_layout)
  View nameLayout;

  @InjectView(R.id.quit_layout)
  View quitLayout;

  private ConvType convType;
  private ConvManager convManager;
  private GroupUsersAdapter usersAdapter;
  private boolean isOwner;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.group_detail_activity);
    ButterKnife.inject(this);
    initData();
    initGrid();
    initActionBar(R.string.conv_detail_title);
    setViewByConvType(convType);
    refresh();
  }

  private void setViewByConvType(ConvType convType) {
    if (convType == ConvType.Single) {
      nameLayout.setVisibility(View.GONE);
      quitLayout.setVisibility(View.GONE);
    } else {
      nameLayout.setVisibility(View.VISIBLE);
      quitLayout.setVisibility(View.VISIBLE);
    }
  }

  @Override
  protected void onConvChanged(AVIMConversation conv) {
    refresh();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuItem invite = menu.add(0, ADD_MEMBERS, 0, R.string.invite);
    alwaysShowMenuItem(invite);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    int menuId = item.getItemId();
    if (menuId == ADD_MEMBERS) {
      Utils.goActivity(ctx, ConvAddMembersActivity.class);
    }
    return super.onMenuItemSelected(featureId, item);
  }

  private void refresh() {
    new SimpleNetTask(ctx) {
      List<AVUser> subMembers = new ArrayList<AVUser>();

      @Override
      protected void doInBack() throws Exception {
        subMembers = convManager.findGroupMembers(conv());
      }

      @Override
      protected void onSucceed() {
        usersAdapter.clear();
        usersAdapter.addAll(subMembers);
      }
    }.execute();
  }

  private void initGrid() {
    usersAdapter = new GroupUsersAdapter(ctx, members);
    usersGrid.setAdapter(usersAdapter);
    usersGrid.setOnItemClickListener(this);
    usersGrid.setOnItemLongClickListener(this);
  }

  private void initData() {
    convManager = ConvManager.getInstance();
    isOwner = conv().getCreator().equals(AVUser.getCurrentUser().getObjectId());
    convType = ConvManager.typeOfConv(conv());
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    AVUser user = (AVUser) parent.getAdapter().getItem(position);
    PersonInfoActivity.goPersonInfo(ctx, user.getObjectId());
  }

  @Override
  public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
    if (convType == ConvType.Single) {
      return true;
    }
    final AVUser user = (AVUser) parent.getAdapter().getItem(position);
    boolean isTheOwner = conv().getCreator().equals(user.getObjectId());
    if (!isTheOwner) {
      new AlertDialog.Builder(ctx).setMessage(R.string.kickTips)
          .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              final ProgressDialog progress = Utils.showSpinnerDialog(ConvDetailActivity.this);
              conv().kickMembers(Arrays.asList(user.getObjectId()), new AVIMConversationCallback() {
                @Override
                public void done(AVException e) {
                  progress.dismiss();
                  if (Utils.filterException(e)) {
                    Utils.toast(R.string.kickSucceed);
                  }
                }
              });
            }
          }).setNegativeButton(R.string.cancel, null).show();
    }
    return true;
  }

  @OnClick(R.id.name_layout)
  void changeName() {
    UpdateContentActivity.goActivityForResult(this, App.ctx.getString(R.string.groupName), INTENT_NAME);
  }

  @OnClick(R.id.quit_layout)
  void quit() {
    final String convid = conv().getConversationId();
    conv().quit(new AVIMConversationCallback() {
      @Override
      public void done(AVException e) {
        if (Utils.filterException(e)) {
          RoomsTable roomsTable = RoomsTable.getCurrentUserInstance();
          roomsTable.deleteRoom(convid);
          Utils.toast(R.string.alreadyQuitConv);
          ConvDetailActivity.this.finish();
          if (ChatActivity.instance != null) {
            ChatActivity.instance.finish();
          }
        }
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      if (requestCode == INTENT_NAME) {
        String newName = UpdateContentActivity.getResultValue(data);
        convManager.updateName(conv(), newName, new AVIMConversationCallback() {
          @Override
          public void done(AVException e) {
            if (Utils.filterException(e)) {
            }
          }
        });
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }
}
