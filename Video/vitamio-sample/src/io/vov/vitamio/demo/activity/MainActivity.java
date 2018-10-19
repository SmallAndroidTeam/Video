package io.vov.vitamio.demo.activity;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.vov.vitamio.demo.R;
import io.vov.vitamio.demo.fragment.CollectFragment;
import io.vov.vitamio.demo.fragment.DirectBroadFragment;
import io.vov.vitamio.demo.fragment.LocalFragment;
import io.vov.vitamio.demo.fragment.OnlineFragment;
import io.vov.vitamio.demo.fragment.SdCardFragment;
import io.vov.vitamio.demo.fragment.UDiskFragment;
import io.vov.vitamio.demo.saveDate.SaveCollectFragment;
import io.vov.vitamio.demo.service.DownLoadService;
import io.vov.vitamio.demo.utils.SaveVideoDownloadStatus;
import io.vov.vitamio.toast.oneToast;

public class MainActivity extends FragmentActivity implements View.OnClickListener{
    private Fragment localFragment,onlineFragment,collectFragment,personalCentreFragment,directBroadFragment;
    public static final String TAG="movie2";
    private RadioButton rb_localvideo;
    private RadioButton rb_netvideo;
    private RadioButton rb_netdirectbroad;
    private RadioButton rb_collectvideo;
    private RadioButton rb_personalcentre;
    private UDiskFragment.UsbBroadcastReceiver usbBroadcastReceiver;
    private DrawerLayout mDrawerLayout;
    private ListView personalcentre_listview;
    private List<View> viewList=new ArrayList<>();//存储最下面的单选按钮
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        //无标题
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        //全屏
//       getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        ininView();

        addData();

