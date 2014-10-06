package com.lzw.talk.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import com.lzw.talk.R;
import com.lzw.talk.ui.view.HeaderLayout;

public class BaseFragment extends Fragment {
  HeaderLayout headerLayout;
  Activity ctx;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    ctx = getActivity();
    headerLayout = (HeaderLayout) getView().findViewById(R.id.headerLayout);
  }
}
