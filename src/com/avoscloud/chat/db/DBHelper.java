package com.avoscloud.chat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by lzw on 14-5-28.
 */
public class DBHelper extends SQLiteOpenHelper {

  public DBHelper(Context context, String name,int version) {
    super(context, name, null, version);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    DBMsg.createTable(db);
  }

  @Override
  public void onOpen(SQLiteDatabase db) {
    super.onOpen(db);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    switch (newVersion){
      case 3:
      case 2:
        DBMsg.dropTable(db);  //lazy solution
        DBMsg.createTable(db);
      case 1:
        break;
    }
  }
}
