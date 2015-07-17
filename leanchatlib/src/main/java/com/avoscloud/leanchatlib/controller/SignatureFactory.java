package com.avoscloud.leanchatlib.controller;

import android.widget.Toast;
import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.Signature;
import com.avoscloud.leanchatlib.R;
import com.avoscloud.leanchatlib.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请配合云代码使用 <br/>
 * <a href="https://github.com/leancloud/leanchat-cloudcode">leanchat-cloudcode</a><br>
 * <a href="https://github.com/leancloud/realtime-messaging-signature-cloudcode">Demo</a>
 */
public class SignatureFactory implements com.avos.avoscloud.SignatureFactory {
  /**
   * imClient.open() ,create conv
   */
  @Override
  public Signature createSignature(String peerId, List<String> watchIds) throws SignatureException {
    LogUtils.i("selfId=" + peerId + " targetIds=" + watchIds);
    HashMap<String, Object> result;
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("self_id", peerId);
    if (watchIds != null) {
      map.put("targetIds", watchIds);
    }
    try {
      result = AVCloud.callFunction("conv_sign", map);
    } catch (AVException e) {
      throw new SignatureException(e.getCode(), e.getMessage());
    }
    return createSignatureByResult(result, watchIds);
  }

  private Signature createSignatureByResult(HashMap<String, Object> params, List<String> peerIds) {
    Signature s = new Signature();
    List<String> copyIds = new ArrayList<String>();
    copyIds.addAll(peerIds);
    s.setTimestamp((Integer) params.get("timestamp"));
    s.setNonce((String) params.get("nonce"));
    s.setSignature((String) params.get("signature"));
    s.setSignedPeerIds(copyIds);
    return s;
  }

  @Override
  public Signature createGroupSignature(String groupId, String peerId, List<String> targetPeerIds,
                                        String action) {
    // v1 版本，忽略
    return null;
  }

  @Override
  public Signature createConversationSignature(String conversationId, String clientId, List<String> targetIds, String action) throws SignatureException {
    LogUtils.i("convid=" + conversationId + " clientid=" + clientId + " targetIds=" + targetIds + " action=" + action);
    HashMap<String, Object> result;
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("self_id", clientId);
    if (conversationId != null) {
      map.put("convid", conversationId);
    }
    if (targetIds != null) {
      map.put("targetIds", targetIds);
    }
    if (action != null) {
      map.put("action", action);
    }
    try {
      result = AVCloud.callFunction("conv_sign", map);
    } catch (AVException e) {
      if (e.getCode() == AVException.INVALID_JSON) {
        Toast.makeText(ChatManager.getContext(), R.string.chat_cloudCodeNotDeployTips, Toast.LENGTH_SHORT).show();
      }
      throw new SignatureException(e.getCode(), e.getMessage());
    }
    return createSignatureByResult(result, targetIds);
  }
}
