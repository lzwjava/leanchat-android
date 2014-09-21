package com.lzw.talk.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import com.lzw.talk.util.PhotoUtil;
import com.lzw.talk.util.Utils;
import com.lzw.talk.view.HeaderLayout;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends Activity implements OnClickListener, MessageListener {
  private static final int IMAGE_REQUEST = 0;
  private EditText mEditTextContent;
  private ListView mListView;
  private ChatMsgAdapter mAdapter;
  private List<Msg> msgs;
  public static ChatActivity instance;
  ProgressBar progressBar;
  User me;
  DBHelper dbHelper;
  View btnSend, addImageBtn;
  HeaderLayout headerLayout;
  private Activity ctx;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ctx = this;
    instance = this;
    getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    setContentView(R.layout.chat_room);
    findView();
    initHeader();
    initData();
  }

  private void initHeader() {
    headerLayout.showTitle(App.chatUser.getUsername());
  }

  private void findView() {
    progressBar = (ProgressBar) findViewById(R.id.progressBar);
    mListView = (ListView) findViewById(R.id.listview);
    addImageBtn = findViewById(R.id.addImageBtn);

    mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);
    headerLayout = (HeaderLayout) findViewById(R.id.headerLayout);
    btnSend = findViewById(R.id.btn_send);
    btnSend.setOnClickListener(this);
    addImageBtn.setOnClickListener(this);
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
    mAdapter = new ChatMsgAdapter(this);
    mListView.setAdapter(mAdapter);
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
        msgs = DBMsg.getMsgs(dbHelper, ChatService.getPeerId(me), ChatService.getPeerId(App.chatUser));
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
      progressBar.setVisibility(View.INVISIBLE);
      if (res) {
        addMsgsAndRefresh(msgs);
      } else {
        Utils.toast(ctx, R.string.failedToGetData);
      }
    }
  }

  public void addMsgsAndRefresh(List<Msg> msgs) {
    this.msgs = msgs;
    mAdapter.setDatas(this.msgs);
    mAdapter.notifyDataSetChanged();
    scroolToLast();
  }

  @Override
  public void onClick(View v) {
    // TODO Auto-generated method stub
    switch (v.getId()) {
      case R.id.btn_send:
        send();
        break;
      case R.id.addImageBtn:
        selectImageFromLocal();
        break;
    }
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
    String contString = mEditTextContent.getText().toString();
    if (contString.length() > 0) {
      new SendTask(contString).execute();
      mEditTextContent.setText("");
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
            final String newPath = PhotoUtil.compressImage(localSelectPath);
            new NetAsyncTask(App.ctx, false) {
              @Override
              protected void doInBack() throws Exception {
                ChatService.sendImageMsg(App.chatUser, newPath);
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
      progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Void doInBackground(Void... params) {
      try {
        ChatService.sendTextMsg(App.chatUser, text);
        res = true;
      } catch (Exception e) {
        e.printStackTrace();
        res = false;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      progressBar.setVisibility(View.GONE);
      if (res) {
        refresh();
      } else {
        Utils.toast(ctx, R.string.badNetwork);
      }
    }
  }

  private void scroolToLast() {
    mListView.setSelection(mListView.getCount() - 1);
  }
}
