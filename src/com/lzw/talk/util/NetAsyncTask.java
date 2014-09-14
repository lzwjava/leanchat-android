package com.lzw.talk.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by lzw on 14-6-7.
 */
public abstract class NetAsyncTask extends AsyncTask<Void, Void, Void> {
  public boolean res;
  ProgressDialog dialog;
  public Context cxt;
  boolean openDialog = true;

  protected NetAsyncTask(Context cxt) {
    this.cxt = cxt;
  }

  protected NetAsyncTask(Context cxt, boolean openDialog) {
    this.cxt = cxt;
    this.openDialog = openDialog;
  }

  public NetAsyncTask setOpenDialog(boolean openDialog) {
    this.openDialog = openDialog;
    return this;
  }

  public ProgressDialog getDialog() {
    return dialog;
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    if (openDialog) {
      dialog = Utils.showSpinnerDialog((Activity) cxt);
    }
  }

  @Override
  protected Void doInBackground(Void... params) {
    try {
      doInBack();
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
    if (openDialog) {
      if(dialog.isShowing()){
        dialog.dismiss();
      }
    }
    onPost(res);
  }

  protected abstract void doInBack() throws Exception;

  protected abstract void onPost(boolean res);
}
