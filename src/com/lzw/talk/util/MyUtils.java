package com.lzw.talk.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.lzw.talk.R;
import com.lzw.talk.base.App;

import java.util.Date;


/**
 * Created by lzw on 14-5-29.
 */
public class MyUtils {

  public static void intentShare(Context context, String title, String shareContent) {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share));
    intent.putExtra(Intent.EXTRA_TEXT, shareContent);
    intent.putExtra(Intent.EXTRA_TITLE, title);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.please_choose)));
  }

  public static void toast(Context cxt, int id) {
    Toast.makeText(cxt, id, Toast.LENGTH_SHORT).show();
  }

  public static void toastLong(Context cxt, int id) {
    Toast.makeText(cxt, id, Toast.LENGTH_LONG).show();
  }

  public static ProgressDialog showSpinnerDialog(Activity activity) {
    activity = Utils.modifyDialogContext(activity);
    ProgressDialog dialog = new ProgressDialog(activity);
    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    dialog.setCancelable(true);
    dialog.setMessage(App.cxt.getString(R.string.hardLoading));
    dialog.show();
    return dialog;
  }

  public static ProgressDialog showHorizontalDialog(Activity activity) {
    activity = Utils.modifyDialogContext(activity);
    ProgressDialog dialog = new ProgressDialog(activity);
    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    dialog.setCancelable(true);
    dialog.show();
    return dialog;
  }

  public static int currentSecs() {
    int l;
    l = (int) (new Date().getTime() / 1000);
    return l;
  }
}
