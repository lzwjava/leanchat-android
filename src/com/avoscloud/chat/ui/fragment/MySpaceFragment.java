package com.avoscloud.chat.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.SaveCallback;
import com.avoscloud.chat.R;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.service.ChatService;
import com.avoscloud.chat.service.UpdateService;
import com.avoscloud.chat.service.UserService;
import com.avoscloud.chat.ui.activity.NotifyPrefActivity;
import com.avoscloud.chat.util.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lzw on 14-9-17.
 */
public class MySpaceFragment extends BaseFragment implements View.OnClickListener {
  private static final int IMAGE_PICK_REQUEST = 1;
  private static final int CROP_REQUEST = 2;
  TextView usernameView, sexView;
  ImageView avatarView;
  View usernameLayout, avatarLayout, logoutLayout,
      sexLayout, notifyLayout, updateLayout;
  String[] sexs = new String[]{App.ctx.getString(R.string.male),
      App.ctx.getString(R.string.female)};

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.my_space_fragment, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    headerLayout.showTitle(R.string.me);
    findView();
    refresh();
  }

  private void refresh() {
    User curUser = User.curUser();
    usernameView.setText(curUser.getUsername());
    sexView.setText(curUser.getSexInfo());
    UserService.displayAvatar(curUser.getAvatarUrl(), avatarView);
  }

  private void findView() {
    usernameView = (TextView) ctx.findViewById(R.id.username);
    avatarView = (ImageView) ctx.findViewById(R.id.avatar);
    usernameLayout = ctx.findViewById(R.id.usernameLayout);
    avatarLayout = ctx.findViewById(R.id.avatarLayout);
    logoutLayout = ctx.findViewById(R.id.logoutLayout);
    sexLayout = ctx.findViewById(R.id.sexLayout);
    notifyLayout = ctx.findViewById(R.id.notifyLayout);
    sexView = (TextView) ctx.findViewById(R.id.sex);
    updateLayout = ctx.findViewById(R.id.updateLayout);

    avatarLayout.setOnClickListener(this);
    logoutLayout.setOnClickListener(this);
    sexLayout.setOnClickListener(this);
    notifyLayout.setOnClickListener(this);
    updateLayout.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.avatarLayout) {
      Intent intent = new Intent(Intent.ACTION_PICK, null);
      intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
      startActivityForResult(intent, IMAGE_PICK_REQUEST);
    } else if (id == R.id.logoutLayout) {
      ChatService.closeSession();
      User.logOut();
      User.setCurUser(null);
      getActivity().finish();
    } else if (id == R.id.sexLayout) {
      showSexChooseDialog();
    } else if (id == R.id.notifyLayout) {
      Utils.goActivity(ctx, NotifyPrefActivity.class);
    } else if (id == R.id.updateLayout) {
      UpdateService updateService = UpdateService.getInstance(ctx);
      updateService.showSureUpdateDialog();
    }
  }

  SaveCallback saveCallback = new SaveCallback() {
    @Override
    public void done(AVException e) {
      refresh();
    }
  };

  private void showSexChooseDialog() {
    User user = User.curUser();
    int checkItem = user.getSex() ? 0 : 1;
    new AlertDialog.Builder(ctx).setTitle(R.string.sex)
        .setSingleChoiceItems(sexs, checkItem, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            boolean isBoy = which == 0 ? true : false;
            UserService.saveSex(isBoy, saveCallback);
            dialog.dismiss();
          }
        }).setNegativeButton(R.string.cancel, null).show();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    Logger.d("on Activity result " + requestCode + " " + resultCode);
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
    Logger.d("outputPath=" + outputPath);
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
        bitmap = PhotoUtil.toRoundCorner(bitmap, 10);
        String filename = new SimpleDateFormat("yyMMddHHmmss")
            .format(new Date());
        path = PathUtils.getAvatarDir() + filename;
        Logger.d("save bitmap to " + path);
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
