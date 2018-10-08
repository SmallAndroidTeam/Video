package io.vov.vitamio.demo.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.util.logging.FileHandler;

import io.vov.vitamio.demo.R;
import io.vov.vitamio.toast.oneToast;

/**
 * 登录界面
 * Created by MR.XIE on 2018/9/28.
 */
public class LoadingActivity extends Activity {
    private final  static long DELAY_TIME=1600;//延迟时间
    private final static String[] permission={Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private AlertDialog alertDialog;
    private final static String TAG="movie2";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //       getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        getPermission(); //动态获取权限
    }

//    进入主界面
    private void gotoMainActivity(){
     new Handler().postDelayed(new Runnable() {
         @Override
         public void run() {
             Intent intent=new Intent(LoadingActivity.this, MainActivity.class);
             startActivity(intent);
             finish();
             LoadingActivity.this.overridePendingTransition(R.anim.in_from_right,R.anim.out_from_left);
             }
     },DELAY_TIME);
    }

    //动态获取权限
     private void getPermission(){
        if(ContextCompat.checkSelfPermission(LoadingActivity.this,permission[0])!= PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(LoadingActivity.this,permission[1])!= PackageManager.PERMISSION_GRANTED){
            showDialogTipUserRequestPermisson();
        }else{
            gotoMainActivity();
        }

     }

     //提示用户请求权限弹出框
    private void showDialogTipUserRequestPermisson() {

        new AlertDialog.Builder(LoadingActivity.this).setTitle("读取权限不可用").setMessage("由于Video需要读取本地视频信息;\n否则，您将无法正常使用")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(LoadingActivity.this,permission,1);
                    }
                }).setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).setCancelable(false).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       switch (requestCode){
           case  1:
                   if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED&&grantResults[1]==PackageManager.PERMISSION_GRANTED){
                       oneToast.showMessage(this,"权限获取成功");
                       gotoMainActivity();
                   }else{
                       //判断用户是否点击了不在提醒
                       boolean b=shouldShowRequestPermissionRationale(permissions[0]);
                       if(!b){
                         //提示用户去应用设置界面手动开启权限
                           showDialogTipUserGoToAppSetting();
                       }else{
                           finish();
                       }
               }
               break;
               default:
                   break;
       }
    }

    private void showDialogTipUserGoToAppSetting() {
        alertDialog = new AlertDialog.Builder(LoadingActivity.this).setTitle("读取权限不可用").setMessage("请在-应用设置-权限，允许Video使用读取权限")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //跳到应用设置界面
                        goToAppSetting();
                    }
                }).setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
    }

    private void goToAppSetting(){
        Intent intent=new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri=Uri.fromParts("package",getPackageName(),null);
        intent.setData(uri);
        startActivityForResult(intent,23);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==23){
                if(ContextCompat.checkSelfPermission(LoadingActivity.this,permission[0])!=PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(LoadingActivity.this,permission[1])!=PackageManager.PERMISSION_GRANTED){
                    showDialogTipUserGoToAppSetting();
                }else{
                    if(alertDialog!=null&&alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    oneToast.showMessage(this,"权限获取成功");
                    gotoMainActivity();
                }
        }
    }
}
