package com.avoscloud.chat.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.avoscloud.chat.R;
import com.avoscloud.chat.base.App;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Utils {

  public static void toast(Context context, String str) {
    Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
  }

  public static AlertDialog.Builder getBaseDialogBuilder(Activity activity, String s) {
    return getBaseDialogBuilder(activity).setMessage(s);
  }

  public static int getWindowWidth(Activity cxt) {
    int width;
    DisplayMetrics metrics = cxt.getResources().getDisplayMetrics();
    width = metrics.widthPixels;
    return width;
  }

  public static long getLongByTimeStr(String begin) throws ParseException {
    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SS");
    String origin = "00:00:00.00";
    Date parse = format.parse(begin);
    return parse.getTime() - format.parse(origin).getTime();
  }

  public static String getEquation(int finalNum, int delta) {
    String equation;
    int abs = Math.abs(delta);
    if (delta >= 0) {
      equation = String.format("%d+%d=%d", finalNum - delta, abs, finalNum);
    } else {
      equation = String.format("%d-%d=%d", finalNum - delta, abs, finalNum);
    }
    return equation;
  }

  public static Uri getCacheUri(String path, String url) {
    Uri uri = Uri.parse(url);
    uri = Uri.parse("cache:" + path + ":" + uri.toString());
    return uri;
  }

  public static void showInfoDialog(Activity cxt, String msg, String title) {
    AlertDialog.Builder builder = getBaseDialogBuilder(cxt);
    builder.setMessage(msg)
        .setPositiveButton(cxt.getString(R.string.chat_utils_right), null)
        .setTitle(title)
        .show();
  }

  public static AlertDialog.Builder getBaseDialogBuilder(Activity ctx) {
    return new AlertDialog.Builder(ctx).setTitle(R.string.chat_utils_tips).setIcon(R.drawable.utils_icon_info_2);
  }

  public static String getStrByRawId(Context ctx, int id) throws UnsupportedEncodingException {
    InputStream is = ctx.getResources().openRawResource(id);
    BufferedReader br = new BufferedReader(new InputStreamReader(is, "gbk"));
    String line;
    StringBuilder sb = new StringBuilder();
    try {
      while ((line = br.readLine()) != null) {
        sb.append(line + "\n");
      }
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return sb.toString();
  }

  public static void showInfoDialog(Activity cxt, int msgId, int titleId) {
    showInfoDialog(cxt, cxt.getString(msgId), cxt.getString(titleId));
  }

  public static void notifyMsg(Context cxt, Class<?> toClz, int titleId, int msgId, int notifyId) {
    notifyMsg(cxt, toClz, cxt.getString(titleId), null, cxt.getString(msgId), notifyId);
  }

  public static String getTodayDayStr() {
    String dateStr;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    dateStr = sdf.format(new Date());
    return dateStr;
  }

  public static Ringtone getDefaultRingtone(Context ctx, int type) {

    return RingtoneManager.getRingtone(ctx,
        RingtoneManager.getActualDefaultRingtoneUri(ctx, type));

  }

  public static Uri getDefaultRingtoneUri(Context ctx, int type) {
    return RingtoneManager.getActualDefaultRingtoneUri(ctx, type);
  }

  public static boolean isEmpty(Activity activity, String str, String prompt) {
    if (str.isEmpty()) {
      toast(activity, prompt);
      return true;
    }
    return false;
  }

  public static String getWifiMac(Context cxt) {
    WifiManager wm = (WifiManager) cxt.getSystemService(Context.WIFI_SERVICE);
    return wm.getConnectionInfo().getMacAddress();
  }

  public static String quote(String str) {
    return "'" + str + "'";
  }

  public static String formatString(Context cxt, int id, Object... args) {
    return String.format(cxt.getString(id), args);
  }

  public static void notifyMsg(Context context, Class<?> clz, String title, String ticker, String msg, int notifyId) {
    int icon = context.getApplicationInfo().icon;
    PendingIntent pend = PendingIntent.getActivity(context, 0,
        new Intent(context, clz), 0);
    Notification.Builder builder = new Notification.Builder(context);
    if (ticker == null) {
      ticker = msg;
    }
    builder.setContentIntent(pend)
        .setSmallIcon(icon)
        .setWhen(System.currentTimeMillis())
        .setTicker(ticker)
        .setContentTitle(title)
        .setContentText(msg)
        .setAutoCancel(true);
    NotificationManager man = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    man.notify(notifyId, builder.getNotification());
  }

  public static void sleep(int partMilli) {
    try {
      Thread.sleep(partMilli);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void setLayoutTopMargin(View view, int topMargin) {
    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)
        view.getLayoutParams();
    lp.topMargin = topMargin;
    view.setLayoutParams(lp);
  }

  public static List<?> getCopyList(List<?> ls) {
    List<?> l = new ArrayList(ls);
    return l;
  }

  public static void fixAsyncTaskBug() {
    // android bug
    new AsyncTask<Void, Void, Void>() {

      @Override
      protected Void doInBackground(Void... params) {
        return null;
      }
    }.execute();
  }

  public static void openUrl(Context context, String url) {
    Intent i = new Intent(Intent.ACTION_VIEW);
    i.setData(Uri.parse(url));
    context.startActivity(i);
  }

  public static Bitmap getCopyBitmap(Bitmap original) {
    Bitmap copy = Bitmap.createBitmap(original.getWidth(),
        original.getHeight(), original.getConfig());
    Canvas copiedCanvas = new Canvas(copy);
    copiedCanvas.drawBitmap(original, 0f, 0f, null);
    return copy;
  }

  public static Bitmap getEmptyBitmap(int w, int h) {
    return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
  }

  public static void intentShare(Context context, String title, String shareContent) {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.chat_utils_share));
    intent.putExtra(Intent.EXTRA_TEXT, shareContent);
    intent.putExtra(Intent.EXTRA_TITLE, title);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.chat_utils_please_choose)));
  }

  public static void toast(int id) {
    toast(App.ctx, id);
  }

  public static void toast(String s) {
    toast(App.ctx, s);
  }

  public static void toast(String s, String exceptionMsg) {
    if (App.debug) {
      s = s + exceptionMsg;
    }
    toast(s);
  }

  public static void toast(int resId, String exceptionMsg) {
    String s = App.ctx.getString(resId);
    toast(s, exceptionMsg);
  }

  public static void toast(Context cxt, int id) {
    Toast.makeText(cxt, id, Toast.LENGTH_SHORT).show();
  }

  public static void toastLong(Context cxt, int id) {
    Toast.makeText(cxt, id, Toast.LENGTH_LONG).show();
  }

  public static ProgressDialog showHorizontalDialog(Activity activity) {
    //activity = modifyDialogContext(activity);
    ProgressDialog dialog = new ProgressDialog(activity);
    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    dialog.setCancelable(true);
    if (activity.isFinishing() == false) {
      dialog.show();
    }
    return dialog;
  }

  public static int currentSecs() {
    int l;
    l = (int) (new Date().getTime() / 1000);
    return l;
  }

  public static String md5(String string) {
    byte[] hash = null;
    try {
      hash = string.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Huh,UTF-8 should be supported?", e);
    }
    return computeMD5(hash);
  }

  public static String computeMD5(byte[] input) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(input, 0, input.length);
      byte[] md5bytes = md.digest();

      StringBuffer hexString = new StringBuffer();
      for (int i = 0; i < md5bytes.length; i++) {
        String hex = Integer.toHexString(0xff & md5bytes[i]);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean doubleEqual(double a, double b) {
    return Math.abs(a - b) < 1E-8;
  }

  public static String getPrettyDistance(double distance) {
    if (distance < 1000) {
      int metres = (int) distance;
      return String.valueOf(metres) + App.ctx.getString(R.string.discover_metres);
    } else {
      String num = String.format("%.1f", distance / 1000);
      return num + App.ctx.getString(R.string.utils_kilometres);
    }
  }

  public static ProgressDialog showSpinnerDialog(Activity activity) {
    //activity = modifyDialogContext(activity);
    ProgressDialog dialog = new ProgressDialog(activity);
    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    dialog.setCancelable(true);
    dialog.setMessage(App.ctx.getString(R.string.chat_utils_hardLoading));
    if (!activity.isFinishing()) {
      dialog.show();
    }
    return dialog;
  }

  public static boolean filterException(Exception e) {
    if (e != null) {
      toast(e.getMessage());
      return false;
    } else {
      return true;
    }
  }

  public static void closeQuietly(Closeable closeable) {
    try {
      closeable.close();
    } catch (Exception e) {
    }
  }

}
