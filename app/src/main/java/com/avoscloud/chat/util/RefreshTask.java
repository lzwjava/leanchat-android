package com.avoscloud.chat.util;

import android.content.Context;

/**
 * Created by lzw on 15/2/27.
 */
public abstract class RefreshTask extends SimpleNetTask {
  private Refreshable refreshable;

  public RefreshTask(Context cxt, Refreshable refreshable) {
    super(cxt);
    this.refreshable = refreshable;
  }

  @Override
  protected void onSucceed() {
    this.refreshable.refresh();
  }
}
