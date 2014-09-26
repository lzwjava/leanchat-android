package com.lzw.talk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-9-25.
 */
public class BaseListAdpter<T> extends BaseAdapter {
  Context ctx;
  List<T> datas = new ArrayList<T>();

  public BaseListAdpter(Context ctx) {
    this.ctx = ctx;
  }

  public void setDatas(List<T> datas) {
    this.datas = datas;
  }

  @Override
  public int getCount() {
    return datas.size();
  }

  @Override
  public Object getItem(int position) {
    return datas.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    return null;
  }
}
