package com.avoscloud.leanchatlib.controller;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.avoscloud.leanchatlib.utils.LogUtils;

/**
 * Created by lzw on 15/7/14.
 */
class DBHelper extends SQLiteOpenHelper {
  private static final int DB_VER = 6;
  private String userId;

  private DBHelper(Context context, String name, int version) {
    super(context, name, null, version);
  }

  DBHelper(Context context, String userId) {
    this(context, "chat_" + userId + ".db3", DB_VER);
    this.userId = userId;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
  }

  @Override
  public void onOpen(SQLiteDatabase db) {
    super.onOpen(db);
    LogUtils.d("chat db path", db.getPath());
    RoomsTable.createTableAndIndex(db);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    switch (newVersion) {
      case 6:
        RoomsTable.dropTable(db);
        RoomsTable.createTableAndIndex(db);
      case 2:
      case 1:
        break;
    }
  }
}
