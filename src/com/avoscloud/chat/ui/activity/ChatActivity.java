package com.avoscloud.chat.ui.activity;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import com.avos.avoscloud.Group;
import com.avos.avoscloud.Session;
import com.avoscloud.chat.adapter.EmotionPagerAdapter;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.db.DBHelper;
import com.avoscloud.chat.db.DBMsg;
import com.avoscloud.chat.entity.MsgBuilder;
import com.avoscloud.chat.entity.RoomType;
import com.avoscloud.chat.service.*;
import com.avoscloud.chat.service.listener.MsgListener;
import com.avoscloud.chat.service.receiver.GroupMsgReceiver;
import com.avoscloud.chat.service.receiver.MsgReceiver;
import com.avoscloud.chat.ui.view.RecordButton;
import com.avoscloud.chat.ui.view.xlist.XListView;
import com.avoscloud.chat.util.*;
import com.avoscloud.chat.R;
import com.avoscloud.chat.adapter.ChatMsgAdapter;
import com.avoscloud.chat.adapter.EmotionGridAdapter;
import com.avoscloud.chat.avobject.ChatGroup;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.entity.Msg;
import com.avoscloud.chat.ui.view.EmotionEditText;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity implements OnClickListener, MsgListener,
    XListView.IXListViewListener {
  private static final int IMAGE_REQUEST = 0;
  public static final int LOCATION_REQUEST = 1;
  private static final int TAKE_CAMERA_REQUEST = 2;
  public static final int PAGE_SIZE = 20;

  private ChatMsgAdapter adapter;
  private List<Msg> msgs = new ArrayList<Msg>();
  User curUser;
  DBHelper dbHelper;
  public static ChatActivity ctx;

  View chatTextLayout, chatAudioLayout, chatAddLayout, chatEmotionLayout;
  View turnToTextBtn, turnToAudioBtn, sendBtn, addImageBtn, showAddBtn, addLocationBtn, showEmotionBtn;
  LinearLayout chatBottomLayout;
  ViewPager emotionPager;
  private EmotionEditText contentEdit;
  private XListView xListView;
  RecordButton recordBtn;
  private String localCameraPath = PathUtils.getTmpPath();
  private View addCameraBtn;
  int msgSize;

  public static RoomType roomType;
  public static final String CHAT_USER_ID = "chatUserId";
  public static final String GROUP_ID = "groupId";
  public static final String ROOM_TYPE = "roomType";
  User chatUser;
  Group group;
  ChatGroup chatGroup;
  String audioId;
  MsgAgent msgAgent;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ctx = this;
    setContentView(R.layout.chat_layout);
    findView();
    initByIntent(getIntent());
  }

  private void initByIntent(Intent intent) {
    initData(intent);
    initActionBar();
    initEmotionPager();
    initRecordBtn();
    setEditTextChangeListener();

    initListView();
    setSoftInputMode();
    loadMsgsFromDB(true);
    ChatService.cancelNotification(ctx);
    Logger.d(EmotionUtils.emotionCodes.length + " len");
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    initByIntent(intent);
  }

  private void initListView() {
    adapter = new ChatMsgAdapter(ctx, msgs);
    adapter.setDatas(msgs);
    xListView.setAdapter(adapter);
    xListView.setPullRefreshEnable(true);
    xListView.setPullLoadEnable(false);
    xListView.setXListViewListener(this);
    xListView.setOnScrollListener(
        new PauseOnScrollListener(UserService.imageLoader, true, true));
  }

  private void initEmotionPager() {
    List<View> views = new ArrayList<View>();
    for (int i = 0; i < 2; i++) {
      views.add(getEmotionGridView(i));
    }
    EmotionPagerAdapter pagerAdapter = new EmotionPagerAdapter(views);
    emotionPager.setAdapter(pagerAdapter);
  }

  private View getEmotionGridView(int pos) {
    LayoutInflater inflater = LayoutInflater.from(ctx);
    View emotionView = inflater.inflate(R.layout.chat_emotion_gridview, null);
    GridView gridView = (GridView) emotionView.findViewById(R.id.gridview);
    final EmotionGridAdapter emotionGridAdapter = new EmotionGridAdapter(ctx);
    List<String> pageEmotions;
    if (pos == 0) {
      pageEmotions = EmotionUtils.emotionTexts1;
    } else {
      pageEmotions = EmotionUtils.emotionTexts2;
    }
    emotionGridAdapter.setDatas(pageEmotions);
    gridView.setAdapter(emotionGridAdapter);
    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String emotionText = (String) parent.getAdapter().getItem(position);
        int start = contentEdit.getSelectionStart();
        StringBuffer sb = new StringBuffer(contentEdit.getText());
        sb.replace(contentEdit.getSelectionStart(), contentEdit.getSelectionEnd(), emotionText);
        contentEdit.setText(sb.toString());
        CharSequence info = contentEdit.getText();
        if (info instanceof Spannable) {
          Spannable spannable = (Spannable) info;
          Selection.setSelection(spannable, start + emotionText.length());
        }
      }
    });
    return gridView;
  }

  public void initRecordBtn() {
    setNewRecordPath();
    recordBtn.setOnFinishedRecordListener(new RecordButton.RecordEventListener() {
      @Override
      public void onFinishedRecord(final String audioPath, int secs) {
        final String objectId = audioId;
        new SendMsgTask(ctx) {
          @Override
          Msg sendMsg() throws Exception {
            return msgAgent.createAndSendMsg(new MsgAgent.MsgBuilderHelper() {
              @Override
              public void specifyType(MsgBuilder msgBuilder) {
                msgBuilder.audio(objectId);
              }
            });
          }
        }.execute();
        setNewRecordPath();
      }

      @Override
      public void onStartRecord() {
      }
    });
  }

  public void setNewRecordPath() {
    audioId = Utils.uuid();
    String audioPath = PathUtils.getChatFilePath(audioId);
    recordBtn.setSavePath(audioPath);
  }

  public void setEditTextChangeListener() {
    contentEdit.addTextChangedListener(new SimpleTextWatcher(){

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
          sendBtn.setEnabled(true);
          showSendBtn();
        } else {
          sendBtn.setEnabled(false);
          showTurnToRecordBtn();
        }
        super.onTextChanged(s, start, before, count);
      }
    });
  }

  private void showTurnToRecordBtn() {
    sendBtn.setVisibility(View.GONE);
    turnToAudioBtn.setVisibility(View.VISIBLE);
  }

  private void showSendBtn() {
    sendBtn.setVisibility(View.VISIBLE);
    turnToAudioBtn.setVisibility(View.GONE);
  }

  void initActionBar() {
    String title;
    if (roomType == RoomType.Single) {
      title = chatUser.getUsername();
    } else {
      title = chatGroup.getTitle();
    }
    initActionBar(title);
  }


  private void findView() {
    xListView = (XListView) findViewById(R.id.listview);
    addImageBtn = findViewById(R.id.addImageBtn);

    contentEdit = (EmotionEditText) findViewById(R.id.textEdit);
    chatTextLayout = findViewById(R.id.chatTextLayout);
    chatAudioLayout = findViewById(R.id.chatRecordLayout);
    chatBottomLayout = (LinearLayout) findViewById(R.id.bottomLayout);
    turnToAudioBtn = findViewById(R.id.turnToAudioBtn);
    turnToTextBtn = findViewById(R.id.turnToTextBtn);
    recordBtn = (RecordButton) findViewById(R.id.recordBtn);
    chatTextLayout = findViewById(R.id.chatTextLayout);
    chatAddLayout = findViewById(R.id.chatAddLayout);
    addLocationBtn = findViewById(R.id.addLocationBtn);
    chatEmotionLayout = findViewById(R.id.chatEmotionLayout);
    showAddBtn = findViewById(R.id.showAddBtn);
    showEmotionBtn = findViewById(R.id.showEmotionBtn);
    sendBtn = findViewById(R.id.sendBtn);
    emotionPager = (ViewPager) findViewById(R.id.emotionPager);
    addCameraBtn = findViewById(R.id.addCameraBtn);

    sendBtn.setOnClickListener(this);
    contentEdit.setOnClickListener(this);
    addImageBtn.setOnClickListener(this);
    addLocationBtn.setOnClickListener(this);
    turnToAudioBtn.setOnClickListener(this);
    turnToTextBtn.setOnClickListener(this);
    showAddBtn.setOnClickListener(this);
    showEmotionBtn.setOnClickListener(this);
    addCameraBtn.setOnClickListener(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (roomType == RoomType.Single) {
      MsgReceiver.addMsgListener(this);
    } else {
      GroupMsgReceiver.addMsgListener(this);
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (roomType == RoomType.Single) {
      MsgReceiver.removeMsgListener(this);
    } else {
      GroupMsgReceiver.removeMsgListener(this);
    }
  }

  public void initData(Intent intent) {
    curUser = User.curUser();
    dbHelper = new DBHelper(ctx, App.DB_NAME, App.DB_VER);
    int roomTypeInt = intent.getIntExtra(ROOM_TYPE, RoomType.Single.getValue());
    roomType = RoomType.fromInt(roomTypeInt);
    msgSize = PAGE_SIZE;
    if (roomType == RoomType.Single) {
      String chatUserId = intent.getStringExtra(CHAT_USER_ID);
      chatUser = App.lookupUser(chatUserId);
      ChatService.withUserToWatch(chatUser, true);
      msgAgent = new MsgAgent(roomType, chatUser.getObjectId());
    } else {
      String groupId = intent.getStringExtra(GROUP_ID);
      Session session = ChatService.getSession();
      group = session.getGroup(groupId);
      chatGroup = App.lookupChatGroup(groupId);
      msgAgent = new MsgAgent(roomType, groupId);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.chat_ativity_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    int menuId = item.getItemId();
    if (menuId == R.id.people) {
      if (roomType == RoomType.Single) {
        PersonInfoActivity.goPersonInfo(ctx, chatUser.getObjectId());
      } else {
        GroupDetailActivity.chatGroup = chatGroup;
        Utils.goActivity(ctx, GroupDetailActivity.class);
      }
    }
    return super.onMenuItemSelected(featureId, item);
  }

  public String currentChatId() {
    if (roomType == RoomType.Single) {
      return chatUser.getObjectId();
    } else {
      return chatGroup.getObjectId();
    }
  }

  public void loadMsgsFromDB(boolean showDialog) {
    new GetDataTask(ctx, showDialog, true).execute();
  }

  @Override
  public void onRefresh() {
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        msgSize += PAGE_SIZE;
        new GetDataTask(ctx, false, false).execute();
      }
    }, 1000);
  }

  @Override
  public void onLoadMore() {
  }

  @Override
  public boolean onMessageUpdate(String otherId) {
    if (otherId.equals(currentChatId())) {
      loadMsgsFromDB(false);
      return true;
    }
    return false;
  }

  public void resendMsg(final Msg resendMsg) {
    new SendMsgTask(ctx) {
      @Override
      Msg sendMsg() throws Exception {
        return ChatService.resendMsg(resendMsg);
      }
    }.execute();
  }

  class GetDataTask extends NetAsyncTask {
    List<Msg> msgs;
    boolean scrollToLast = true;

    GetDataTask(Context cxt, boolean openDialog, boolean scrollToLast) {
      super(cxt, openDialog);
      this.scrollToLast = scrollToLast;
    }

    @Override
    protected void doInBack() throws Exception {
      String convid;
      if (roomType == RoomType.Single) {
        convid = AVOSUtils.convid(ChatService.getPeerId(curUser), ChatService.getPeerId(chatUser));
      } else {
        convid = group.getGroupId();
      }
      msgs = DBMsg.getMsgs(dbHelper, convid, msgSize);
      ChatService.cacheUserOrChatGroup(msgs);
    }

    @Override
    protected void onPost(Exception e) {
      if (e == null) {
        ChatUtils.stopRefresh(xListView);
        addMsgsAndRefresh(msgs, scrollToLast);
      } else {
        Utils.toast(R.string.failedToGetData);
      }
    }
  }

  public void addMsgsAndRefresh(List<Msg> msgs, boolean scrollToLast) {
    int lastN = adapter.getCount();
    int newN = msgs.size();
    this.msgs = msgs;
    adapter.setDatas(this.msgs);
    adapter.notifyDataSetChanged();
    if (scrollToLast) {
      scrollToLast();
    } else {
      xListView.setSelection(newN - lastN - 1);
      if (lastN == newN) {
        Utils.toast(R.string.loadMessagesFinish);
      }
    }
    if (newN < PAGE_SIZE) {
      xListView.setPullRefreshEnable(false);
    } else {
      xListView.setPullRefreshEnable(true);
    }
  }

  @Override
  public void onClick(View v) {
    // TODO Auto-generated method stub
    switch (v.getId()) {
      case R.id.sendBtn:
        sendText();
        break;
      case R.id.addImageBtn:
        selectImageFromLocal();
        break;
      case R.id.turnToAudioBtn:
        showAudioLayout();
        break;
      case R.id.turnToTextBtn:
        showTextLayout();
        break;
      case R.id.showAddBtn:
        toggleBottomAddLayout();
        break;
      case R.id.showEmotionBtn:
        toggleEmotionLayout();
        break;
      case R.id.addLocationBtn:
        selectLocationFromMap();
        break;
      case R.id.textEdit:
        hideBottomLayoutAndScrollToLast();
        break;
      case R.id.addCameraBtn:
        selectImageFromCamera();
        break;
    }
  }

  private void hideBottomLayoutAndScrollToLast() {
    hideBottomLayout();
    scrollToLast();
  }

  private void hideBottomLayout() {
    hideAddLayout();
    chatEmotionLayout.setVisibility(View.GONE);
  }

  private void selectLocationFromMap() {
    Intent intent = new Intent(this, LocationActivity.class);
    intent.putExtra("type", "select");
    startActivityForResult(intent, LOCATION_REQUEST);
  }

  private void toggleEmotionLayout() {
    if (chatEmotionLayout.getVisibility() == View.VISIBLE) {
      chatEmotionLayout.setVisibility(View.GONE);
    } else {
      chatEmotionLayout.setVisibility(View.VISIBLE);
      hideAddLayout();
      showTextLayout();
      hideSoftInputView();
    }
  }

  private void toggleBottomAddLayout() {
    if (chatAddLayout.getVisibility() == View.VISIBLE) {
      hideAddLayout();
    } else {
      chatEmotionLayout.setVisibility(View.GONE);
      hideSoftInputView();
      showAddLayout();
    }
  }

  private void hideAddLayout() {
    chatAddLayout.setVisibility(View.GONE);
  }

  private void showAddLayout() {
    chatAddLayout.setVisibility(View.VISIBLE);
  }

  private void showTextLayout() {
    chatTextLayout.setVisibility(View.VISIBLE);
    chatAudioLayout.setVisibility(View.GONE);
  }

  private void showAudioLayout() {
    chatTextLayout.setVisibility(View.GONE);
    chatAudioLayout.setVisibility(View.VISIBLE);
    chatEmotionLayout.setVisibility(View.GONE);
    hideSoftInputView();
  }

  public void selectImageFromLocal() {
    Intent intent;
    intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("image/*");
    startActivityForResult(intent, IMAGE_REQUEST);
  }

  public void selectImageFromCamera() {
    Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    Uri imageUri = Uri.fromFile(new File(localCameraPath));
    openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
    startActivityForResult(openCameraIntent,
        TAKE_CAMERA_REQUEST);
  }

  private void sendText() {
    final String content = contentEdit.getText().toString();
    if (TextUtils.isEmpty(content) == false) {
      new SendMsgTask(ctx) {
        @Override
        Msg sendMsg() throws Exception {
          return msgAgent.createAndSendMsg(new MsgAgent.MsgBuilderHelper() {
            @Override
            public void specifyType(MsgBuilder msgBuilder) {
              msgBuilder.text(content);
            }
          });
        }

        @Override
        protected void onPost(Exception e) {
          super.onPost(e);
          if (e == null) {
            contentEdit.setText("");
          }
        }
      }.execute();
    }
  }

  private String parsePathByReturnData(Intent data) {
    if (data == null) {
      return null;
    }
    String localSelectPath = null;
    Uri selectedImage = data.getData();
    if (selectedImage != null) {
      Cursor cursor = getContentResolver().query(
          selectedImage, null, null, null, null);
      cursor.moveToFirst();
      int columnIndex = cursor.getColumnIndex("_data");
      localSelectPath = cursor.getString(columnIndex);
      cursor.close();
    }
    return localSelectPath;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      switch (requestCode) {
        case IMAGE_REQUEST:
          String localSelectPath = parsePathByReturnData(data);
          sendImageByPath(localSelectPath);
          break;
        case TAKE_CAMERA_REQUEST:
          sendImageByPath(localCameraPath);
          break;
        case LOCATION_REQUEST:
          sendLocationByReturnData(data);
          break;
      }
    }
    hideBottomLayout();
    super.onActivityResult(requestCode, resultCode, data);
  }

  private void sendLocationByReturnData(Intent data) {
    final double latitude = data.getDoubleExtra("x", 0);
    final double longitude = data.getDoubleExtra("y", 0);
    final String address = data.getStringExtra("address");
    if (address != null && !address.equals("")) {
      new SendMsgTask(ctx) {
        @Override
        Msg sendMsg() throws Exception {
          return msgAgent.createAndSendMsg(new MsgAgent.MsgBuilderHelper() {
            @Override
            public void specifyType(MsgBuilder msgBuilder) {
              msgBuilder.location(address, latitude, longitude);
            }
          });
        }
      }.execute();
    } else {
      Utils.toast(ctx, R.string.cannotGetYourAddressInfo);
    }
  }

  public abstract class SendMsgTask extends NetAsyncTask {
    Msg msg;

    protected SendMsgTask(Context cxt) {
      super(cxt, false);
    }

    @Override
    protected void doInBack() throws Exception {
      if (Connectivity.isConnected(ctx) == false) {
        throw new NetworkErrorException(App.ctx.getString(R.string.pleaseCheckNetwork));
      } else if (ChatService.isSessionPaused()) {
        throw new NetworkErrorException(App.ctx.getString(R.string.sessionPausedTips));
      } else {
        msg = sendMsg();
      }
    }

    @Override
    protected void onPost(Exception e) {
      if (e != null) {
        e.printStackTrace();
        Utils.toast(e.getMessage());
      } else {
        loadMsgsFromDB(false);
      }
    }

    abstract Msg sendMsg() throws Exception;
  }

  public String getOtherId() {
    if (roomType == RoomType.Single) {
      return chatUser.getObjectId();
    } else {
      return group.getGroupId();
    }
  }

  private void sendImageByPath(String localSelectPath) {
    final String objectId = Utils.uuid();
    final String newPath = PathUtils.getChatFilePath(objectId);
    //PhotoUtil.simpleCompressImage(localSelectPath,newPath);
    PhotoUtil.compressImage(localSelectPath, newPath);
    new SendMsgTask(ctx) {
      @Override
      Msg sendMsg() throws Exception {
        return msgAgent.createAndSendMsg(new MsgAgent.MsgBuilderHelper() {
          @Override
          public void specifyType(MsgBuilder msgBuilder) {
            msgBuilder.image(objectId);
          }
        });
      }
    }.execute();
  }

  public void scrollToLast() {
    Logger.d("scrollToLast");
    //xListView.smoothScrollToPosition(xListView.getCount() - 1);
    xListView.setSelection(xListView.getCount() - 1);
  }

  @Override
  protected void onDestroy() {
    if (roomType == RoomType.Single) {
      ChatService.withUserToWatch(chatUser, false);
    }
    ctx = null;
    super.onDestroy();
  }

  public static void goUserChat(Activity ctx, String userId) {
    Intent intent = getUserChatIntent(ctx, userId);
    ctx.startActivity(intent);
  }

  public static Intent getUserChatIntent(Context ctx, String userId) {
    Intent intent = new Intent(ctx, ChatActivity.class);
    intent.putExtra(CHAT_USER_ID, userId);
    intent.putExtra(ROOM_TYPE, RoomType.Single.getValue());
    return intent;
  }

  public static void goGroupChat(Activity ctx, String groupId) {
    Intent intent = getGroupChatIntent(ctx, groupId);
    ctx.startActivity(intent);
  }

  public static Intent getGroupChatIntent(Context ctx, String groupId) {
    Intent intent = new Intent(ctx, ChatActivity.class);
    intent.putExtra(GROUP_ID, groupId);
    intent.putExtra(ROOM_TYPE, RoomType.Group.getValue());
    return intent;
  }
}
