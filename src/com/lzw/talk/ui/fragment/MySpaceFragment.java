package com.lzw.talk.ui.fragment;

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
import com.lzw.talk.R;
import com.lzw.talk.avobject.User;
import com.lzw.talk.util.PathUtils;
import com.lzw.talk.util.PhotoUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lzw on 14-9-17.
 */
public class MySpaceFragment extends BaseFragment implements View.OnClickListener {
  private static final int IMAGE_PICK_REQUEST = 1;
  private static final int CROP_REQUEST = 2;
  TextView usernameView;
  ImageView avatarView;
  View usernameLayout, avatarLayout, logoutLayout;

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
    ImageLoader imageLoader = ImageLoader.getInstance();
    imageLoader.displayImage(curUser.getAvatarUrl(), avatarView,
        PhotoUtil.getAvatarImageOptions());
    avatarLayout.setOnClickListener(this);
    logoutLayout.setOnClickListener(this);
  }

  private void findView() {
    usernameView = (TextView) ctx.findViewById(R.id.username);
    avatarView = (ImageView) ctx.findViewById(R.id.avatar);
    usernameLayout = ctx.findViewById(R.id.usernameLayout);
    avatarLayout = ctx.findViewById(R.id.avatarLayout);
    logoutLayout = ctx.findViewById(R.id.logoutLayout);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.avatarLayout) {
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
    if (requestCode == IMAGE_PICK_REQUEST) {
      if (resultCode == Activity.RESULT_OK) {
        Uri uri = data.getData();
        startImageCrop(ctx, uri, 200, 200, CROP_REQUEST);
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

  public static Uri startImageCrop(Activity ctx, Uri uri, int outputX, int outputY,
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
    ctx.startActivityForResult(intent, requestCode);
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
