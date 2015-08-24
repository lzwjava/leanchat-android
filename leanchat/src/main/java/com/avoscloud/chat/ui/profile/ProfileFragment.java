package com.avoscloud.chat.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avoscloud.chat.R;
import com.avoscloud.chat.service.PushManager;
import com.avoscloud.chat.service.UpdateService;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.ui.base_activity.BaseFragment;
import com.avoscloud.chat.ui.entry.EntryLoginActivity;
import com.avoscloud.chat.util.PathUtils;
import com.avoscloud.chat.util.PhotoUtils;
import com.avoscloud.chat.util.SimpleNetTask;
import com.avoscloud.leanchatlib.controller.ChatManager;

import java.io.File;

/**
 * Created by lzw on 14-9-17.
 */
public class ProfileFragment extends BaseFragment {
  private static final int IMAGE_PICK_REQUEST = 1;
  private static final int CROP_REQUEST = 2;

  @InjectView(R.id.profile_avatar_view)
  ImageView avatarView;

  @InjectView(R.id.profile_username_view)
  TextView userNameView;

  ChatManager chatManager;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.profile_fragment, container, false);
    ButterKnife.inject(this, view);
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    headerLayout.showTitle(R.string.profile_title);
    chatManager = ChatManager.getInstance();
  }

  @Override
  public void onResume() {
    super.onResume();
    refresh();
  }

  private void refresh() {
    AVUser curUser = AVUser.getCurrentUser();
    userNameView.setText(curUser.getUsername());
    UserService.displayAvatar(curUser, avatarView);
  }

  @OnClick(R.id.profile_checkupdate_view)
  public void onCheckUpdateClick() {
    UpdateService updateService = UpdateService.getInstance(getActivity());
    updateService.showSureUpdateDialog();
  }

  @OnClick(R.id.profile_notifysetting_view)
  public void onNotifySettingClick() {
    Intent intent = new Intent(ctx, ProfileNotifySettingActivity.class);
    ctx.startActivity(intent);
  }

  @OnClick(R.id.profile_logout_btn)
  public void onLogoutClick() {
    chatManager.closeWithCallback(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient avimClient, AVIMException e) {
      }
    });
    PushManager.getInstance().unsubscribeCurrentUserChannel();
    AVUser.logOut();
    getActivity().finish();
    Intent intent = new Intent(ctx, EntryLoginActivity.class);
    ctx.startActivity(intent);
  }

  @OnClick(R.id.profile_avatar_layout)
  public void onAvatarClick() {
    Intent intent = new Intent(Intent.ACTION_PICK, null);
    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
    startActivityForResult(intent, IMAGE_PICK_REQUEST);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == IMAGE_PICK_REQUEST) {
        Uri uri = data.getData();
        startImageCrop(uri, 200, 200, CROP_REQUEST);
      } else if (requestCode == CROP_REQUEST) {
        final String path = saveCropAvatar(data);
        new SimpleNetTask(ctx) {
          @Override
          protected void doInBack() throws Exception {
            UserService.saveAvatar(path);
          }

          @Override
          protected void onSucceed() {
            refresh();
          }
        }.execute();

      }
    }
  }

  public Uri startImageCrop(Uri uri, int outputX, int outputY,
                            int requestCode) {
    Intent intent = null;
    intent = new Intent("com.android.camera.action.CROP");
    intent.setDataAndType(uri, "image/*");
    intent.putExtra("crop", "true");
    intent.putExtra("aspectX", 1);
    intent.putExtra("aspectY", 1);
    intent.putExtra("outputX", outputX);
    intent.putExtra("outputY", outputY);
    intent.putExtra("scale", true);
    String outputPath = PathUtils.getAvatarTmpPath();
    Uri outputUri = Uri.fromFile(new File(outputPath));
    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
    intent.putExtra("return-data", true);
    intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
    intent.putExtra("noFaceDetection", false); // face detection
    startActivityForResult(intent, requestCode);
    return outputUri;
  }

  private String saveCropAvatar(Intent data) {
    Bundle extras = data.getExtras();
    String path = null;
    if (extras != null) {
      Bitmap bitmap = extras.getParcelable("data");
      if (bitmap != null) {
        bitmap = PhotoUtils.toRoundCorner(bitmap, 10);
        path = PathUtils.getAvatarCropPath();
        PhotoUtils.saveBitmap(path, bitmap);
        if (bitmap != null && bitmap.isRecycled() == false) {
          bitmap.recycle();
        }
      }
    }
    return path;
  }
}
