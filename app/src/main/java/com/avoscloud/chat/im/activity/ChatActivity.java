package com.avoscloud.chat.im.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage;
import com.avos.avoscloud.im.v2.messages.AVIMLocationMessage;
import com.avoscloud.chat.R;
import com.avoscloud.chat.im.adapter.ChatEmotionGridAdapter;
import com.avoscloud.chat.im.adapter.ChatEmotionPagerAdapter;
import com.avoscloud.chat.im.adapter.ChatMessageAdapter;
import com.avoscloud.chat.im.controller.*;
import com.avoscloud.chat.im.db.MsgsTable;
import com.avoscloud.chat.im.db.RoomsTable;
import com.avoscloud.chat.im.model.ConversationType;
import com.avoscloud.chat.im.model.MessageEvent;
import com.avoscloud.chat.im.view.EmotionEditText;
import com.avoscloud.chat.im.view.RecordButton;
import com.avoscloud.chat.im.view.xlist.XListView;
import com.avoscloud.chat.service.CacheService;
import com.avoscloud.chat.util.*;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import de.greenrobot.event.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity implements OnClickListener,
    XListView.IXListViewListener {
  public static final String CONVID = "convid";

  private static final int LOCATION_REQUEST = 1;
  private static final int PAGE_SIZE = 20;
  private static final int TAKE_CAMERA_REQUEST = 2;
  private static final int GALLERY_REQUEST = 0;
  private static final int GALLERY_KITKAT_REQUEST = 3;

  private static ChatActivity chatInstance;
  //用来判断是否弹出通知
  private static String currentChattingConvid;
  private ChatMessageAdapter adapter;
  private RoomsTable roomsTable;

  private View chatTextLayout, chatAudioLayout, chatAddLayout, chatEmotionLayout;
  private View turnToTextBtn, turnToAudioBtn, sendBtn, addImageBtn, showAddBtn, addLocationBtn, showEmotionBtn;
  private ViewPager emotionPager;
  private EmotionEditText contentEdit;
  private XListView xListView;
  private RecordButton recordBtn;
  private String localCameraPath = PathUtils.getTmpPath();
  private View addCameraBtn;
  protected ConversationType conversationType;
  protected AVIMConversation conversation;
  protected MsgsTable msgsTable;
  protected MessageAgent messageAgent;
  protected MessageAgent.SendCallback defaultSendCallback = new DefaultSendCallback();
  protected EventBus eventBus;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.chat_layout);
    commonInit();
    findView();
    initEmotionPager();
    initRecordBtn();
    setEditTextChangeListener();
    initListView();
    setSoftInputMode();
    initByIntent(getIntent());
  }

  public static ChatActivity getChatInstance() {
    return chatInstance;
  }


  public static String getCurrentChattingConvid() {
    return currentChattingConvid;
  }

  public static void setCurrentChattingConvid(String currentChattingConvid) {
    ChatActivity.currentChattingConvid = currentChattingConvid;
  }


  private void findView() {
    xListView = (XListView) findViewById(R.id.listview);
    addImageBtn = findViewById(R.id.addImageBtn);

    contentEdit = (EmotionEditText) findViewById(R.id.textEdit);
    chatTextLayout = findViewById(R.id.chatTextLayout);
    chatAudioLayout = findViewById(R.id.chatRecordLayout);
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

  private void initByIntent(Intent intent) {
    initData(intent);
    refreshMsgsFromDB();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    initByIntent(intent);
  }

  private void initListView() {
    xListView.setPullRefreshEnable(true);
    xListView.setPullLoadEnable(false);
    xListView.setXListViewListener(this);
    xListView.setOnScrollListener(
        new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
  }

  private void initEmotionPager() {
    List<View> views = new ArrayList<View>();
    for (int i = 0; i < EmotionUtils.emojiGroups.size(); i++) {
      views.add(getEmotionGridView(i));
    }
    ChatEmotionPagerAdapter pagerAdapter = new ChatEmotionPagerAdapter(views);
    emotionPager.setOffscreenPageLimit(3);
    emotionPager.setAdapter(pagerAdapter);
  }

  private View getEmotionGridView(int pos) {
    LayoutInflater inflater = LayoutInflater.from(ctx);
    View emotionView = inflater.inflate(R.layout.chat_emotion_gridview, null, false);
    GridView gridView = (GridView) emotionView.findViewById(R.id.gridview);
    final ChatEmotionGridAdapter chatEmotionGridAdapter = new ChatEmotionGridAdapter(ctx);
    List<String> pageEmotions = EmotionUtils.emojiGroups.get(pos);
    chatEmotionGridAdapter.setDatas(pageEmotions);
    gridView.setAdapter(chatEmotionGridAdapter);
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
    recordBtn.setSavePath(PathUtils.getRecordTmpPath());
    recordBtn.setRecordEventListener(new RecordButton.RecordEventListener() {
      @Override
      public void onFinishedRecord(final String audioPath, int secs) {
        messageAgent.sendAudio(audioPath);
      }

      @Override
      public void onStartRecord() {

      }
    });
  }

  public void setEditTextChangeListener() {
    contentEdit.addTextChangedListener(new SimpleTextWatcher() {

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

  void commonInit() {
    ctx = this;
    chatInstance = this;
    msgsTable = MsgsTable.getCurrentUserInstance();
    roomsTable = RoomsTable.getCurrentUserInstance();
    eventBus = EventBus.getDefault();
    eventBus.register(this);
  }

  public void initData(Intent intent) {
    String convid = intent.getStringExtra(CONVID);
    conversation = CacheService.lookupConv(convid);
    if (conversation == null) {
      throw new NullPointerException("conv is null");
    }
    initActionBar(ConversationManager.titleOfConv(conversation));
    messageAgent = new MessageAgent(conversation);
    messageAgent.setSendCallback(defaultSendCallback);
    CacheService.setCurConv(conversation);
    roomsTable.insertRoom(convid);
    roomsTable.clearUnread(conversation.getConversationId());
    conversationType = ConversationManager.typeOfConv(conversation);
    bindAdapterToListView(conversationType);
  }

  private void bindAdapterToListView(ConversationType conversationType) {
    adapter = new ChatMessageAdapter(this, conversationType);
    adapter.setClickListener(new ChatMessageAdapter.ClickListener() {
      @Override
      public void onFailButtonClick(AVIMTypedMessage msg) {
        messageAgent.resendMsg(msg, defaultSendCallback);
      }

      @Override
      public void onLocationViewClick(AVIMLocationMessage locMsg) {
        Intent intent = new Intent(ctx, LocationActivity.class);
        intent.putExtra(LocationActivity.TYPE, LocationActivity.TYPE_SCAN);
        intent.putExtra(LocationActivity.LATITUDE, locMsg.getLocation().getLatitude());
        intent.putExtra(LocationActivity.LONGITUDE, locMsg.getLocation().getLongitude());
        ctx.startActivity(intent);
      }

      @Override
      public void onImageViewClick(AVIMImageMessage imageMsg) {
        ImageBrowserActivity.go(ChatActivity.this,
            MessageUtils.getFilePath(imageMsg),
            imageMsg.getFileUrl());
      }
    });
    xListView.setAdapter(adapter);
  }

  public void refreshMsgsFromDB() {
    new GetDataTask(ctx, false).execute();
  }

  @Override
  public void onRefresh() {
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        new GetDataTask(ctx, true).execute();
      }
    }, 1000);
  }

  @Override
  public void onLoadMore() {
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
    intent.putExtra(LocationActivity.TYPE, LocationActivity.TYPE_SELECT);
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
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.chat_activity_select_picture)),
          GALLERY_REQUEST);
    } else {
      Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("image/*");
      startActivityForResult(intent, GALLERY_KITKAT_REQUEST);
    }
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
    if (!TextUtils.isEmpty(content)) {
      messageAgent.sendText(content);
      contentEdit.setText("");
    }
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      switch (requestCode) {
        case GALLERY_REQUEST:
        case GALLERY_KITKAT_REQUEST:
          if (data == null) {
            Utils.toast("return data is null");
            return;
          }
          Uri uri;
          if (requestCode == GALLERY_REQUEST) {
            uri = data.getData();
          } else {
            //for Android 4.4
            uri = data.getData();
            final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
          }
          String localSelectPath = ProviderPathUtils.getPath(ctx, uri);
          messageAgent.sendImage(localSelectPath);
          break;
        case TAKE_CAMERA_REQUEST:
          messageAgent.sendImage(localCameraPath);
          break;
        case LOCATION_REQUEST:
          final double latitude = data.getDoubleExtra(LocationActivity.LATITUDE, 0);
          final double longitude = data.getDoubleExtra(LocationActivity.LONGITUDE, 0);
          final String address = data.getStringExtra(LocationActivity.ADDRESS);
          if (!TextUtils.isEmpty(address)) {
            messageAgent.sendLocation(latitude, longitude, address);
          } else {
            Utils.toast(ctx, R.string.chat_cannotGetYourAddressInfo);
          }
          break;
      }
    }
    hideBottomLayout();
    super.onActivityResult(requestCode, resultCode, data);
  }

  public void scrollToLast() {
    xListView.post(new Runnable() {
      @Override
      public void run() {
        //fast scroll
        /*xListView.requestFocusFromTouch();
        xListView.setSelection(xListView.getAdapter().getCount() - 1);
        contentEdit.requestFocusFromTouch();*/
        xListView.smoothScrollToPosition(xListView.getAdapter().getCount() - 1);
      }
    });
  }

  @Override
  protected void onDestroy() {
    CacheService.setCurConv(null);
    chatInstance = null;
    eventBus.unregister(this);
    super.onDestroy();
  }

  public void onEvent(MessageEvent messageEvent) {
    AVIMTypedMessage msg = messageEvent.getMsg();
    if (msg.getConversationId().equals(conversation.getConversationId())) {
      roomsTable.clearUnread(conversation.getConversationId());
      refreshMsgsFromDB();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (conversation == null) {
      throw new IllegalStateException("conv is null");
    }
    setCurrentChattingConvid(conversation.getConversationId());
    ChatManager.getInstance().cancelNotification();
  }

  @Override
  protected void onPause() {
    super.onPause();
    setCurrentChattingConvid(null);
  }

  class GetDataTask extends NetAsyncTask {
    private List<AVIMTypedMessage> msgs;
    private boolean loadHistory;

    GetDataTask(Context ctx, boolean loadHistory) {
      super(ctx, false);
      this.loadHistory = loadHistory;
    }

    @Override
    protected void doInBack() throws Exception {
      String msgId = null;
      long maxTime = System.currentTimeMillis() + 10 * 1000;
      int limit;
      long time;
      if (loadHistory == false) {
        time = maxTime;
        int count = adapter.getCount();
        if (count > PAGE_SIZE) {
          limit = count;
        } else {
          limit = PAGE_SIZE;
        }
      } else {
        if (adapter.getDatas().size() > 0) {
          msgId = adapter.getDatas().get(0).getMessageId();
          AVIMTypedMessage firstMsg = adapter.getDatas().get(0);
          time = firstMsg.getTimestamp();
        } else {
          time = maxTime;
        }
        limit = PAGE_SIZE;
      }
      msgs = msgsTable.selectMsgs(conversation.getConversationId(), time, limit);
      //msgs = ConvManager.getInstance().queryHistoryMessage(conv, msgId, time, limit);
      CacheService.cacheMsgs(msgs);
    }

    @Override
    protected void onPost(Exception e) {
      if (filterException(e)) {
        if (xListView.getPullRefreshing()) {
          xListView.stopRefresh();
        }
        if (loadHistory == false) {
          adapter.setDatas(msgs);
          adapter.notifyDataSetChanged();
          scrollToLast();
        } else {
          List<AVIMTypedMessage> newMsgs = new ArrayList<AVIMTypedMessage>();
          newMsgs.addAll(msgs);
          newMsgs.addAll(adapter.getDatas());
          adapter.setDatas(newMsgs);
          adapter.notifyDataSetChanged();
          if (msgs.size() > 0) {
            xListView.setSelection(msgs.size() - 1);
          } else {
            Utils.toast(R.string.chat_activity_loadMessagesFinish);
          }
        }
      }
    }
  }

  class DefaultSendCallback implements MessageAgent.SendCallback {

    @Override
    public void onError(Exception e) {
      refreshMsgsFromDB();
    }

    @Override
    public void onSuccess(AVIMTypedMessage msg) {
      refreshMsgsFromDB();
    }
  }
}
