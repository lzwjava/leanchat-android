package com.lzw.talk.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.SaveCallback;
import com.lzw.talk.R;
import com.lzw.talk.avobject.User;
import com.lzw.talk.service.UserService;
import com.lzw.talk.util.PathUtils;
import com.lzw.talk.util.PhotoUtil;
import com.lzw.talk.util.Utils;
import com.lzw.talk.view.activity.UpdateContentActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lzw on 14-9-17.
 */
public class MySpaceFragment extends BaseFragment implements View.OnClickListener {
  private static final int REQUEST_CODE_NICKNAME = 0;
  private static final int IMAGE_PICK_REQUEST = 1;
  private static final int CROP_REQUEST = 2;
  TextView usernameView, nicknameView;
  ImageView avatarView;
  View nicknameLayout, usernameLayout, avatarLayout, logoutLayout;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.my_space_fragment, null);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    headerLayout.showTitle(R.string.me);
    findView();
    init();
  }

  private void init() {
    User curUser = User.curUser();
    usernameView.setText(curUser.getUsername());
    nicknameView.setText(curUser.getNickname());
    ImageLoader imageLoader = ImageLoader.getInstance();
    imageLoader.displayImage(curUser.getAvatarUrl(), avatarView,
        PhotoUtil.getImageLoaderOptions());
    nicknameLayout.setOnClickListener(this);
    avatarLayout.setOnClickListener(this);
    logoutLayout.setOnClickListener(this);
  }

  private void findView() {
    usernameView = (TextView) ctx.findViewById(R.id.username);
    nicknameView = (TextView) ctx.findViewById(R.id.nickname);
    avatarView = (ImageView) ctx.findViewById(R.id.avatar);
    usernameLayout = ctx.findViewById(R.id.usernameLayout);
    nicknameLayout = ctx.findViewById(R.id.nicknameLayout);
    avatarLayout = ctx.findViewById(R.id.avatarLayout);
    logoutLayout = ctx.findViewById(R.id.logoutLayout);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.nicknameLayout) {
      Intent intent = new Intent(ctx, UpdateContentActivity.class);
      intent.putExtra(UpdateContentActivity.FIELD_NAME, ctx.getString(R.string.nickname));
      startActivityForResult(intent, REQUEST_CODE_NICKNAME);
    } else if (id == R.id.avatarLayout) {
      Intent intent = new Intent(Intent.ACTION_PICK, null);
      intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
      startActivityForResult(intent, IMAGE_PICK_REQUEST);
    } else if (id == R.id.logoutLayout) {
      User.logOut();
      getActivity().finish();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_NICKNAME) {
      if (resultCode == Activity.RESULT_OK) {
        String value = data.getStringExtra(UpdateContentActivity.VALUE);
        UserService.updateNickname(User.curUser(), value, new SaveCallback() {

          @Override
          public void done(AVException e) {
            if (e != null) {
              e.printStackTrace();
              Utils.toast(ctx, R.string.badNetwork);
            } else {
              init();
            }
          }
        });
      }
    } else if (requestCode == IMAGE_PICK_REQUEST) {
      if (resultCode == Activity.RESULT_OK) {
        Uri uri = data.getData();
        startImageCrop(uri, 200, 200, CROP_REQUEST);
      }
    } else if (requestCode == CROP_REQUEST) {
      if (resultCode == Activity.RESULT_OK) {
        String path = saveCropAvatar(data);
        User curUser = User.curUser();
        curUser.saveAvatar(path);
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
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
    intent.putExtra("noFaceDetection", true); // no face detection
    startActivityForResult(intent, requestCode);
    return outputUri;
  }

  private String saveCropAvatar(Intent data) {
    Bundle extras = data.getExtras();
    String path = null;
    if (extras != null) {
      Bitmap bitmap = extras.getParcelable("data");
      Log.i("life", "avatar - bitmap = " + bitmap);
      if (bitmap != null) {
        bitmap = PhotoUtil.toRoundCorner(bitmap, 10);
        avatarView.setImageBitmap(bitmap);
        String filename = new SimpleDateFormat("yyMMddHHmmss")
            .format(new Date());
        path = PathUtils.getAvatarDir() + filename;
        PhotoUtil.saveBitmap(PathUtils.getAvatarDir(), filename,
            bitmap, true);
        if (bitmap != null && bitmap.isRecycled() == false) {
          //bitmap.recycle();
        }
      }
    }
    return path;
  }
}
