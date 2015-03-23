package com.avoscloud.chat.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.avoscloud.chat.R;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.util.Logger;
import com.avoscloud.chat.util.Utils;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.*;

public class LocationActivity extends BaseActivity implements
    OnGetGeoCoderResultListener {
  public static final int SEND = 0;
  public static final String TYPE = "type";
  public static final String TYPE_SELECT = "select";
  public static final String LATITUDE = "latitude";
  public static final String LONGITUDE = "longitude";
  public static final String TYPE_SCAN = "scan";
  public static final String ADDRESS = "address";
  static BDLocation lastLocation = null;
  private LocationClient locClient;
  private MyLocationListener myListener = new MyLocationListener();
  private MapView mapView;
  private BaiduMap baiduMap;
  private BaiduReceiver receiver;
  private GeoCoder geoCoder = null;
  private BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);

  private String intentType;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.chat_location_activity);
    initBaiduMap();
  }

  private void initBaiduMap() {
    mapView = (MapView) findViewById(R.id.bmapView);
    baiduMap = mapView.getMap();
    baiduMap.setMaxAndMinZoomLevel(18, 13);
    IntentFilter iFilter = new IntentFilter();
    iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
    iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
    receiver = new BaiduReceiver();
    registerReceiver(receiver, iFilter);

    geoCoder = GeoCoder.newInstance();
    geoCoder.setOnGetGeoCodeResultListener(this);

    Intent intent = getIntent();
    intentType = intent.getStringExtra(TYPE);
    initActionBar(R.string.position);
    if (intentType.equals(TYPE_SELECT)) {// 选择发送位置
      //		 开启定位图层
      baiduMap.setMyLocationEnabled(true);
      baiduMap.setMyLocationConfigeration(new MyLocationConfigeration(
          MyLocationConfigeration.LocationMode.NORMAL, true, null));
      // 定位初始化
      locClient = new LocationClient(this);
      locClient.registerLocationListener(myListener);
      LocationClientOption option = new LocationClientOption();
      option.setProdName("avosim");
      option.setOpenGps(true);
      option.setCoorType("bd09ll");
      option.setScanSpan(1000);
      option.setOpenGps(true);
      option.setIsNeedAddress(true);
      option.setIgnoreKillProcess(true);
      locClient.setLocOption(option);
      locClient.start();
      if (locClient != null && locClient.isStarted()) {
        locClient.requestLocation();
      }
      if (lastLocation != null) {
        // 显示在地图上
        LatLng ll = new LatLng(lastLocation.getLatitude(),
            lastLocation.getLongitude());
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
        baiduMap.animateMapStatus(u);
      }
    } else {
      Bundle b = intent.getExtras();
      LatLng latlng = new LatLng(b.getDouble(LATITUDE), b.getDouble(LONGITUDE));//维度在前，经度在后
      baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(latlng));
      OverlayOptions ooA = new MarkerOptions().position(latlng).icon(descriptor).zIndex(9);
      baiduMap.addOverlay(ooA);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (intentType != null && intentType.equals(TYPE_SELECT)) {
      MenuItem add = menu.add(0, SEND, 0, R.string.send);
      alwaysShowMenuItem(add);
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    int id = item.getItemId();
    if (id == SEND) {
      gotoChatPage();
    }
    return super.onMenuItemSelected(featureId, item);
  }

  private void gotoChatPage() {
    if (lastLocation != null) {
      Intent intent = new Intent();
      intent.putExtra(LATITUDE, lastLocation.getLatitude());// 经度
      intent.putExtra(LONGITUDE, lastLocation.getLongitude());// 维度
      intent.putExtra(ADDRESS, lastLocation.getAddrStr());
      setResult(RESULT_OK, intent);
      this.finish();
    } else {
      Utils.toast(App.ctx, R.string.getGeoInfoFailed);
    }
  }

  @Override
  public void onGetGeoCodeResult(GeoCodeResult arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
    // TODO Auto-generated method stub
    if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
      Utils.toast(ctx, App.ctx.getString(R.string.cannotFindResult));
      return;
    }
    Logger.d(App.ctx.getString(R.string.reverseGeoCodeResultIs) + result.getAddress());
    lastLocation.setAddrStr(result.getAddress());
  }

  @Override
  protected void onPause() {
    mapView.onPause();
    super.onPause();
    lastLocation = null;
  }

  @Override
  protected void onResume() {
    mapView.onResume();
    super.onResume();
  }

  @Override
  protected void onDestroy() {
    if (locClient != null && locClient.isStarted()) {
      // 退出时销毁定位
      locClient.stop();
    }
    // 关闭定位图层
    baiduMap.setMyLocationEnabled(false);
    mapView.onDestroy();
    mapView = null;
    // 取消监听 SDK 广播
    unregisterReceiver(receiver);
    super.onDestroy();
    // 回收 bitmap 资源
    descriptor.recycle();
  }

  /**
   * 定位SDK监听函数
   */
  public class MyLocationListener implements BDLocationListener {

    @Override
    public void onReceiveLocation(BDLocation location) {
      // map view 销毁后不在处理新接收的位置
      if (location == null || mapView == null)
        return;

      if (lastLocation != null) {
        if (lastLocation.getLatitude() == location.getLatitude()
            && lastLocation.getLongitude() == location
            .getLongitude()) {
          Logger.d(App.ctx.getString(R.string.geoIsSame));// 若两次请求获取到的地理位置坐标是相同的，则不再定位
          locClient.stop();
          return;
        }
      }
      lastLocation = location;

      Logger.d("lontitude = " + location.getLongitude() + ",latitude = "
          + location.getLatitude() + "," + App.ctx.getString(R.string.position) + " = "
          + lastLocation.getAddrStr());

      MyLocationData locData = new MyLocationData.Builder()
          .accuracy(location.getRadius())
              // 此处设置开发者获取到的方向信息，顺时针0-360
          .direction(100).latitude(location.getLatitude())
          .longitude(location.getLongitude()).build();
      baiduMap.setMyLocationData(locData);
      LatLng ll = new LatLng(location.getLatitude(),
          location.getLongitude());
      String address = location.getAddrStr();
      if (address != null && !address.equals("")) {
        lastLocation.setAddrStr(address);
      } else {
        // 反Geo搜索
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(ll));
      }
      // 显示在地图上
      MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
      baiduMap.animateMapStatus(u);
      //设置按钮可点击
    }
  }

  /**
   * 构造广播监听类，监听 SDK key 验证以及网络异常广播
   */
  public class BaiduReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
      String s = intent.getAction();
      if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
        Utils.toast(ctx, App.ctx.getString(R.string.mapKeyErrorTips));
      } else if (s
          .equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
        Utils.toast(ctx, App.ctx.getString(R.string.badNetwork));
      }
    }
  }

}
