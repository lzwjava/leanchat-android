package com.avoscloud.chat.service;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avoscloud.chat.R;
import com.avoscloud.chat.avobject.UpdateInfo;
import com.avoscloud.chat.util.Logger;
import com.avoscloud.chat.util.NetAsyncTask;
import com.avoscloud.chat.util.Utils;

import java.util.List;

/**
 * Created by lzw on 14-6-24.
 */
public class UpdateService {
  private static final String LAST_VERSION = "lastVersion";
  private static final String PROMTED_UPDATE = "promtedUpdate";
  Activity activity;
  static UpdateService updateService;
  SharedPreferences pref;
  SharedPreferences.Editor editor;

  private UpdateService(Activity activity) {
    this.activity = activity;
    pref = PreferenceManager.getDefaultSharedPreferences(activity);
    editor = pref.edit();
  }

  public static UpdateService getInstance(Activity ctx) {
    if (updateService == null) {
      updateService = new UpdateService(ctx);
    }
    return updateService;
  }

  public void openUrlInBrowser(String url) {
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    activity.startActivity(intent);
  }

  public static int getVersionCode(Context ctx) {
    int versionCode = 0;
    try {
      versionCode = ctx.getPackageManager().getPackageInfo(
          ctx.getPackageName(), 0).versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return versionCode;
  }

  public static String getVersionName(Context ctx) {
    String versionName = null;
    try {
      versionName = ctx.getPackageManager().getPackageInfo(
          ctx.getPackageName(), 0).versionName;
    } catch (PackageManager.NameNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return versionName;
  }

  public void checkUpdate() {
    //setPromptedUpdate(false);
    new UpdateTask(activity, false, AVQuery.CachePolicy.NETWORK_ELSE_CACHE) {
      @Override
      public void done(final UpdateInfo info, Exception e) {
        if (e == null) {
          int ver = info.getVersion();
          int curVer = UpdateService.getVersionCode(ctx);
          //Logger.d("info url=" + info.getApkUrl());
          if (curVer < ver) {
            if (isPromptedUpdate() == false) {
              setPromptedUpdate(true);
              AlertDialog.Builder builder = new AlertDialog.Builder(UpdateService.this.activity);
              builder.setTitle(R.string.haveNewVersion)
                  .setMessage(info.getDesc())
                  .setPositiveButton(R.string.installNewVersion, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      openUrlInBrowser(info.getApkUrl());
                    }
                  }).setNegativeButton(R.string.laterNotify, null).show();
            }
          } else {
            if (curVer == ver) {
              int lastVer = getLastVersion();
              boolean firstRunThisVersion = lastVer < curVer;
              if (firstRunThisVersion) {
                setPromptedUpdate(false);
                setLastVersion(curVer);
                String msg = info.getDesc();
                String title = UpdateService.this.activity.getString(R.string.updateLog);
                Utils.showInfoDialog(UpdateService.this.activity, msg, title);
              }
            }
          }
        }
      }
    }.execute();
  }

  private int getLastVersion() {
    return pref.getInt(LAST_VERSION, 0);
  }

  private void setLastVersion(int lastVersion) {
    editor.putInt(LAST_VERSION, lastVersion).commit();
  }

  private boolean isPromptedUpdate() {
    return pref.getBoolean(PROMTED_UPDATE, false);
  }

  private void setPromptedUpdate(boolean promptedUpdate) {
    editor.putBoolean(PROMTED_UPDATE, promptedUpdate).commit();
  }

  public void showSureUpdateDialog() {
    new UpdateTask(activity, true, AVQuery.CachePolicy.NETWORK_ELSE_CACHE) {
      @Override
      public void done(final UpdateInfo info, Exception e) {
        if (e == null) {
          if (info.getVersion() > getVersionCode(ctx)) {
            AlertDialog.Builder builder = Utils.getBaseDialogBuilder((Activity) ctx);
            builder.setTitle(R.string.sureToUpdate)
                .setMessage(info.getDesc())
                .setPositiveButton(R.string.right, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    openUrlInBrowser(info.getApkUrl());
                  }
                }).setNegativeButton(R.string.cancel, null).show();
          } else {
            Utils.toast(ctx, R.string.versionIsAlreadyNew);
          }
        } else {
          e.printStackTrace();
          Utils.toast(ctx, R.string.failedToGetData);
        }
      }
    }.execute();
  }

  public static void createUpdateInfoInBackground() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          createUpdateInfo();
        } catch (AVException e) {
          e.printStackTrace();
        }
        Logger.d("createUpdateInfo");
      }
    }).start();
  }

  public static void createUpdateInfo() throws AVException {
    UpdateInfo updateInfo = new UpdateInfo();
    updateInfo.setVersion(1);
    updateInfo.setAppName("appName");
    updateInfo.setApkUrl("https://leancloud.cn");
    updateInfo.setDesc("desc");
    updateInfo.save();
  }

  public abstract static class UpdateTask extends NetAsyncTask {
    UpdateInfo info;
    AVQuery.CachePolicy policy;

    protected UpdateTask(Context ctx, boolean openDialog, AVQuery.CachePolicy policy) {
      super(ctx, openDialog);
      this.policy = policy;
    }

    @Override
    protected void doInBack() throws Exception {
      info = getNewestUpdateInfo();
      if (info == null) {
        throw new NullPointerException("not found any update info");
      }
    }

    private UpdateInfo getNewestUpdateInfo() throws AVException {
      AVQuery<UpdateInfo> query = AVObject.getQuery(UpdateInfo.class);
      query.setLimit(1);
      query.orderByDescending(UpdateInfo.VERSION);
      if (policy != null) {
        query.setCachePolicy(policy);
      }
      List<UpdateInfo> updateInfos = query.find();
      if (updateInfos.size() > 0) {
        return updateInfos.get(0);
      }
      return null;
    }

    @Override
    protected void onPost(Exception e) {
      if (e != null) {
        done(null, e);
      } else {
        done(info, null);
      }
    }

    public abstract void done(UpdateInfo info, Exception e);
  }
}
