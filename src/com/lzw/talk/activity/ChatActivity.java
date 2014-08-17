package com.lzw.talk.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.Session;
import com.avos.avoscloud.SessionManager;
import com.lzw.commons.Logger;
import com.lzw.commons.TimeUtils;
import com.lzw.talk.R;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.db.DBHelper;
import com.lzw.talk.db.DBMsg;
import com.lzw.talk.entity.ChatMsgEntity;
import com.lzw.talk.adapter.ChatMsgViewAdapter;
import com.lzw.talk.entity.Msg;
import com.lzw.talk.receiver.MsgReceiver;
import com.lzw.talk.service.ChatService;
import com.lzw.talk.service.MessageListener;
import com.lzw.talk.util.MyUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends Activity implements OnClickListener, MessageListener {
  private EditText mEditTextContent;
  private ListView mListView;
  private ChatMsgViewAdapter mAdapter;
  private List<ChatMsgEntity> mDataArrays;
  public static ChatActivity instance;
  Activity cxt;
  MediaPlayer player;
  ProgressBar progressBar;
  AVUser me;
  DBHelper dbHelper;
  View btnSend;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    cxt = this;
    instance = this;
    getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    setContentView(R.layout.chat_xiaohei);
    findView();
    initPlayer();
    initData();
  }

  private void initPlayer() {
    player = new MediaPlayer();
  }

  private void findView() {
    progressBar = (ProgressBar) findViewById(R.id.progressBar);
    mListView = (ListView) findViewById(R.id.listview);

    mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);
    btnSend=findViewById(R.id.btn_send);
    btnSend.setOnClickListener(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    refresh();
    MsgReceiver.registerSessionListener(ChatService.getPeerId(App.chatUser), this);
  }

  @Override
  public void onPause() {
    super.onPause();
    MsgReceiver.unregisterSessionListener(ChatService.getPeerId(App.chatUser));
  }

  public void initData() {
    ActionBar actionBar = getActionBar();
    View view= LayoutInflater.from(cxt).inflate(R.layout.chat_bar,null);
    TextView title= (TextView) view.findViewById(R.id.title);
    actionBar.setDisplayShowCustomEnabled(true);
    actionBar.setDisplayShowTitleEnabled(false);
    actionBar.setDisplayShowHomeEnabled(false);
    actionBar.setCustomView(view);
    title.setText(App.chatUser.getUsername());
    me = AVUser.getCurrentUser();
    dbHelper = new DBHelper(cxt, App.DB_NAME, App.DB_VER);
    mDataArrays = new ArrayList<ChatMsgEntity>();
    mAdapter = new ChatMsgViewAdapter(this);
    mListView.setAdapter(mAdapter);
  }

  public void refresh() {
    new GetDataTask().execute();
  }

  @Override
  public void onMessage(String msg) {
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
        MyUtils.toast(cxt, R.string.getDataFailed);
      }
    }
  }

  public void addMsgsAndRefresh(List<Msg> msgs) {
    List<ChatMsgEntity> sublists = new ArrayList<ChatMsgEntity>();
    for (Msg msg : msgs) {
      ChatMsgEntity entity = getChatMsgEntity(msg);
      sublists.add(entity);
    }
    mDataArrays = sublists;
    mAdapter.setDatas(mDataArrays);
    mAdapter.notifyDataSetChanged();
    scroolToLast();
  }

  private ChatMsgEntity getChatMsgEntity(Msg msg) {
    ChatMsgEntity entity = new ChatMsgEntity();
    int created = msg.getCreated();
    Date date = new Date(created * 1000L);
    entity.setDate(TimeUtils.getDate(date));
    String fromId = msg.getFrom();
    String curId = ChatService.getPeerId(me);
    if (curId.equals(fromId)) {
      entity.setName(me.getUsername());
      entity.setMsgType(false);
    } else {
      String name;
      name = App.chatUser.getUsername();
      entity.setName(name);
      entity.setMsgType(true);
    }
    entity.setText(msg.getTxt());
    entity.setMsg(msg);
    return entity;
  }

  @Override
  public void onClick(View v) {
    // TODO Auto-generated method stub
    switch (v.getId()) {
      case R.id.btn_send:
        send();
        break;
    }
  }

  private void send() {
    String contString = mEditTextContent.getText().toString();
    if (contString.length() > 0) {
      new SendTask(contString).execute();
      mEditTextContent.setText("");
    }
  }

  class SendTask extends AsyncTask<Void, Void, Void> {
    String text;
    Msg msg;
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
        msg = new Msg();
        msg.setTxt(text);
        msg.setFrom(ChatService.getPeerId(me));
        int l = (int) (new Date().getTime() / 1000);
        msg.setCreated(l);
        msg.setTo(ChatService.getPeerId(App.chatUser));
        Logger.d(msg.getFromName());
        String json = msg.toJson();
        DBMsg.insertMsg(dbHelper, msg);
        List<String> ids = new ArrayList<String>();
        ids.add(ChatService.getPeerId(App.chatUser));
        String selfId = ChatService.getPeerId(User.curUser());
        Session session = SessionManager.getInstance(selfId);
        session.sendMessage(json, ids);
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
        MyUtils.toast(cxt, R.string.no_network);
      }
    }
  }

  private void scroolToLast() {
    mListView.setSelection(mListView.getCount() - 1);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (player != null) {
      player.release();
    }
  }
}
