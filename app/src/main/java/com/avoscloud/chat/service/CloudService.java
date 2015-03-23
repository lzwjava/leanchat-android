package com.avoscloud.chat.service;

import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.SignatureFactory;
import com.avoscloud.chat.R;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lzw on 14-9-29.
 */
public class CloudService {
  public static void checkCloudCodeDeploy(AVException e) {
    if (e.getCode() == AVException.INVALID_JSON) {
      Utils.toast(App.ctx.getString(R.string.cloudCodeNotDeployTips));
    }
  }

  public static HashMap<String, Object> convSign(String selfId, String convid, List<String> targetIds, String action) throws SignatureFactory.SignatureException {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("self_id", selfId);
    if (convid != null) {
      map.put("convid", convid);
    }
    if (targetIds != null) {
      map.put("targetIds", targetIds);
    }
    if (action != null) {
      map.put("action", action);
    }
    try {
      return AVCloud.callFunction("conv_sign", map);
    } catch (AVException e) {
      throw new SignatureFactory.SignatureException(e.getCode(), e.getMessage());
    }
  }
}
