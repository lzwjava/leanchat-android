package com.lzw.talk.view.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import com.lzw.talk.R;
import com.lzw.talk.view.HeaderLayout;

/**
 * Created by lzw on 14-9-17.
 */
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
