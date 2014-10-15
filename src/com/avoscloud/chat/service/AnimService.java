package com.avoscloud.chat.service;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.R;
import com.avoscloud.chat.util.SimpleAnimationListener;

/**
 * Created by lzw on 14-10-6.
 */
public class AnimService {
  public Animation popupFromBottomAnim, hideToBottomAnim;
  Context ctx;
  public static AnimService instance;


  public AnimService(Context ctx) {
    this.ctx = ctx;
    popupFromBottomAnim = AnimationUtils.loadAnimation(App.ctx, R.anim.popup_from_bottom);
    hideToBottomAnim = AnimationUtils.loadAnimation(App.ctx, R.anim.slide_out_to_bottom);
  }

  public void hideView(final View view) {
    view.startAnimation(hideToBottomAnim);
    hideToBottomAnim.setAnimationListener(new SimpleAnimationListener() {
      @Override
      public void onAnimationEnd(Animation animation) {
        view.setVisibility(View.GONE);
      }
    });
  }

  public static AnimService getInstance() {
    if (instance == null) {
      if (App.ctx == null) {
        throw new NullPointerException("App context is null");
      }
      instance = new AnimService(App.ctx);
    }
    return instance;
  }
}
