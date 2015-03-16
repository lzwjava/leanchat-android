package com.avoscloud.chat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationQueryCallback;
import com.avoscloud.chat.R;
import com.avoscloud.chat.adapter.GroupAdapter;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.service.CacheService;
import com.avoscloud.chat.service.event.ConvChangeEvent;
import com.avoscloud.chat.service.chat.ConvManager;
import com.avoscloud.chat.service.chat.IM;
import com.avoscloud.chat.service.event.FinishEvent;
import com.avoscloud.chat.ui.view.BaseListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lzw on 14-10-7.
 */
public class GroupConvListActivity extends ConvEventBaseActivity {
  private static final int GROUP_NAME_REQUEST = 0;
  @InjectView(R.id.groupList)
  BaseListView<AVIMConversation> groupListView;

  private List<AVIMConversation> convs = new ArrayList<AVIMConversation>();
  private GroupAdapter groupAdapter;
  private String groupName;
  private IM im;
  private ConvManager convManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.group_list_activity);
    ButterKnife.inject(this);
    im = IM.getInstance();
    convManager = ConvManager.getInstance();
    initList();
    initActionBar(App.ctx.getString(R.string.group));
    groupListView.onRefresh();
  }

  @Override
  public void onEvent(ConvChangeEvent convChangeEvent) {
    groupListView.refreshWithoutAnim();
  }

  @Override
  public void onEvent(FinishEvent finishEvent) {

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater menuInflater = getMenuInflater();
    menuInflater.inflate(R.menu.group_list_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.create) {
      UpdateContentActivity.goActivityForResult(this, App.ctx.getString(R.string.groupName), GROUP_NAME_REQUEST);
    }
    return super.onMenuItemSelected(featureId, item);
  }

  private void initList() {
    groupAdapter = new GroupAdapter(ctx, convs);
    groupListView.init(new BaseListView.DataFactory<AVIMConversation>() {
      AVException exception;
      List<AVIMConversation> convs;

      @Override
      public List<AVIMConversation> getDatas(int skip, int limit, List<AVIMConversation> currentDatas) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        convManager.findGroupConvsIncludeMe(new AVIMConversationQueryCallback() {
          @Override
          public void done(List<AVIMConversation> conversations, AVException e) {
            convs = conversations;
            exception = e;
            latch.countDown();
          }
        });
        latch.await();
        if (exception != null) {
          throw exception;
        }
        CacheService.registerConvs(convs);
        return convs;
      }
    }, groupAdapter);
    groupListView.setPullLoadEnable(false);
    groupListView.setItemListener(new BaseListView.ItemListener<AVIMConversation>() {
      @Override
      public void onItemSelected(AVIMConversation item) {
        ChatActivity.goByConv(GroupConvListActivity.this, item);
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      if (requestCode == GROUP_NAME_REQUEST) {
        groupName = UpdateContentActivity.getResultValue(data);
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }
}
