package com.lzw.talk.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import com.lzw.talk.R;
import com.lzw.talk.adapter.ChatMsgAdapter;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.db.DBHelper;
import com.lzw.talk.db.DBMsg;
import com.lzw.talk.entity.Msg;
import com.lzw.talk.receiver.MsgReceiver;
import com.lzw.talk.service.ChatService;
import com.lzw.talk.service.MessageListener;
import com.lzw.talk.util.NetAsyncTask;
import com.lzw.talk.util.PathUtils;
import com.lzw.talk.util.PhotoUtil;
import com.lzw.talk.util.Utils;
import com.lzw.talk.view.HeaderLayout;
import com.lzw.talk.view.RecordButton;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatActivity extends Activity implements OnClickListener, MessageListener {
  public static final String CHAT_USER_ID = "chatUserId";
  private static final int IMAGE_REQUEST = 0;
  private ChatMsgAdapter adapter;
  private List<Msg> msgs;
  public static ChatActivity instance;
  User me;
  DBHelper dbHelper;
  private Activity ctx;
  User chatUser;

  HeaderLayout headerLayout;
  View chatTextLayout, chatAudioLayout, chatMoreLayout, chatAddLayout;
  View turnToTextBtn, turnToAudioBtn, sendBtn, addImageBtn;
  private EditText contentEdit;
  private ListView listView;
  RecordButton recordBtn;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ctx = this;
    instance = this;
    getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    setContentView(R.layout.chat_room);
    findView();
    initData();
    initHeader();
    initRecordBtn();
    setEditTextChangeListener();
    turnToAudioBtn.performClick();
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

    contentEdit = (EditText) findViewById(R.id.textEdit);
    headerLayout = (HeaderLayout) findViewById(R.id.headerLayout);
    chatTextLayout = findViewById(R.id.chatTextLayout);
    chatAudioLayout = findViewById(R.id.chatRecordLayout);
    turnToAudioBtn = findViewById(R.id.turnToAudioBtn);
    turnToTextBtn = findViewById(R.id.turnToTextBtn);
    recordBtn = (RecordButton) findViewById(R.id.recordBtn);

    sendBtn = findViewById(R.id.sendBtn);
    sendBtn.setOnClickListener(this);
    addImageBtn.setOnClickListener(this);
    turnToAudioBtn.setOnClickListener(this);
    turnToTextBtn.setOnClickListener(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    refresh();
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
    String chatUserId = intent.getStringExtra("chatUserId");
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
        send();
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
    }
  }

  private void showTextLayout() {
    chatTextLayout.setVisibility(View.VISIBLE);
    chatAudioLayout.setVisibility(View.GONE);
  }

  private void showAudioLayout() {
    chatTextLayout.setVisibility(View.GONE);
    chatAudioLayout.setVisibility(View.VISIBLE);
  }

  public void selectImageFromLocal() {
    Intent intent;
    if (Build.VERSION.SDK_INT < 19) {
      intent = new Intent(Intent.ACTION_GET_CONTENT);
      intent.setType("image/*");
    } else {
      intent = new Intent(
          Intent.ACTION_PICK,
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }
    startActivityForResult(intent, IMAGE_REQUEST);
  }

  private void send() {
    String contString = contentEdit.getText().toString();
    if (contString.length() > 0) {
      new SendTask(contString).execute();
      contentEdit.setText("");
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) switch (requestCode) {
      case IMAGE_REQUEST:
        if (data != null) {
          Uri selectedImage = data.getData();
          if (selectedImage != null) {
            Cursor cursor = getContentResolver().query(
                selectedImage, null, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex("_data");
            final String localSelectPath = cursor.getString(columnIndex);
            cursor.close();
            if (localSelectPath == null
                || localSelectPath.equals("null")) {
              Utils.toast(ctx, R.string.cannotFindImage);
              return;
            }
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
        }
        break;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  class SendTask extends AsyncTask<Void, Void, Void> {
    String text;

    boolean res;

    public SendTask(String text) {
      this.text = text;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
      try {
        ChatService.sendTextMsg(chatUser, text);
        res = true;
      } catch (Exception e) {
        e.printStackTrace();
        res = false;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      if (res) {
        refresh();
      } else {
        Utils.toast(ctx, R.string.badNetwork);
      }
    }
  }

  public void scroolToLast() {
    listView.setSelection(listView.getCount() - 1);
  }
}
