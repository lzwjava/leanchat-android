package com.lzw.talk.util;

import android.content.Context;
import com.lzw.talk.R;

/**
 * Created by lzw on 14-9-27.
 */
public abstract class SimpleNetTask extends NetAsyncTask {
  protected SimpleNetTask(Context cxt) {
    super(cxt);
  }

  protected SimpleNetTask(Context cxt, boolean openDialog) {
    super(cxt, openDialog);
  }


  @Override
  protected void onPost(Exception e) {
    if (e != null) {
      e.printStackTrace();
      Utils.toast(cxt, R.string.pleaseCheckNetwork);
    } else {
      onSucceed();
    }
  }

  protected abstract void doInBack() throws Exception;
  protected abstract void onSucceed();
}
