package com.avoscloud.chat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.avos.avoscloud.AVUser;

/**
 * Created by lzw on 14-5-28.
 */
public class DBHelper extends SQLiteOpenHelper {
  private static final int DB_VER = 6;
  private static DBHelper currentUserDBHelper;

  private DBHelper(Context context, String name, int version) {
    super(context, name, null, version);
  }

  private DBHelper(Context context, String userId) {
    this(context, "chat_" + userId + ".db3", DB_VER);
  }

  public synchronized static DBHelper getCurrentUserInstance(Context context) {
    AVUser user = AVUser.getCurrentUser();
    if (user == null) {
      throw new NullPointerException("current user is null");
    }
    if (currentUserDBHelper == null) {
      currentUserDBHelper = new DBHelper(context, user.getObjectId());
    }
    return currentUserDBHelper;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    MsgsTable.getCurrentUserInstance().createTable(db);
    RoomsTable.getCurrentUserInstance().createTable(db);
  }

  @Override
  public void onOpen(SQLiteDatabase db) {
    super.onOpen(db);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    switch (newVersion) {
      case 6:
        MsgsTable msgsTable = MsgsTable.getCurrentUserInstance();
        msgsTable.dropTable(db);
        msgsTable.createTable(db);
        RoomsTable roomsTable = RoomsTable.getCurrentUserInstance();
        roomsTable.dropTable(db);
        roomsTable.createTable(db);
      case 2:
      case 1:
        break;
    }
  }

  public synchronized void closeHelper() {
    MsgsTable.getCurrentUserInstance().close();
    RoomsTable.getCurrentUserInstance().close();
    currentUserDBHelper = null;
  }
}
