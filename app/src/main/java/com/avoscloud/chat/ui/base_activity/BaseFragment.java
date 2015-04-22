package com.avoscloud.chat.ui.base_activity;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import com.avoscloud.chat.R;
import com.avoscloud.chat.ui.view.HeaderLayout;

public class BaseFragment extends Fragment {
  protected HeaderLayout headerLayout;
  protected Context ctx;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    ctx = getActivity();
    headerLayout = (HeaderLayout) getView().findViewById(R.id.headerLayout);
  }
}
