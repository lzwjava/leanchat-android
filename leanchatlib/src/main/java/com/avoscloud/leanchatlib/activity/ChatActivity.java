package com.avoscloud.leanchatlib.activity;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMReservedMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.messages.AVIMAudioMessage;
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage;
import com.avos.avoscloud.im.v2.messages.AVIMLocationMessage;
import com.avoscloud.leanchatlib.R;
import com.avoscloud.leanchatlib.adapter.ChatEmotionGridAdapter;
import com.avoscloud.leanchatlib.adapter.ChatEmotionPagerAdapter;
import com.avoscloud.leanchatlib.adapter.ChatMessageAdapter;
import com.avoscloud.leanchatlib.controller.AVIMTypedMessagesArrayCallback;
import com.avoscloud.leanchatlib.controller.ChatManager;
import com.avoscloud.leanchatlib.controller.ConversationHelper;
import com.avoscloud.leanchatlib.controller.EmotionHelper;
import com.avoscloud.leanchatlib.controller.MessageAgent;
import com.avoscloud.leanchatlib.controller.MessageHelper;
import com.avoscloud.leanchatlib.controller.RoomsTable;
import com.avoscloud.leanchatlib.model.ConversationType;
import com.avoscloud.leanchatlib.model.MessageEvent;
import com.avoscloud.leanchatlib.utils.LogUtils;
import com.avoscloud.leanchatlib.utils.PathUtils;
import com.avoscloud.leanchatlib.utils.ProviderPathUtils;
import com.avoscloud.leanchatlib.utils.Utils;
import com.avoscloud.leanchatlib.view.EmotionEditText;
import com.avoscloud.leanchatlib.view.RecordButton;
import com.avoscloud.leanchatlib.view.RefreshableView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import de.greenrobot.event.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatActivity extends Activity implements OnClickListener {
  public static final String CONVID = "convid";
  private static final int PAGE_SIZE = 8;
  private static final int TAKE_CAMERA_REQUEST = 2;
  private static final int GALLERY_REQUEST = 0;
  private static final int GALLERY_KITKAT_REQUEST = 3;

  private volatile static ChatActivity chatInstance;
  protected ConversationType conversationType;
  protected AVIMConversation conversation;
  protected MessageAgent messageAgent;
  protected MessageAgent.SendCallback defaultSendCallback = new DefaultSendCallback();
  protected EventBus eventBus;
  protected ChatManager chatManager = ChatManager.getInstance();
  protected ChatMessageAdapter adapter;
  protected RoomsTable roomsTable;
  protected View chatTextLayout, chatAudioLayout, chatAddLayout, chatEmotionLayout;
  protected View turnToTextBtn, turnToAudioBtn, sendBtn, addImageBtn, showAddBtn, addLocationBtn, showEmotionBtn;
  protected ViewPager emotionPager;
  protected EmotionEditText contentEdit;
  protected RefreshableView refreshableView;
  protected ListView messageListView;
  protected RecordButton recordBtn;
  protected String localCameraPath = PathUtils.getPicturePathByCurrentTime();
  protected View addCameraBtn;

  public static ChatActivity getChatInstance() {
    return chatInstance;
  }

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.chat_layout);
    commonInit();
    findView();
    initEmotionPager();
    initRecordBtn();
    setEditTextChangeListener();
    initListView();

    initByIntent(getIntent());
  }

  private void findView() {
    refreshableView = (RefreshableView) findViewById(R.id.refreshableView);
    messageListView = (ListView) findViewById(R.id.messageListView);
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

    addLocationBtn.setVisibility(View.GONE);
  }

  private void initByIntent(Intent intent) {
    initData(intent);
    loadMessagesWhenInit(PAGE_SIZE);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    initByIntent(intent);
  }

  private void initListView() {
    refreshableView.setRefreshListener(new RefreshableView.ListRefreshListener(messageListView) {
      @Override
      public void onRefresh() {
        loadOldMessages();
      }
    });
    messageListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
  }

  private void initEmotionPager() {
    List<View> views = new ArrayList<View>();
    for (int i = 0; i < EmotionHelper.emojiGroups.size(); i++) {
      views.add(getEmotionGridView(i));
    }
    ChatEmotionPagerAdapter pagerAdapter = new ChatEmotionPagerAdapter(views);
    emotionPager.setOffscreenPageLimit(3);
    emotionPager.setAdapter(pagerAdapter);
  }

  private View getEmotionGridView(int pos) {
    LayoutInflater inflater = LayoutInflater.from(this);
    View emotionView = inflater.inflate(R.layout.chat_emotion_gridview, null, false);
    GridView gridView = (GridView) emotionView.findViewById(R.id.gridview);
    final ChatEmotionGridAdapter chatEmotionGridAdapter = new ChatEmotionGridAdapter(this);
    List<String> pageEmotions = EmotionHelper.emojiGroups.get(pos);
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
    recordBtn.setSavePath(PathUtils.getRecordPathByCurrentTime());
    recordBtn.setRecordEventListener(new RecordButton.RecordEventListener() {
      @Override
      public void onFinishedRecord(final String audioPath, int secs) {
//        LogUtils.d("audioPath = ", audioPath);
        messageAgent.sendAudio(audioPath);
      }

      @Override
      public void onStartRecord() {

      }
    });
  }

  public void setEditTextChangeListener() {
    contentEdit.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        if (charSequence.length() > 0) {
          sendBtn.setEnabled(true);
          showSendBtn();
        } else {
          sendBtn.setEnabled(false);
          showTurnToRecordBtn();
        }
      }

      @Override
      public void afterTextChanged(Editable editable) {

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
    chatInstance = this;
    roomsTable = ChatManager.getInstance().getRoomsTable();
    eventBus = EventBus.getDefault();
    eventBus.register(this);
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
  }

  private boolean isConversationEmpty(AVIMConversation conversation) {
    if (conversation == null) {
      toast("未找到对话，请退出重试。请检查是否调用了 ChatManager.registerConversation()");
      this.finish();
      return true;
    }
    return false;
  }

  public void initData(Intent intent) {
    String convid = intent.getStringExtra(CONVID);
    conversation = chatManager.lookUpConversationById(convid);
    if (isConversationEmpty(conversation)) {
      return;
    }
    initActionBar(ConversationHelper.titleOfConversation(conversation));
    messageAgent = new MessageAgent(conversation);
    messageAgent.setSendCallback(defaultSendCallback);
    roomsTable.clearUnread(conversation.getConversationId());
    conversationType = ConversationHelper.typeOfConversation(conversation);
    bindAdapterToListView(conversationType);
  }

  protected void initActionBar(String title) {
    ActionBar actionBar = getActionBar();
    if (actionBar != null) {
      if (title != null) {
        actionBar.setTitle(title);
      }
      actionBar.setDisplayUseLogoEnabled(false);
      actionBar.setDisplayHomeAsUpEnabled(true);
    } else {
      LogUtils.i("action bar is null, so no title, please set an ActionBar style for activity");
    }
  }

  private void bindAdapterToListView(ConversationType conversationType) {
    adapter = new ChatMessageAdapter(this, conversationType);
    adapter.setClickListener(new ChatMessageAdapter.ClickListener() {
      @Override
      public void onFailButtonClick(AVIMTypedMessage msg) {
        messageAgent.resendMessage(msg, new MessageAgent.SendCallback() {
          @Override
          public void onError(AVIMTypedMessage message, Exception e) {
            LogUtils.d("resend message error");
            // 应该只重新加载一条, Todo
            loadMessagesWhenInit(adapter.getCount());
          }

          @Override
          public void onSuccess(AVIMTypedMessage message) {
            LogUtils.d("resend message success");
            // 应该只重新加载一条, Todo
            loadMessagesWhenInit(adapter.getCount());
          }
        });
      }

      @Override
      public void onLocationViewClick(AVIMLocationMessage locMsg) {
        onLocationMessageViewClicked(locMsg);
      }

      @Override
      public void onImageViewClick(AVIMImageMessage imageMsg) {
        ImageBrowserActivity.go(ChatActivity.this,
            MessageHelper.getFilePath(imageMsg),
            imageMsg.getFileUrl());
      }
    });
    messageListView.setAdapter(adapter);
  }

  @Override
  public void onClick(View v) {
    //之所以不用 switch 是因为在Library中 R.id.btn 不是常量，不能用在switch case中。
    // 如果主项目有重名，Library 的会被重命名。，所以不能是常量。
    if (v.getId() == R.id.sendBtn) {
      sendText();
    } else if (v.getId() == R.id.addImageBtn) {
      selectImageFromLocal();
    } else if (v.getId() == R.id.turnToAudioBtn) {
      showAudioLayout();
    } else if (v.getId() == R.id.turnToTextBtn) {
      showTextLayout();
    } else if (v.getId() == R.id.showAddBtn) {
      toggleBottomAddLayout();
    } else if (v.getId() == R.id.showEmotionBtn) {
      toggleEmotionLayout();
    } else if (v.getId() == R.id.addLocationBtn) {
      onAddLocationButtonClicked(v);
    } else if (v.getId() == R.id.textEdit) {
      hideBottomLayoutAndScrollToLast();
    } else if (v.getId() == R.id.addCameraBtn) {
      selectImageFromCamera();
    }
  }

  private void hideBottomLayoutAndScrollToLast() {
    hideBottomLayout();
    scrollToLast();
  }

  protected void hideBottomLayout() {
    hideAddLayout();
    chatEmotionLayout.setVisibility(View.GONE);
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

  protected void hideSoftInputView() {
    if (getWindow().getAttributes().softInputMode !=
        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
      InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
      View currentFocus = getCurrentFocus();
      if (currentFocus != null) {
        manager.hideSoftInputFromWindow(currentFocus.getWindowToken(),
            InputMethodManager.HIDE_NOT_ALWAYS);
      }
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
    Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
    Uri imageUri = Uri.fromFile(new File(localCameraPath));
    takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
      startActivityForResult(takePictureIntent, TAKE_CAMERA_REQUEST);
    }
  }

  private void sendText() {
    final String content = contentEdit.getText().toString();
    if (!TextUtils.isEmpty(content)) {
      messageAgent.sendText(content);
      contentEdit.setText("");
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        super.onBackPressed();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (resultCode == RESULT_OK) {
      switch (requestCode) {
        case GALLERY_REQUEST:
        case GALLERY_KITKAT_REQUEST:
          if (intent == null) {
            toast("return intent is null");
            return;
          }
          Uri uri;
          if (requestCode == GALLERY_REQUEST) {
            uri = intent.getData();
          } else {
            //for Android 4.4
            uri = intent.getData();
            final int takeFlags = intent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
          }
          String localSelectPath = ProviderPathUtils.getPath(this, uri);
          messageAgent.sendImage(localSelectPath);
          hideBottomLayout();
          break;
        case TAKE_CAMERA_REQUEST:
          messageAgent.sendImage(localCameraPath);
          hideBottomLayout();
          break;
      }
    }
  }

  public void scrollToLast() {
    messageListView.post(new Runnable() {
      @Override
      public void run() {
        messageListView.smoothScrollToPosition(messageListView.getAdapter().getCount() - 1);
      }
    });
  }

  @Override
  protected void onDestroy() {
    chatInstance = null;
    eventBus.unregister(this);
    super.onDestroy();
  }

  public void onEvent(MessageEvent messageEvent) {
    final AVIMTypedMessage message = messageEvent.getMessage();
    if (message.getConversationId().equals(conversation
        .getConversationId())) {
      if (messageEvent.getType() == MessageEvent.Type.Come) {
        new CacheMessagesTask(this, Arrays.asList(message)) {
          @Override
          void onSucceed(List<AVIMTypedMessage> messages) {
            addMessageAndScroll(message);
          }
        }.execute();
      } else if (messageEvent.getType() == MessageEvent.Type.Receipt) {
        //Utils.i("receipt");
        AVIMTypedMessage originMessage = findMessage(message.getMessageId());
        if (originMessage != null) {
          originMessage.setMessageStatus(message.getMessageStatus());
          originMessage.setReceiptTimestamp(message.getReceiptTimestamp());
          adapter.notifyDataSetChanged();
        }
      }
    }
  }

  private AVIMTypedMessage findMessage(String messageId) {
    for (AVIMTypedMessage originMessage : adapter.getDatas()) {
      if (originMessage.getMessageId() != null && originMessage.getMessageId().equals(messageId)) {
        return originMessage;
      }
    }
    return null;
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (isConversationEmpty(conversation)) {
      return;
    }
    ChatManager.setCurrentChattingConvid(conversation.getConversationId());
  }

  @Override
  protected void onPause() {
    super.onPause();
    roomsTable.clearUnread(conversation.getConversationId());
    ChatManager.setCurrentChattingConvid(null);
  }

  public void loadMessagesWhenInit(int limit) {
    ChatManager.getInstance().queryMessages(conversation, null, System.currentTimeMillis(), limit, new
        AVIMTypedMessagesArrayCallback() {
          @Override
          public void done(final List<AVIMTypedMessage> typedMessages, AVException e) {
            if (filterException(e)) {
              new CacheMessagesTask(ChatActivity.this, typedMessages) {
                @Override
                void onSucceed(List<AVIMTypedMessage> messages) {
                  adapter.setDatas(typedMessages);
                  adapter.notifyDataSetChanged();
                  scrollToLast();
                }
              }.execute();
            }
          }
        });
  }

  public abstract class CacheMessagesTask extends AsyncTask<Void, Void, Void> {
    private List<AVIMTypedMessage> messages;
    private volatile Exception e;

    public CacheMessagesTask(Context context, List<AVIMTypedMessage> messages) {
      this.messages = messages;
    }

    @Override
    protected Void doInBackground(Void... voids) {
      Set<String> userIds = new HashSet<>();
      for (AVIMTypedMessage msg : messages) {
        AVIMReservedMessageType type = AVIMReservedMessageType.getAVIMReservedMessageType(msg.getMessageType());
        if (type == AVIMReservedMessageType.AudioMessageType) {
          File file = new File(MessageHelper.getFilePath(msg));
          if (!file.exists()) {
            AVIMAudioMessage audioMsg = (AVIMAudioMessage) msg;
            String url = audioMsg.getFileUrl();
//            if (audioMsg.getFileMetaData() != null) {
//              int size = (Integer) audioMsg.getFileMetaData().get("size");
//              LogUtils.d("metaData size", size + "");
//            }
            Utils.downloadFileIfNotExists(url, file);
          }
        }
        userIds.add(msg.getFrom());
      }
      if (chatManager.getChatManagerAdapter() == null) {
        throw new IllegalStateException("please set ChatManagerAdapter in ChatManager to provide userInfo");
      }
      try {
        chatManager.getChatManagerAdapter().cacheUserInfoByIdsInBackground(new ArrayList<String>(userIds));
      } catch (Exception e1) {
        e1.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      if (filterException(e)) {
        onSucceed(messages);
      }
    }

    abstract void onSucceed(List<AVIMTypedMessage> messages);
  }

  public void loadOldMessages() {
    if (adapter.getDatas().size() == 0) {
      refreshableView.finishRefreshing();
      return;
    } else {
      AVIMTypedMessage firstMsg = adapter.getDatas().get(0);
      String msgId = firstMsg.getMessageId();
      long time = firstMsg.getTimestamp();
      ChatManager.getInstance().queryMessages(conversation, msgId, time, PAGE_SIZE, new AVIMTypedMessagesArrayCallback() {
        @Override
        public void done(List<AVIMTypedMessage> typedMessages, AVException e) {
          refreshableView.finishRefreshing();
          if (filterException(e)) {
            new CacheMessagesTask(ChatActivity.this, typedMessages) {
              @Override
              void onSucceed(List<AVIMTypedMessage> typedMessages) {
                List<AVIMTypedMessage> newMessages = new ArrayList<>(PAGE_SIZE);
                newMessages.addAll(typedMessages);
                newMessages.addAll(adapter.getDatas());
                adapter.setDatas(newMessages);
                adapter.notifyDataSetChanged();
                if (typedMessages.size() > 0) {
                  messageListView.setSelection(typedMessages.size() - 1);
                } else {
                  toast(R.string.chat_activity_loadMessagesFinish);
                }
              }
            }.execute();
          }
        }
      });
    }

  }

  class DefaultSendCallback implements MessageAgent.SendCallback {

    @Override
    public void onError(AVIMTypedMessage message, Exception e) {
      LogUtils.i();
      addMessageAndScroll(message);
    }

    @Override
    public void onSuccess(AVIMTypedMessage message) {
//      Utils.i();
      addMessageAndScroll(message);
    }
  }

  public void addMessageAndScroll(AVIMTypedMessage message) {
    AVIMTypedMessage foundMessage = findMessage(message.getMessageId());
    if (foundMessage == null) {
      adapter.add(message);
      scrollToLast();
    }
  }

  protected boolean filterException(Exception e) {
    if (e != null) {
      e.printStackTrace();
      toast(e.getMessage());
      return false;
    } else {
      return true;
    }
  }

  protected void toast(String str) {
    Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
  }

  protected void toast(Exception e) {
    if (e != null) {
      toast("发生错误：" + e.getMessage());
    }
  }

  protected void toast(int id) {
    Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
  }

  /**
   * 当发送地理位置按钮被点击时
   *
   * @param v
   */
  protected void onAddLocationButtonClicked(View v) {

  }

  /**
   * 当地图消息view被点击时
   *
   * @param locationMessage
   */
  protected void onLocationMessageViewClicked(AVIMLocationMessage locationMessage) {

  }
}
