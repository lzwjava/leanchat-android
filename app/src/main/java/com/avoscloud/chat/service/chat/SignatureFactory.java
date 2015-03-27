package com.avoscloud.chat.service.chat;

import com.avos.avoscloud.Signature;
import com.avoscloud.chat.service.CloudService;
import com.avoscloud.chat.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    Logger.d("selfId=" + peerId + " targetIds=" + watchIds);
    HashMap<String, Object> map = CloudService.convSign(peerId, null, watchIds, null);
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
    HashMap<String, Object> map = CloudService.convSign(clientId, conversationId, targetIds, action);
    return createSignatureByResult(map, targetIds);
  }
}
