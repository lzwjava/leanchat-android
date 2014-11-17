package com.avoscloud.chat.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import com.avoscloud.chat.ui.view.HeaderLayout;
import com.avoscloud.chat.R;

public class BaseFragment extends Fragment {
  HeaderLayout headerLayout;
  Context ctx;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    ctx = getActivity();
    headerLayout = (HeaderLayout) getView().findViewById(R.id.headerLayout);
  }
}
