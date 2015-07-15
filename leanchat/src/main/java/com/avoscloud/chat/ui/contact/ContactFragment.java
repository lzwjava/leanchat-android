package com.avoscloud.chat.ui.contact;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.avoscloud.chat.R;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.entity.SortUser;
import com.avoscloud.chat.service.AddRequestManager;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.service.event.ContactRefreshEvent;
import com.avoscloud.chat.ui.base_activity.BaseFragment;
import com.avoscloud.chat.ui.chat.ChatRoomActivity;
import com.avoscloud.chat.ui.conversation.ConversationGroupListActivity;
import com.avoscloud.chat.ui.view.BaseListView;
import com.avoscloud.chat.ui.view.EnLetterView;
import com.avoscloud.chat.util.CharacterParser;
import com.avoscloud.chat.util.NetAsyncTask;
import com.avoscloud.chat.util.Utils;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 联系人列表
 */
public class ContactFragment extends BaseFragment {
  private static CharacterParser characterParser;
  private static PinyinComparator pinyinComparator;
  @InjectView(R.id.dialog)
  TextView dialogTextView;
  @InjectView(R.id.list_friends)
  BaseListView<SortUser> friendsList;
  @InjectView(R.id.right_letter)
  EnLetterView rightLetter;
  View listHeaderView;
  private ContactFragmentAdapter userAdapter;
  private ListHeaderViewHolder listHeaderViewHolder = new ListHeaderViewHolder();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    View view = inflater.inflate(R.layout.contact_fragment, container, false);
    ButterKnife.inject(this, view);

    listHeaderView = inflater.inflate(R.layout.contact_fragment_header_layout, null, false);
    ButterKnife.inject(listHeaderViewHolder, listHeaderView);
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onActivityCreated(savedInstanceState);
    characterParser = CharacterParser.getInstance();
    pinyinComparator = new PinyinComparator();

    initHeader();
    initListView();
    initRightLetterViewAndSearchEdit();
    refresh();
    EventBus.getDefault().register(this);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
  }

  private void initRightLetterViewAndSearchEdit() {
    rightLetter.setTextView(dialogTextView);
    rightLetter.setOnTouchingLetterChangedListener(new LetterListViewListener());
  }

  private void initHeader() {
    headerLayout.showTitle(App.ctx.getString(R.string.contact));
    headerLayout.showRightImageButton(R.drawable.base_action_bar_add_bg_selector, new OnClickListener() {
      @Override
      public void onClick(View v) {
        Utils.goActivity(ctx, ContactAddFriendActivity.class);
      }
    });
  }

  private List<SortUser> convertAVUser(List<AVUser> datas) {
    List<SortUser> sortUsers = new ArrayList<SortUser>();
    int total = datas.size();
    for (int i = 0; i < total; i++) {
      AVUser avUser = datas.get(i);
      SortUser sortUser = new SortUser();
      sortUser.setInnerUser(avUser);
      String username = avUser.getUsername();
      if (username != null) {
        String pinyin = characterParser.getSelling(username);
        String sortString = pinyin.substring(0, 1).toUpperCase();
        if (sortString.matches("[A-Z]")) {
          sortUser.setSortLetters(sortString.toUpperCase());
        } else {
          sortUser.setSortLetters("#");
        }
      } else {
        sortUser.setSortLetters("#");
      }
      sortUsers.add(sortUser);
    }
    Collections.sort(sortUsers, pinyinComparator);
    return sortUsers;
  }

  private void initListView() {
    userAdapter = new ContactFragmentAdapter(getActivity());
    friendsList.init(new BaseListView.DataFactory<SortUser>() {
      @Override
      public List<SortUser> getDatasInBackground(int skip, int limit, List<SortUser> currentDatas) throws Exception {
        return convertAVUser(UserService.findFriends());
      }
    }, userAdapter);

    friendsList.addHeaderView(listHeaderView, null, false);
    friendsList.setPullLoadEnable(false);
    friendsList.setOnTouchListener(new OnTouchListener() {

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (getActivity().getWindow().getAttributes().softInputMode !=
            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
          InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
          View currentFocus = getActivity().getCurrentFocus();
          if (currentFocus != null) {
            manager.hideSoftInputFromWindow(currentFocus.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
          }
        }
        return false;
      }
    });
    friendsList.setItemListener(new BaseListView.ItemListener<SortUser>() {
      @Override
      public void onItemSelected(SortUser item) {
        ChatRoomActivity.chatByUserId(getActivity(), item.getInnerUser().getObjectId());
      }

      @Override
      public void onItemLongPressed(SortUser item) {
        showDeleteDialog(item);
      }
    });
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    // TODO Auto-generated method stub
    if (isVisibleToUser) {
      //refreshMsgsFromDB();
    }
    super.setUserVisibleHint(isVisibleToUser);
  }

  void findAddRequest() {
    new NetAsyncTask(ctx, false) {
      boolean haveAddRequest;

      @Override
      protected void doInBack() throws Exception {
        haveAddRequest = AddRequestManager.getInstance().hasAddRequest();
      }

      @Override
      protected void onPost(Exception e) {
        if (filterException(e)) {
          listHeaderViewHolder.getMsgTipsView().setVisibility(haveAddRequest ? View.VISIBLE : View.GONE);
        }
      }
    }.execute();
  }

  private void refresh() {
    friendsList.onRefresh();
    findAddRequest();
  }

  public void showDeleteDialog(final SortUser user) {
    new AlertDialog.Builder(ctx).setMessage(R.string.contact_deleteContact)
        .setPositiveButton(R.string.common_sure, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            final ProgressDialog dialog1 = showSpinnerDialog();
            UserService.removeFriend(user.getInnerUser().getObjectId(), new SaveCallback() {
              @Override
              public void done(AVException e) {
                dialog1.dismiss();
                if (filterException(e)) {
                  forceRefresh();
                }
              }
            });
          }
        }).setNegativeButton(R.string.chat_common_cancel, null).show();
  }

  class ListHeaderViewHolder {
    @InjectView(R.id.iv_msg_tips)
    ImageView msgTipsView;

    @OnClick(R.id.layout_new)
    void goNewFriend() {
      Utils.goActivity(ctx, ContactNewFriendActivity.class);
    }

    @OnClick(R.id.layout_group)
    void goGroupConvList() {
      Utils.goActivity(ctx, ConversationGroupListActivity.class);
    }

    public ImageView getMsgTipsView() {
      return msgTipsView;
    }
  }

  private class LetterListViewListener implements
      EnLetterView.OnTouchingLetterChangedListener {

    @Override
    public void onTouchingLetterChanged(String s) {
      int position = userAdapter.getPositionForSection(s.charAt(0));
      if (position != -1) {
        friendsList.setSelection(position);
      }
    }
  }

  public static class PinyinComparator implements Comparator<SortUser> {
    public int compare(SortUser o1, SortUser o2) {
      if (o1.getSortLetters().equals("@")
          || o2.getSortLetters().equals("#")) {
        return -1;
      } else if (o1.getSortLetters().equals("#")
          || o2.getSortLetters().equals("@")) {
        return 1;
      } else {
        return o1.getSortLetters().compareTo(o2.getSortLetters());
      }
    }
  }

  public void forceRefresh() {
    AVQuery<AVUser> q = UserService.getFriendQuery();
    q.clearCachedResult();
    friendsList.onRefresh();
  }

  public void onEvent(ContactRefreshEvent event) {
    forceRefresh();
  }
}
