package com.avoscloud.chat.util;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.R;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {
  public static BufferedReader bufferedReader(String url) throws IOException,
      ClientProtocolException, UnsupportedEncodingException {
    HttpGet get = new HttpGet(url);
    DefaultHttpClient client = new DefaultHttpClient();
    HttpResponse response = client.execute(get);
    HttpEntity entity = response.getEntity();
    InputStream stream = entity.getContent();
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream,
        "GBK"));
    return reader;
  }

  public static String readFile(String path) throws FileNotFoundException,
      IOException {
    InputStream input = new FileInputStream(new File(path));
    DataInputStream dataInput = new DataInputStream(input);
    byte[] bytes = new byte[input.available()];
    dataInput.readFully(bytes);
    String text = new String(bytes);
    input.close();
    dataInput.close();
    return text;
  }

  public static Bitmap urlToBitmap(String url) throws ClientProtocolException,
      IOException {
    return BitmapFactory.decodeStream(inputStreamFromUrl(url));
  }

  public static InputStream inputStreamFromUrl(String url) throws IOException,
      ClientProtocolException {
    DefaultHttpClient client = new DefaultHttpClient();
    HttpGet get = new HttpGet(url);
    HttpResponse response = client.execute(get);
    HttpEntity entity = response.getEntity();
    InputStream stream = entity.getContent();
    return stream;
  }

  public static Bitmap bitmapFromFile(File file) throws FileNotFoundException {
    return BitmapFactory.decodeStream(new BufferedInputStream(
        new FileInputStream(file)));
  }

  public static void inputToOutput(FileOutputStream outputStream,
                                   InputStream inputStream) throws IOException {
    byte[] buffer = new byte[1024];
    int len;
    while ((len = inputStream.read(buffer)) != -1) {
      outputStream.write(buffer, 0, len);
    }
    outputStream.close();
    inputStream.close();
  }

  public static byte[] readStream(InputStream inStream) throws Exception {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int len = 0;
    while ((len = inStream.read(buffer)) != -1) {
      outStream.write(buffer, 0, len);
    }
    outStream.close();
    inStream.close();
    return outStream.toByteArray();
  }

  public static byte[] getBytesFromUrl(String url) throws Exception {
    InputStream in = inputStreamFromUrl(url);
    return readStream(in);
  }

  public static Bitmap saveBitmapLocal(String bitmapUrl, File bitmapFile)
      throws IOException, FileNotFoundException, ClientProtocolException {
    Bitmap resultBitmap;
    downloadFileIfNotExists(bitmapUrl, bitmapFile);
    resultBitmap = Utils.bitmapFromFile(bitmapFile);
    return resultBitmap;
  }

  public static void downloadFileIfNotExists(String url, File toFile) throws IOException {
    if (toFile.exists()) {
    } else {
      downloadFile(url, toFile);
    }
  }

  public static void downloadFile(String url, File toFile) throws IOException {
    toFile.createNewFile();
    FileOutputStream outputStream = new FileOutputStream(toFile);
    InputStream inputStream = Utils.inputStreamFromUrl(url);
    Utils.inputToOutput(outputStream, inputStream);
  }

  public static Bitmap getBitmapFromUrl(String logoUrl, String filmEnName,
                                        String appPath) throws IOException, FileNotFoundException,
      ClientProtocolException {
    Bitmap resultBitmap;
    String logoPath = appPath + "logo/";
    File dir = new File(logoPath);
    if (dir.exists() == false) {
      dir.mkdirs();
    }
    File logoLocalFile = new File(logoPath + filmEnName + ".jpg");
    resultBitmap = Utils.saveBitmapLocal(logoUrl, logoLocalFile);
    return resultBitmap;
  }

  public static void bytesToFile(final File file, byte[] bytes)
      throws FileNotFoundException, IOException {
    FileOutputStream output = new FileOutputStream(file);
    output.write(bytes);
    output.close();
  }

  public static void toastCheckNetwork(Context context) {
    toastIt(context, R.string.pleaseCheckNetwork, false);
  }


  public static void toast(Context context, String str) {
    Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
  }

  private static void toastIt(Context context, int strId, boolean isLong) {
    int ti;
    if (isLong) ti = Toast.LENGTH_LONG;
    else ti = Toast.LENGTH_SHORT;
    Toast.makeText(context, context.getString(strId), ti).show();
  }

  public static boolean hasSDcard() {
    // TODO Auto-generated method stub
    return Environment.MEDIA_MOUNTED.equals(Environment
        .getExternalStorageState());
  }

  public static int dip2px(Context context, float dipValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dipValue * scale + 0.5f);
  }


  public static String format(int curPos) {
    int sec1 = curPos / 1000;
    int min = sec1 / 60;
    int sec = sec1 % 60;
    int tm = min / 10, mm = min % 10;
    int ts = sec / 10, ms = sec % 10;
    String str = String.format("%d%d:%d%d", tm, mm, ts, ms);
    return str;
  }

  public static void clearDir(String dir) {
    File file = new File(dir);
    File[] fs = file.listFiles();
    for (File f : fs) {
      f.delete();
    }
  }

  public static String prettyFormat(Date date) {
    String dateStr;
    String am_pm = "am";
    SimpleDateFormat format = new SimpleDateFormat("M-d h");
    SimpleDateFormat format1 = new SimpleDateFormat("aa", Locale.ENGLISH);
    if (format1.format(date).equals("PM")) {
      am_pm = "pm";
    }
    dateStr = format.format(date) + am_pm;
    return dateStr;
  }

  public static void alertDialog(Activity activity, String s) {
    new AlertDialog.Builder(activity).setMessage(s).show();
  }

  public static void alertDialog(Activity activity, int msgId) {
    new AlertDialog.Builder(activity).
        setMessage(activity.getString(msgId)).show();
  }

  public static void alertIconDialog(Activity activity, String s, int iconId) {
    getBaseDialogBuilder(activity, s).show();
  }

  public static void alertIconDialog(Activity activity, int sId) {
    getBaseDialogBuilder(activity, activity.getString(sId)).show();
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

  public static long getPassTime(long st) {
    return System.currentTimeMillis() - st;
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

  public static void notify(Context context, String msg, String title, Class<?> toClz, int notifyId) {
    PendingIntent pend = PendingIntent.getActivity(context, 0,
        new Intent(context, toClz), 0);
    Notification.Builder builder = new Notification.Builder(context);
    int icon = context.getApplicationInfo().icon;
    builder.setContentIntent(pend)
        .setSmallIcon(icon)
        .setWhen(System.currentTimeMillis())
        .setTicker(msg)
        .setContentTitle(title)
        .setContentText(msg)
        .setAutoCancel(true);

    NotificationManager man = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    man.notify(notifyId, builder.getNotification());
  }

  public static void cancelNotification(Context ctx, int notifyId) {
    String ns = Context.NOTIFICATION_SERVICE;
    NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
    nMgr.cancel(notifyId);
  }

  public static String getStringByFile(File f) throws IOException {
    StringBuilder builder = new StringBuilder();
    BufferedReader br = new BufferedReader(new FileReader(f));
    String line;
    while ((line = br.readLine()) != null) {
      builder.append(line);
    }
    br.close();
    return builder.toString();
  }

  public static String getShortUrl(String longUrl) throws IOException, JSONException {
    if (longUrl.startsWith("http") == false) {
      throw new IllegalArgumentException("longUrl must start with http");
    }
    String url = "https://api.weibo.com/2/short_url/shorten.json";
    HttpPost post = new HttpPost(url);
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("access_token", "2.00_hkjqBR1dbuCc632289355qerfeD"));
    params.add(new BasicNameValuePair("url_long", longUrl));
    post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
    HttpResponse res = new DefaultHttpClient().execute(post);
    if (res.getStatusLine().getStatusCode() == 200) {
      String str = EntityUtils.toString(res.getEntity());
      JSONObject json = new JSONObject(str);
      JSONArray arr = json.getJSONArray("urls");
      JSONObject urls = arr.getJSONObject(0);
      if (urls.getBoolean("result")) {
        return urls.getString("url_short");
      } else {
        return null;
      }
    }
    return null;
  }

  public static String getGb2312Encode(String s) throws UnsupportedEncodingException {
    return URLEncoder.encode(s, "gb2312");
  }

  public static void goActivity(Context cxt, Class<?> clz) {
    Intent intent = new Intent(cxt, clz);
    cxt.startActivity(intent);
  }

  public static void installApk(Context context, String path) {
    Intent intent1 = new Intent();
    intent1.setAction(Intent.ACTION_VIEW);
    File file = new File(path);
    Log.i("lzw", file.getAbsolutePath());
    intent1.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent1);
  }

  public static void showInfoDialog(Activity cxt, String msg, String title) {
    AlertDialog.Builder builder = getBaseDialogBuilder(cxt);
    builder.setMessage(msg)
        .setPositiveButton(cxt.getString(R.string.right), null)
        .setTitle(title)
        .show();
  }

  public static Activity modifyDialogContext(Activity cxt) {
    Activity parent = cxt.getParent();
    if (parent != null) {
      return parent;
    } else {
      return cxt;
    }
  }

  public static AlertDialog.Builder getBaseDialogBuilder(Activity ctx) {
    return new AlertDialog.Builder(ctx).setTitle(R.string.tips).setIcon(R.drawable.icon_info_2);
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
    new AsyncTask<Void, Void, Void>() {

      @Override
      protected Void doInBackground(Void... params) {
        return null;
      }
    }.execute();
  }

  public static String getPhoneNum(Context cxt) {
    TelephonyManager tm = (TelephonyManager) cxt.getSystemService(Context.TELEPHONY_SERVICE);
    String deviceid = tm.getDeviceId();
    String tel = tm.getLine1Number();
    String imei = tm.getSimSerialNumber();
    String imsi = tm.getSubscriberId();
    Logger.d("tel=" + tel + "  " + imei + " " + imei);
    return tel;
  }

  public static void goActivityAndFinish(Activity cxt, Class<?> clz) {
    Intent intent = new Intent(cxt, clz);
    cxt.startActivity(intent);
    cxt.finish();
  }

  public static String getRealPathFromURI(Context context, Uri contentUri) {
    Cursor cursor = null;
    try {
      String[] proj = {MediaStore.Images.Media.DATA};
      cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
      int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
      cursor.moveToFirst();
      return cursor.getString(column_index);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
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
    intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share));
    intent.putExtra(Intent.EXTRA_TEXT, shareContent);
    intent.putExtra(Intent.EXTRA_TITLE, title);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.please_choose)));
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

  public static ProgressDialog showSpinnerDialog(Activity activity) {
    //activity = modifyDialogContext(activity);

    ProgressDialog dialog = new ProgressDialog(activity);
    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    dialog.setCancelable(true);
    dialog.setMessage(App.ctx.getString(R.string.hardLoading));
    if (activity.isFinishing() == false) {
      dialog.show();
    }
    return dialog;
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

  public static String uuid() {
    //return UUID.randomUUID().toString().substring(0, 24);
    return myUUID();
  }

  public static String myUUID() {
    StringBuilder sb = new StringBuilder();
    int start = 48, end = 58;
    appendChar(sb, start, end);
    appendChar(sb, 65, 90);
    appendChar(sb, 97, 123);
    String charSet = sb.toString();
    StringBuilder sb1 = new StringBuilder();
    for (int i = 0; i < 24; i++) {
      int len = charSet.length();
      int pos = new Random().nextInt(len);
      sb1.append(charSet.charAt(pos));
    }
    return sb1.toString();
  }

  public static void appendChar(StringBuilder sb, int start, int end) {
    int i;
    for (i = start; i < end; i++) {
      sb.append((char) i);
    }
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

  public static boolean isListNotEmpty(Collection<?> collection) {
    if (collection != null && collection.size() > 0) {
      return true;
    }
    return false;
  }

  public static Map<String, User> list2map(List<User> users) {
    Map<String, User> friends = new HashMap<String, User>();
    for (User user : users) {
      friends.put(user.getUsername(), user);
    }
    return friends;
  }

  public static List<User> map2list(Map<String, User> maps) {
    List<User> users = new ArrayList<User>();
    Iterator<Map.Entry<String, User>> iterator = maps.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, User> entry = iterator.next();
      users.add(entry.getValue());
    }
    return users;
  }

  public static int getColor(int resId) {
    return App.ctx.getResources().getColor(resId);
  }

  public static void hideSoftInputView(Activity activity) {
    if (activity.getWindow().getAttributes().softInputMode !=
        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
      InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
      View currentFocus = activity.getCurrentFocus();
      if (currentFocus != null) {
        manager.hideSoftInputFromWindow(currentFocus.getWindowToken(),
            InputMethodManager.HIDE_NOT_ALWAYS);
      }
    }
  }

  public static boolean doubleEqual(double a, double b) {
    return Math.abs(a - b) < 1E-8;
  }

  public static String getPrettyDistance(double distance) {
    if (distance < 1000) {
      int metres = (int) distance;
      return String.valueOf(metres) + App.ctx.getString(R.string.metres);
    } else {
      String num = String.format("%.1f", distance / 1000);
      return num + App.ctx.getString(R.string.kilometres);
    }
  }

  public static void printException(Exception e) {
    if (App.debug) {
      e.printStackTrace();
    }
  }

  public static void alwaysShowMenuItem(MenuItem add) {
    add.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
        | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
  }
}
