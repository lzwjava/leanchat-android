package com.avoscloud.chat.adapter;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.avoscloud.chat.util.Logger;

import java.util.ArrayList;
import java.util.List;


public class BaseCheckListAdapter<T> extends BaseListAdapter<T> {
  List<Boolean> checkStates = new ArrayList<Boolean>();
  boolean defaultState = false;

  public BaseCheckListAdapter(Context ctx, List<T> datas) {
    super(ctx, datas);
    initCheckStates(datas);
  }

  @Override
  public void setDatas(List<T> datas) {
    super.setDatas(datas);
    initCheckStates(datas);
  }

  private void initCheckStates(List<T> datas) {
    checkStates.clear();
    for (int i = 0; i < datas.size(); i++) {
      checkStates.add(defaultState);
    }
  }

  public List<Boolean> getCheckStates() {
    return checkStates;
  }

  public List<T> getCheckedDatas() {
    List<T> checkedDatas = new ArrayList<T>();
    for (int i = 0; i < checkStates.size(); i++) {
      if (checkStates.get(i)) {
        checkedDatas.add(datas.get(i));
      }
    }
    return checkedDatas;
  }

  void checkItem(int position) {
    assertSize(position);
    if (checkStates.get(position) == false) {
      checkStates.set(position, true);
    }
  }

  void uncheckItem(int position) {
    assertSize(position);
    if (checkStates.get(position)) {
      checkStates.set(position, false);
    }
  }

  void toggleItem(int position) {
    assertSize(position);
    checkStates.set(position, !checkStates.get(position));
  }

  void setCheckState(int position, boolean state) {
    assertSize(position);
    checkStates.set(position, state);
  }

  void assertSize(int position) {
    if (position >= checkStates.size()) {
      //throw new IllegalArgumentException("position is bigger than size");
      Logger.d("illegal " + position + checkStates.size());
    }
  }

  @Override
  public void addAll(List<T> subDatas) {
    unsupport();
  }

  private void unsupport() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(T object) {
    unsupport();
  }

  @Override
  public void remove(int position) {
    unsupport();
  }

  @Override
  public void clear() {
    unsupport();
  }

  protected void setCheckBox(CheckBox checkBox, int position) {
    assertSize(position);
    checkBox.setChecked(checkStates.get(position));
  }

  class CheckListener implements CompoundButton.OnCheckedChangeListener {
    int position;

    CheckListener(int position) {
      this.position = position;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
      setCheckState(position, isChecked);
    }
  }
}
