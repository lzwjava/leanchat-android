package com.lzw.talk.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import com.lzw.talk.R;
import com.lzw.talk.adapter.ChatMsgAdapter;
import com.lzw.talk.adapter.EmotionGridAdapter;
import com.lzw.talk.adapter.EmotionPagerAdapter;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.db.DBHelper;
import com.lzw.talk.db.DBMsg;
import com.lzw.talk.entity.Msg;
import com.lzw.talk.receiver.MsgReceiver;
import com.lzw.talk.service.ChatService;
import com.lzw.talk.service.EmotionService;
import com.lzw.talk.service.MessageListener;
import com.lzw.talk.ui.view.EmotionsEditText;
import com.lzw.talk.ui.view.HeaderLayout;
import com.lzw.talk.ui.view.RecordButton;
import com.lzw.talk.util.NetAsyncTask;
import com.lzw.talk.util.PathUtils;
import com.lzw.talk.util.PhotoUtil;
import com.lzw.talk.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatActivity extends BaseActivity implements OnClickListener, MessageListener {
  public static final String CHAT_USER_ID = "chatUserId";
  private static final int IMAGE_REQUEST = 0;
  public static final int LOCATION_REQUEST = 1;
  private static final int TAKE_CAMERA_REQUEST = 2;
  private ChatMsgAdapter adapter;
  private List<Msg> msgs;
  public static ChatActivity instance;
  User me;
  DBHelper dbHelper;
  private Activity ctx;
  User chatUser;

  HeaderLayout headerLayout;
  View chatTextLayout, chatAudioLayout, chatAddLayout, chatEmotionLayout;
  View turnToTextBtn, turnToAudioBtn, sendBtn, addImageBtn, showAddBtn, addLocationBtn, showEmotionBtn;
  ViewPager emotionPager;
  private EmotionsEditText contentEdit;
  private ListView listView;
  RecordButton recordBtn;
  List<String> emotions = EmotionService.emotionTexts;
  private String localCameraPath = PathUtils.getTmpPath();
  private View addCameraBtn;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ctx = this;
    instance = this;
    setContentView(R.layout.chat_layout);
    findView();
    initData();
    initHeader();
    initEmotionPager();
    initRecordBtn();
    setEditTextChangeListener();
    //turnToAudioBtn.performClick();
    refresh();
    setSoftInputMode();
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
    EmotionGridAdapter emotionGridAdapter = new EmotionGridAdapter(ctx);
    List<String> pageEmotions;
    if (pos == 0) {
      pageEmotions = emotions.subList(0, 20);
    } else {
      pageEmotions = emotions.subList(20, emotions.size());
    }
    emotionGridAdapter.setDatas(pageEmotions);
    gridView.setAdapter(emotionGridAdapter);
    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String emotionText = (String) parent.getAdapter().getItem(position);
        int start = contentEdit.getSelectionStart();
        CharSequence content = contentEdit.getText().insert(start, emotionText);
        contentEdit.setText(content);
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
        Pattern pattern = Pattern.compile(".*/(.*)");
        Matcher matcher = pattern.matcher(audioPath);
        matcher.matches();
        final String objectId = matcher.group(1);
        new NetAsyncTask(ctx, false) {
          @Override
          protected void doInBack() throws Exception {
            ChatService.sendAudioMsg(chatUser, audioPath, objectId);
          }

          @Override
          protected void onPost(boolean res) {
            if (res) {
              refresh();
            } else {
              Utils.toast(ctx, R.string.badNetwork);
            }
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
    recordBtn.setSavePath(PathUtils.getRecordUuidPath());
  }

  public void setEditTextChangeListener() {
    contentEdit.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
          showSendBtn();
        } else {
          showTurnToRecordBtn();
        }
      }

      @Override
      public void afterTextChanged(Editable s) {
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

  private void initHeader() {
    headerLayout.showTitle(chatUser.getNickname());
    headerLayout.showLeftBackButton(R.string.back, null);
  }

  private void findView() {
    listView = (ListView) findViewById(R.id.listview);
    addImageBtn = findViewById(R.id.addImageBtn);

    contentEdit = (EmotionsEditText) findViewById(R.id.textEdit);
    headerLayout = (HeaderLayout) findViewById(R.id.headerLayout);
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

  @Override
  public void onResume() {
    super.onResume();
    MsgReceiver.registerMessageListener(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    MsgReceiver.unregisterMessageListener();
  }

  public void initData() {
    me = User.curUser();
    dbHelper = new DBHelper(ctx, App.DB_NAME, App.DB_VER);
    msgs = new ArrayList<Msg>();
    adapter = new ChatMsgAdapter(this);
    Intent intent = getIntent();
    String chatUserId = intent.getStringExtra(CHAT_USER_ID);
    chatUser = App.lookupUser(chatUserId);
    listView.setAdapter(adapter);
  }

  public void refresh() {
    new GetDataTask().execute();
  }

  @Override
  public void onMessage(Msg msg) {
    refresh();
  }

  @Override
  public void onMessageFailure(Msg msg) {
    refresh();
  }

  @Override
  public void onMessageSent(Msg msg) {
    refresh();
  }

  class GetDataTask extends AsyncTask<Void, Void, Void> {
    boolean res;
    List<Msg> msgs;

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
      try {
        msgs = DBMsg.getMsgs(dbHelper, ChatService.getPeerId(me),
            ChatService.getPeerId(chatUser));
        res = true;
      } catch (Exception e) {
        e.printStackTrace();
        res = false;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      if (res) {
        addMsgsAndRefresh(msgs);
      } else {
        Utils.toast(ctx, R.string.failedToGetData);
      }
    }
  }

  public void addMsgsAndRefresh(List<Msg> msgs) {
    this.msgs = msgs;
    adapter.setDatas(this.msgs);
    adapter.notifyDataSetChanged();
    scroolToLast();
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
        onTextEditClick();
        break;
      case R.id.addCameraBtn:
        selectImageFromCamera();
        break;
    }
  }

  private void onTextEditClick() {
    chatAddLayout.setVisibility(View.GONE);
    chatEmotionLayout.setVisibility(View.GONE);
    scroolToLast();
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
      chatAddLayout.setVisibility(View.GONE);
      showTextLayout();
    }
  }

  private void toggleBottomAddLayout() {
    if (chatAddLayout.getVisibility() == View.VISIBLE) {
      chatAddLayout.setVisibility(View.GONE);
    } else {
      chatEmotionLayout.setVisibility(View.GONE);
      chatAddLayout.setVisibility(View.VISIBLE);
      hideSoftInputView();
    }
  }

  private void showTextLayout() {
    chatTextLayout.setVisibility(View.VISIBLE);
    chatAudioLayout.setVisibility(View.GONE);
  }

  private void showAudioLayout() {
    chatTextLayout.setVisibility(View.GONE);
    chatAudioLayout.setVisibility(View.VISIBLE);
    hideSoftInputView();
  }

  public void selectImageFromLocal() {
    Intent intent;
    if (Build.VERSION.SDK_INT < 19) {
      intent = new Intent(Intent.ACTION_GET_CONTENT);
      intent.setType("image/*");
    } else {
      intent = new Intent(Intent.ACTION_PICK,
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }
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
    final String contString = contentEdit.getText().toString();
    if (contString.length() > 0) {
      new NetAsyncTask(ctx, false) {
        @Override
        protected void doInBack() throws Exception {
          ChatService.sendTextMsg(chatUser, contString);
        }

        @Override
        protected void onPost(boolean res) {
          if (res) {
            hideSoftInputView();
            refresh();
          } else {
            Utils.toast(ctx, R.string.badNetwork);
          }
        }
      }.execute();
      contentEdit.setText("");
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
      if (localSelectPath == null
          || localSelectPath.equals("null")) {
        Utils.toast(ctx, R.string.cannotFindImage);
        return null;
      }
    }
    return localSelectPath;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      switch (requestCode) {
        case IMAGE_REQUEST:
          String localSelectPath = parsePathByReturnData(data);
          if (localSelectPath != null) {
            sendImageByPath(localSelectPath);
          }
          break;
        case TAKE_CAMERA_REQUEST:
          sendImageByPath(localCameraPath);
          break;
        case LOCATION_REQUEST:
          sendLocationByReturnData(data);
          break;
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  private void sendLocationByReturnData(Intent data) {
    double latitude = data.getDoubleExtra("x", 0);// 维度
    double longtitude = data.getDoubleExtra("y", 0);// 经度
    String address = data.getStringExtra("address");
    if (address != null && !address.equals("")) {
      ChatService.sendLocationMessage(ChatService.getPeerId(chatUser), address, latitude, longtitude);
    } else {
      Utils.toast(ctx, R.string.cannotGetYourAddressInfo);
    }
  }

  private void sendImageByPath(String localSelectPath) {
    final String objectId = Utils.uuid();
    final String newPath = PathUtils.getImageDir() + objectId;
    PhotoUtil.compressImage(localSelectPath, newPath);
    new NetAsyncTask(App.ctx, false) {
      @Override
      protected void doInBack() throws Exception {
        ChatService.sendImageMsg(chatUser, newPath, objectId);
      }

      @Override
      protected void onPost(boolean res) {
        refresh();
      }
    }.execute();
  }

  public void scroolToLast() {
    listView.setSelection(listView.getCount() - 1);
  }
}
