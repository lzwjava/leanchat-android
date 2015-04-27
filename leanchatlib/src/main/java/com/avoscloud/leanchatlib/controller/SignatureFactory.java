package com.avoscloud.leanchatlib.controller;

import android.widget.Toast;
import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.Signature;
import com.avoscloud.leanchatlib.R;
import com.avoscloud.leanchatlib.utils.Logger;

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
   * imClient.openWithUserId() ,create conv
   */
  @Override
  public Signature createSignature(String peerId, List<String> watchIds) throws SignatureException {
    Logger.d("selfId=" + peerId + " targetIds=" + watchIds);
    HashMap<String, Object> result;
    Map<String, Object> map1 = new HashMap<String, Object>();
    map1.put("self_id", peerId);
    if (null != null) {
      map1.put("convid", null);
    }
    if (watchIds != null) {
      map1.put("targetIds", watchIds);
    }
    if (null != null) {
      map1.put("action", null);
    }
    try {
      result = AVCloud.callFunction("conv_sign", map1);
    } catch (AVException e) {
      throw new SignatureException(e.getCode(), e.getMessage());
    }
    HashMap<String, Object> map = result;
    return createSignatureByResult(map, watchIds);
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
    return null;
  }

  @Override
  public Signature createConversationSignature(String conversationId, String clientId, List<String> targetIds, String action) throws SignatureException {
    Logger.d("convid=" + conversationId + " clientid=" + clientId + " targetIds=" + targetIds + " action=" + action);
    HashMap<String, Object> result;
    Map<String, Object> map1 = new HashMap<String, Object>();
    map1.put("self_id", clientId);
    if (conversationId != null) {
      map1.put("convid", conversationId);
    }
    if (targetIds != null) {
      map1.put("targetIds", targetIds);
    }
    if (action != null) {
      map1.put("action", action);
    }
    try {
      result = AVCloud.callFunction("conv_sign", map1);
    } catch (AVException e) {
      if (e.getCode() == AVException.INVALID_JSON) {
        Toast.makeText(ChatManager.getContext(), R.string.chat_cloudCodeNotDeployTips, Toast.LENGTH_SHORT).show();
      }
      throw new SignatureException(e.getCode(), e.getMessage());
    }
    HashMap<String, Object> map = result;
    return createSignatureByResult(map, targetIds);
  }
}
