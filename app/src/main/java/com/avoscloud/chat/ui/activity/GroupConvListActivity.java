package com.avoscloud.chat.ui.activity;

import android.os.Bundle;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avoscloud.chat.R;
import com.avoscloud.chat.adapter.GroupAdapter;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.service.CacheService;
import com.avoscloud.chat.service.chat.ConvManager;
import com.avoscloud.chat.service.event.ConvChangeEvent;
import com.avoscloud.chat.service.event.FinishEvent;
import com.avoscloud.chat.ui.view.BaseListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lzw on 14-10-7.
 */
public class GroupConvListActivity extends ConvEventBaseActivity {
  @InjectView(R.id.groupList)
  BaseListView<AVIMConversation> groupListView;

  private List<AVIMConversation> convs = new ArrayList<AVIMConversation>();
  private GroupAdapter groupAdapter;
  private ConvManager convManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.group_list_activity);
    ButterKnife.inject(this);
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
}
