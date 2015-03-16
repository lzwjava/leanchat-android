package com.avoscloud.chat.ui.activity;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import com.avoscloud.chat.R;
import com.avoscloud.chat.service.PreferenceMap;

/**
 * Created by lzw on 14-9-24.
 */
public class NotifySettingFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
  public static final String NOTIFY_WHEN_NEWS = "notifyWhenNews";
  public static final String VOICE_NOTIFY = "voiceNotify";
  public static final String VIBRATE_NOTIFY = "vibrateNotify";

  PreferenceMap preferenceMap;
  CheckBoxPreference notifyWhenNews, voiceNotify, vibrateNotify;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.pref);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    preferenceMap = PreferenceMap.getCurUserPrefDao(getActivity());
    notifyWhenNews = (CheckBoxPreference) findPreference(NOTIFY_WHEN_NEWS);
    voiceNotify = (CheckBoxPreference) findPreference(VOICE_NOTIFY);
    vibrateNotify = (CheckBoxPreference) findPreference(VIBRATE_NOTIFY);

    notifyWhenNews.setOnPreferenceChangeListener(this);
    voiceNotify.setOnPreferenceChangeListener(this);
    vibrateNotify.setOnPreferenceChangeListener(this);
  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    String key = preference.getKey();
    boolean value = (Boolean) newValue;
    if (key.equals(NOTIFY_WHEN_NEWS)) {
      preferenceMap.setNotifyWhenNews(value);
    } else if (key.equals(VOICE_NOTIFY)) {
      preferenceMap.setVoiceNotify(value);
    } else if (key.equals(VIBRATE_NOTIFY)) {
      preferenceMap.setVibrateNotify(value);
    }
    return true;
  }
}