        initClickListener();
        rb_localvideo.setChecked(true);
        seletTab(0);
        usbBroadcastReceiver=new UDiskFragment.UsbBroadcastReceiver();
        //动态注册广播
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(UDiskFragment.USB_DEVICE_ATTACHED);
        intentFilter.addAction(UDiskFragment.USB_DEVICE_DETACHED);
        intentFilter.addAction(UDiskFragment.ACTION_USB_PERMISSION);
        registerReceiver(usbBroadcastReceiver,intentFilter);
        UDiskFragment.setmContext(this);
        DownLoadService.setMainActivityContext(this);//设置下载服务的主页的上下文环境
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbBroadcastReceiver);

    }

    private void initClickListener() {
        rb_localvideo.setOnClickListener(this);
        rb_netvideo.setOnClickListener(this);
        rb_netdirectbroad.setOnClickListener(this);
        rb_collectvideo.setOnClickListener(this);
        rb_personalcentre.setOnClickListener(this);

        personalcentre_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 1://下载管理(打开下载中心的活动页）
                        mDrawerLayout.closeDrawers();
                        Intent intent=new Intent(MainActivity.this,DownloadCenterActivity.class);
                        //下载测试用
                        startActivity(intent);
                        MainActivity.this.overridePendingTransition(R.anim.in_from_right,R.anim.out_from_left);
                        break;
                    case 2://退出
                        alertDialog().show();

                        break;
                        default:
                            break;
                }
            }
        });

       mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
           @Override
           public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

           }

           @Override
           public void onDrawerOpened(@NonNull View drawerView) {

           }

           @Override
           public void onDrawerClosed(@NonNull View drawerView) {//关闭侧方菜单
               Log.i("movie444", "onDrawerClosed: 11"+"//"+preIndex);
               currentIndex=preIndex;
               ((RadioButton)viewList.get(preIndex)).setChecked(true);

               onClick(viewList.get(preIndex));

           }

           @Override
           public void onDrawerStateChanged(int newState) {

           }
       });
    }
    public Dialog alertDialog(){
      final   View view = LayoutInflater.from(this).inflate(R.layout.alert_exit_dialog, null);
        final Button cancelButton=(Button)view.findViewById(R.id.cancel);
        final Button yesButton=(Button)view.findViewById(R.id.yes);

         final Dialog dialog = new Dialog(this, R.style.MyDialog);
         dialog.setCancelable(false);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
//        window.setWindowAnimations(R.style.dialogAnimation);
        window.setContentView(view);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mDrawerLayout.closeDrawers();
                finish();//关闭界面

            }
        });
        WindowManager.LayoutParams lp = window.getAttributes(); // 获取对话框当前的参数值
        lp.width =WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height =WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        return dialog;
    }

    private void ininView() {
        rb_localvideo = (RadioButton)this.findViewById(R.id.rb_localvideo);
        rb_netvideo = (RadioButton)this.findViewById(R.id.rb_netvideo);
        rb_netdirectbroad = (RadioButton)this.findViewById(R.id.rb_netdirectbroad);
        rb_collectvideo = (RadioButton)this.findViewById(R.id.rb_collectvideo);
        rb_personalcentre = (RadioButton)this.findViewById(R.id.rb_personalcentre);
        mDrawerLayout = (DrawerLayout)this.findViewById(R.id.mDrawerLayout);
        personalcentre_listview = (ListView)this.findViewById(R.id.personalcentre_listview);
        viewList.add(rb_localvideo);
        viewList.add(rb_netvideo);
        viewList.add(rb_netdirectbroad);
        viewList.add(rb_collectvideo);
        viewList.add(rb_personalcentre);
    }


    private void addData() {

    final List<Map<String,Object>> persionList=new ArrayList<>();
    Map<String,Object> map=new ArrayMap<>();
    map.put("personCentreImageview",R.drawable.personcentre_download);
    map.put("personCentreTextview","下载管理");
    persionList.add(map);
    map=new ArrayMap<>();
    map.put("personCentreImageview",R.drawable.personcentre_exit);
    map.put("personCentreTextview","退出");
    persionList.add(map);

    final SimpleAdapter simpleAdapter=new SimpleAdapter(this,persionList,R.layout.personal_centre_fragment_item,new String[]{
    "personCentreImageview","personCentreTextview"
    },new int[]{R.id.personcentre_imageview,R.id.personcentre_textview});

        personalcentre_listview.addHeaderView(LayoutInflater.from(this).inflate(R.layout.personcentre_listview_header,personalcentre_listview,false));

    personalcentre_listview.setAdapter(simpleAdapter);

    }



    private int preIndex=0;//上一个radiobutton的下标
    private int currentIndex=0;//当前的radiobutton的下标

    @Override
    public void onClick(View view) {
       switch (view.getId()){
           case R.id.rb_localvideo:
               seletTab(0);
               preIndex=currentIndex;
               currentIndex=0;
               break;
           case R.id.rb_netvideo:
               seletTab(1);
               preIndex=currentIndex;
               currentIndex=1;
               break;
           case R.id.rb_netdirectbroad:
               seletTab(2);
               preIndex=currentIndex;
               currentIndex=2;
               break;
           case R.id.rb_collectvideo:
               seletTab(3);
               preIndex=currentIndex;
               currentIndex=3;
               break;
           case R.id.rb_personalcentre:
               mDrawerLayout.openDrawer(Gravity.START);
               preIndex=currentIndex;
               currentIndex=4;
               break;
               default:
                   break;
       }
    }



    private void seletTab(int i){

     //   oneToast.showMessage(this,""+i);
        final FragmentManager fragmentManager=getSupportFragmentManager();
         FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        hideFragment(fragmentTransaction);
       switch (i){
           case 0:
               if(localFragment==null){
                   localFragment=new LocalFragment();
                   fragmentTransaction.add(R.id.mainFragment,localFragment);
               }else{
                   ((SdCardFragment)((LocalFragment)localFragment).getFragmentList().get(1)).UpdateView();
                   fragmentTransaction.show(localFragment);
               }
               break;
           case 1:
               if(onlineFragment==null){
                   onlineFragment=new OnlineFragment();
                   fragmentTransaction.add(R.id.mainFragment,onlineFragment);
               }else{
                   fragmentTransaction.show(onlineFragment);
               }
               break;
           case 2:
               if(directBroadFragment==null){
                   directBroadFragment=new DirectBroadFragment();
                   fragmentTransaction.add(R.id.mainFragment,directBroadFragment);
               }else{

                   fragmentTransaction.show(directBroadFragment);
               }
               break;
           case 3:
           if(collectFragment==null){
                collectFragment=new CollectFragment();
                fragmentTransaction.add(R.id.mainFragment,collectFragment);
           }else{
               fragmentTransaction.show(collectFragment);
               ((CollectFragment)collectFragment).updateView();
           }
           SaveCollectFragment.setCollectFragment(collectFragment);
           break;
               default:
                   break;
       }
       fragmentTransaction.commit();
    }

    private  void hideFragment(FragmentTransaction fragmentTransaction){
        if(localFragment!=null)
        {fragmentTransaction.hide(localFragment);
        }
        if(onlineFragment!=null)
        {fragmentTransaction.hide(onlineFragment);
        }
        if(directBroadFragment!=null)
        {fragmentTransaction.hide(directBroadFragment);
        }
        if(collectFragment!=null)
        {fragmentTransaction.hide(collectFragment);
        }

    }


}
