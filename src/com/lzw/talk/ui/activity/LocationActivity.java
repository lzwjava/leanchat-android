package com.lzw.talk.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.*;
import com.lzw.talk.R;
import com.lzw.talk.base.App;
import com.lzw.talk.util.Logger;
import com.lzw.talk.util.Utils;
import com.lzw.talk.ui.view.HeaderLayout;

/**
 * 用于发送位置的界面
 *
 * @author smile
 * @ClassName: LocationActivity
 * @Description: TODO
 * @date 2014-6-23 下午3:17:05
 */
public class LocationActivity extends BaseActivity implements
    OnGetGeoCoderResultListener {

  // 定位相关
  LocationClient mLocClient;
  public MyLocationListenner myListener = new MyLocationListenner();
  BitmapDescriptor mCurrentMarker;

  MapView mMapView;
  BaiduMap mBaiduMap;
  HeaderLayout headerLayout;

  private BaiduReceiver mReceiver;// 注册广播接收器，用于监听网络以及验证key

  GeoCoder mSearch = null; // 搜索模块，因为百度定位sdk能够得到经纬度，但是却无法得到具体的详细地址，因此需要采取反编码方式去搜索此经纬度代表的地址

  static BDLocation lastLocation = null;

  BitmapDescriptor bdgeo = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_location);
    initBaiduMap();
  }

  private void initBaiduMap() {
    // 地图初始化
    mMapView = (MapView) findViewById(R.id.bmapView);
    headerLayout = (HeaderLayout) findViewById(R.id.headerLayout);
    mBaiduMap = mMapView.getMap();
    //设置缩放级别
    mBaiduMap.setMaxAndMinZoomLevel(18, 13);
    // 注册 SDK 广播监听者
    IntentFilter iFilter = new IntentFilter();
    iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
    iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
    mReceiver = new BaiduReceiver();
    registerReceiver(mReceiver, iFilter);

    Intent intent = getIntent();
    String type = intent.getStringExtra("type");
    if (type.equals("select")) {// 选择发送位置
      headerLayout.showTitle(R.string.position);
      headerLayout.showRightTextButton(R.string.send, new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          gotoChatPage();
        }
      });
      initLocClient();
    } else {// 查看当前位置
      headerLayout.showTitle(R.string.position);
      Bundle b = intent.getExtras();
      LatLng latlng = new LatLng(b.getDouble("latitude"), b.getDouble("longtitude"));//维度在前，经度在后
      mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(latlng));
      //显示当前位置图标
      OverlayOptions ooA = new MarkerOptions().position(latlng).icon(bdgeo).zIndex(9);
      mBaiduMap.addOverlay(ooA);
    }

    mSearch = GeoCoder.newInstance();
    mSearch.setOnGetGeoCodeResultListener(this);

  }

  /**
   * 回到聊天界面
   *
   * @param
   * @return void
   * @throws
   * @Title: gotoChatPage
   * @Description: TODO
   */
  private void gotoChatPage() {
    if (lastLocation != null) {
      Intent intent = new Intent();
      intent.putExtra("y", lastLocation.getLongitude());// 经度
      intent.putExtra("x", lastLocation.getLatitude());// 维度
      intent.putExtra("address", lastLocation.getAddrStr());
      setResult(RESULT_OK, intent);
      this.finish();
    } else {
      Utils.toast(App.ctx, R.string.getGeoInfoFailed);
    }
  }

  private void initLocClient() {
//		 开启定位图层
    mBaiduMap.setMyLocationEnabled(true);
    mBaiduMap.setMyLocationConfigeration(new MyLocationConfigeration(
        MyLocationConfigeration.LocationMode.NORMAL, true, null));
    // 定位初始化
    mLocClient = new LocationClient(this);
    mLocClient.registerLocationListener(myListener);
    LocationClientOption option = new LocationClientOption();
    option.setProdName("bmobim");// 设置产品线
    option.setOpenGps(true);// 打开gps
    option.setCoorType("bd09ll"); // 设置坐标类型
    option.setScanSpan(1000);
    option.setOpenGps(true);
    option.setIsNeedAddress(true);
    option.setIgnoreKillProcess(true);
    mLocClient.setLocOption(option);
    mLocClient.start();
    if (mLocClient != null && mLocClient.isStarted())
      mLocClient.requestLocation();

    if (lastLocation != null) {
      // 显示在地图上
      LatLng ll = new LatLng(lastLocation.getLatitude(),
          lastLocation.getLongitude());
      MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
      mBaiduMap.animateMapStatus(u);
    }
  }

  /**
   * 定位SDK监听函数
   */
  public class MyLocationListenner implements BDLocationListener {

    @Override
    public void onReceiveLocation(BDLocation location) {
      // map view 销毁后不在处理新接收的位置
      if (location == null || mMapView == null)
        return;

      if (lastLocation != null) {
        if (lastLocation.getLatitude() == location.getLatitude()
            && lastLocation.getLongitude() == location
            .getLongitude()) {
          Logger.d(App.ctx.getString(R.string.geoIsSame));// 若两次请求获取到的地理位置坐标是相同的，则不再定位
          mLocClient.stop();
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
      mBaiduMap.setMyLocationData(locData);
      LatLng ll = new LatLng(location.getLatitude(),
          location.getLongitude());
      String address = location.getAddrStr();
      if (address != null && !address.equals("")) {
        lastLocation.setAddrStr(address);
      } else {
        // 反Geo搜索
        mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(ll));
      }
      // 显示在地图上
      MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
      mBaiduMap.animateMapStatus(u);
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
    mMapView.onPause();
    super.onPause();
    lastLocation = null;
  }

  @Override
  protected void onResume() {
    mMapView.onResume();
    super.onResume();
  }

  @Override
  protected void onDestroy() {
    if (mLocClient != null && mLocClient.isStarted()) {
      // 退出时销毁定位
      mLocClient.stop();
    }
    // 关闭定位图层
    mBaiduMap.setMyLocationEnabled(false);
    mMapView.onDestroy();
    mMapView = null;
    // 取消监听 SDK 广播
    unregisterReceiver(mReceiver);
    super.onDestroy();
    // 回收 bitmap 资源
    bdgeo.recycle();
  }

}
