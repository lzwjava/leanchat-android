package com.avoscloud.chat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.avos.avoscloud.AVGeoPoint;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.service.PreferenceMap;
import com.avoscloud.chat.ui.view.ViewHolder;
import com.avoscloud.chat.R;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.util.Utils;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

public class NearPeopleAdapter extends BaseListAdapter<User> {
  PrettyTime prettyTime;
  AVGeoPoint location;

  public NearPeopleAdapter(Context ctx) {
    super(ctx);
    init();
  }

  private void init() {
    prettyTime = new PrettyTime();
    PreferenceMap preferenceMap = PreferenceMap.getCurUserPrefDao(ctx);
    location = preferenceMap.getLocation();
  }

  public NearPeopleAdapter(Context ctx, List<User> datas) {
    super(ctx, datas);
    init();
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.discover_near_people_item, null, false);
    }
    final User user = datas.get(position);
    TextView nameView = ViewHolder.findViewById(convertView, R.id.name_text);
    TextView distanceView = ViewHolder.findViewById(convertView, R.id.distance_text);
    TextView loginTimeView = ViewHolder.findViewById(convertView, R.id.login_time_text);
    ImageView avatarView = ViewHolder.findViewById(convertView, R.id.avatar_view);
    String avatarUrl = user.getAvatarUrl();

    UserService.displayAvatar(avatarUrl, avatarView);

    AVGeoPoint geoPoint = user.getLocation();
    String currentLat = String.valueOf(location.getLatitude());
    String currentLong = String.valueOf(location.getLongitude());
    if (geoPoint != null && !currentLat.equals("") && !currentLong.equals("")) {
      double distance = DistanceOfTwoPoints(Double.parseDouble(currentLat), Double.parseDouble(currentLong),
          user.getLocation().getLatitude(),
          user.getLocation().getLongitude());
      distanceView.setText(Utils.getPrettyDistance(distance));
    } else {
      distanceView.setText(App.ctx.getString(R.string.unknown));
    }
    nameView.setText(user.getUsername());
    Date updatedAt = user.getUpdatedAt();
    String prettyTimeStr = this.prettyTime.format(updatedAt);
    loginTimeView.setText(App.ctx.getString(R.string.recent_login_time) + prettyTimeStr);
    return convertView;
  }

  private static final double EARTH_RADIUS = 6378137;

  private static double rad(double d) {
    return d * Math.PI / 180.0;
  }

  /**
   * 根据两点间经纬度坐标（double值），计算两点间距离，
   *
   * @param lat1
   * @param lng1
   * @param lat2
   * @param lng2
   * @return 距离：单位为米
   */
  public static double DistanceOfTwoPoints(double lat1, double lng1,
                                           double lat2, double lng2) {
    double radLat1 = rad(lat1);
    double radLat2 = rad(lat2);
    double a = radLat1 - radLat2;
    double b = rad(lng1) - rad(lng2);
    double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
        + Math.cos(radLat1) * Math.cos(radLat2)
        * Math.pow(Math.sin(b / 2), 2)));
    s = s * EARTH_RADIUS;
    s = Math.round(s * 10000) / 10000;
    return s;
  }

}
