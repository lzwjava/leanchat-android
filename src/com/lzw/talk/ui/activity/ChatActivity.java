package com.lzw.talk.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.lzw.talk.R;
import com.lzw.talk.adapter.ChatMsgAdapter;
import com.lzw.talk.adapter.EmotionGridAdapter;
import com.lzw.talk.adapter.EmotionPagerAdapter;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.db.DBHelper;
import com.lzw.talk.db.DBMsg;
import com.lzw.talk.entity.Msg;
import com.lzw.talk.service.*;
import com.lzw.talk.ui.view.EmotionEditText;
import com.lzw.talk.ui.view.HeaderLayout;
import com.lzw.talk.ui.view.RecordButton;
import com.lzw.talk.ui.view.xlist.XListView;
import com.lzw.talk.util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatActivity extends BaseActivity implements OnClickListener, MessageListener,
    XListView.IXListViewListener {
  public static final String CHAT_USER_ID = "chatUserId";
  private static final int IMAGE_REQUEST = 0;
  public static final int LOCATION_REQUEST = 1;
  private static final int TAKE_CAMERA_REQUEST = 2;
  public static final int PAGE_SIZE = 10;
  private ChatMsgAdapter adapter;
  private List<Msg> msgs = new ArrayList<Msg>();
  public static ChatActivity instance;
  User me;
  DBHelper dbHelper;
  private Activity ctx;
  User chatUser;

  HeaderLayout headerLayout;
  View chatTextLayout, chatAudioLayout, chatAddLayout, chatEmotionLayout;
  View turnToTextBtn, turnToAudioBtn, sendBtn, addImageBtn, showAddBtn, addLocationBtn, showEmotionBtn;
  LinearLayout chatBottomLayout;
  ViewPager emotionPager;
  private EmotionEditText contentEdit;
  private XListView xListView;
  RecordButton recordBtn;
  List<String> emotions = EmotionService.emotionTexts;
  private String localCameraPath = PathUtils.getTmpPath();
  private View addCameraBtn;
  int msgSize = PAGE_SIZE;
  AnimService animService;

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
    initListView();
    setSoftInputMode();
    ChatService.withUserToWatch(chatUser, true);
    loadNewMsg();
  }

  private void initListView() {
    adapter = new ChatMsgAdapter(ctx, msgs);
    adapter.setDatas(msgs);
    xListView.setAdapter(adapter);
    xListView.setPullRefreshEnable(true);
    xListView.setPullLoadEnable(false);
    xListView.setXListViewListener(this);
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
        new SendMsgTask(ctx) {
          @Override
          Msg sendMsg() throws Exception {
            return ChatService.sendAudioMsg(chatUser, audioPath, objectId);
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
    headerLayout.showTitle(chatUser.getUsername());
    headerLayout.showLeftBackButton(R.string.back, null);
  }

  private void findView() {
    xListView = (XListView) findViewById(R.id.listview);
    addImageBtn = findViewById(R.id.addImageBtn);

    contentEdit = (EmotionEditText) findViewById(R.id.textEdit);
    headerLayout = (HeaderLayout) findViewById(R.id.headerLayout);
    chatTextLayout = findViewById(R.id.chatTextLayout);
    chatAudioLayout = findViewById(R.id.chatRecordLayout);
    chatBottomLayout= (LinearLayout) findViewById(R.id.bottomLayout);
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
    MsgReceiver.registerMessageListener(chatUser.getObjectId(), this);
  }

  @Override
  public void onPause() {
    super.onPause();
    MsgReceiver.unregisterMessageListener(chatUser.getObjectId());
  }

  public void initData() {
    me = User.curUser();
    dbHelper = new DBHelper(ctx, App.DB_NAME, App.DB_VER);
    Intent intent = getIntent();
    String chatUserId = intent.getStringExtra(CHAT_USER_ID);
    chatUser = App.lookupUser(chatUserId);
    animService= AnimService.getInstance();
  }

  @Override
  public void onMessage(Msg msg) {
    Logger.d("onMessage on ChatActivity " + msg.getContent());
    if (msg.getType() == Msg.TYPE_RESPONSE) {
      loadNewMsg();
    } else {
      addMsgAndScrollToLast(msg);
    }
  }

  public void addMsgAndScrollToLast(Msg msg) {
    adapter.add(msg);
    hideBottomLayoutAndScrollToLast();
  }

  @Override
  public void onMessageFailure(Msg failMsg) {
    Logger.d("onMessageFailure on Chat Activity " + failMsg.getContent());
    Msg msg = adapter.getItem(failMsg.getObjectId());
    if (msg != null) {
      msg.setStatus(Msg.STATUS_SEND_FAILED);
      adapter.notifyDataSetChanged();
    }
  }

  @Override
  public void onMessageSent(Msg sentMsg) {
    Logger.d("onMessageSent on ChatActivity " + sentMsg.getContent());
    Msg msg = adapter.getItem(sentMsg.getObjectId());
    if (msg != null) {
      msg.setStatus(Msg.STATUS_SEND_SUCCEED);
      adapter.notifyDataSetChanged();
    }
  }

  public void setViewStatusByMsg(Msg msg) {
    View msgView;
    msgView = getMsgViewByMsg(msg);
    if (msgView != null) {
      TextView contentView = (TextView) msgView.findViewById(R.id.textContent);
      Logger.w("find view's content=" + contentView.getText().toString());
      TextView statusView = (TextView) msgView.findViewById(R.id.status);
      statusView.setText(msg.getStatusDesc());
    }
  }

  public View getMsgViewByMsg(Msg msg) {
    int msgPos = adapter.getItemPosById(msg.getObjectId());
    if (msgPos < 0) {
      Logger.i("cannot find msg " + msg.getContent());
      return null;
    }
    Logger.d("msgPos=" + msgPos);
    int firstPos = xListView.getFirstVisiblePosition()
        - xListView.getHeaderViewsCount();
    int wantedChild = msgPos - firstPos;
    Logger.d("wanted child pos=" + wantedChild);
    if (wantedChild < 0 || wantedChild >= xListView.getChildCount()) {
      Logger.d("Unable to get view for desired position");
      return null;
    }
    return xListView.getChildAt(wantedChild);

  }

  public void loadNewMsg() {
    new GetDataTask(true).execute();
  }

  @Override
  public void onRefresh() {
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        msgSize += 6;
        new GetDataTask(false).execute();
      }
    }, 1000);
  }

  @Override
  public void onLoadMore() {
  }

  class GetDataTask extends AsyncTask<Void, Void, Void> {
    boolean res;
    List<Msg> msgs;
    boolean scrollToLast = true;

    GetDataTask(boolean scrollToLast) {
      this.scrollToLast = scrollToLast;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
      try {
        msgs = DBMsg.getMsgs(dbHelper, ChatService.getPeerId(me),
            ChatService.getPeerId(chatUser), msgSize);
        res = true;
      } catch (Exception e) {
        e.printStackTrace();
        res = false;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      ChatUtils.stopRefresh(xListView);
      if (res) {
        addMsgsAndRefresh(msgs, scrollToLast);
      } else {
        Utils.toast(ctx, R.string.failedToGetData);
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
      if(lastN==newN){
        Utils.toast(R.string.loadMessagesFinish);
      }
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
    hideAddLayout();
    chatEmotionLayout.setVisibility(View.GONE);
    scrollToLast();
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

  private void hideAddLayout(){
    if(chatAddLayout.getVisibility()==View.VISIBLE){
      animService.hideView(chatAddLayout);
    }
  }

  private void showAddLayout() {
    if(chatAddLayout.getVisibility()==View.GONE){
      chatAddLayout.setVisibility(View.VISIBLE);
      chatAddLayout.startAnimation(animService.popupFromBottomAnim);
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
    if (TextUtils.isEmpty(contString) == false) {
      new SendMsgTask(ctx) {
        @Override
        Msg sendMsg() throws Exception {
          return ChatService.sendTextMsg(chatUser, contString);
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
      if (localSelectPath == null) {
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
    final double latitude = data.getDoubleExtra("x", 0);// 缁村害
    final double longtitude = data.getDoubleExtra("y", 0);// 缁忓害
    final String address = data.getStringExtra("address");
    if (address != null && !address.equals("")) {
      new SendMsgTask(ctx) {
        @Override
        Msg sendMsg() throws Exception {
          return ChatService.sendLocationMessage(ChatService.getPeerId(chatUser),
              address, latitude, longtitude);
        }
      }.execute();
    } else {
      Utils.toast(ctx, R.string.cannotGetYourAddressInfo);
    }
  }

  public abstract class SendMsgTask extends SimpleNetTask {
    Msg msg;

    protected SendMsgTask(Context cxt) {
      super(cxt, false);
    }

    @Override
    protected void doInBack() throws Exception {
      msg = sendMsg();
    }

    @Override
    public void onSucceed() {
      addMsgAndScrollToLast(msg);
    }

    abstract Msg sendMsg() throws Exception;
  }

  private void sendImageByPath(String localSelectPath) {
    final String objectId = Utils.uuid();
    final String newPath = PathUtils.getImageDir() + objectId;
    PhotoUtil.compressImage(localSelectPath, newPath);
    new SendMsgTask(ctx) {
      @Override
      Msg sendMsg() throws Exception {
        return ChatService.sendImageMsg(chatUser, newPath, objectId);
      }
    }.execute();
  }

  public void scrollToLast() {
    Logger.d("scrollToLast");
    xListView.setSelection(adapter.getCount() - 1);
  }

  @Override
  protected void onDestroy() {
    ChatService.withUserToWatch(chatUser, false);
    super.onDestroy();
  }
}
