package com.avoscloud.chat.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by lzw on 14-9-25.
 */
public class BasePagerAdapter extends PagerAdapter {
  List<View> views;

  public BasePagerAdapter(List<View> views) {
    this.views = views;
  }

  @Override
  public int getCount() {
    return views.size();
  }

  @Override
  public boolean isViewFromObject(View view, Object o) {
    return view == o;
  }

  @Override
  public Object instantiateItem(ViewGroup container, int position) {
    View view = views.get(position);
    container.addView(view);
    return view;
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    container.removeView(views.get(position));
  }
}
