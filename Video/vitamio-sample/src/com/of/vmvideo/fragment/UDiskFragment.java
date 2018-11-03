package com.of.vmvideo.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.LoginFilter;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileInputStream;
import com.github.mjdev.libaums.partition.Partition;
import com.of.vmvideo.activity.PlayActivity;
import com.of.vmvideo.adapter.UdiskAdapter;
import com.of.vmvideo.localData.FileManger;
import com.of.vmvideo.saveDate.SaveCollectFragment;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.acl.NotOwnerException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.vov.vitamio.bean.Video;

import io.vov.vitamio.toast.oneToast;

import com.of.vmvideo.R;
/**
 * Created by MR.XIE on 2018/9/7.
 */
public class UDiskFragment extends Fragment {

    private static TextView uDiskTipMessage;
    public static final String TAG="movie2";

    public final  static String  USB_DEVICE_ATTACHED="android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public final  static String USB_DEVICE_DETACHED="android.hardware.usb.action.USB_DEVICE_DETACHED";

    public final static String ACTION_USB_PERMISSION="com.android.example.USB_PERMISSION";
    private static UsbManager usbManager;
    private static AlertDialog alertDialog;
    public static UsbMassStorageDevice[] storageDevices;//当前U盘列表
    private final  static int READSUCCESS=1;//读取U盘信息成功
    private  final  static int READFAIL=0;//读取U盘信息失败
    private final  static int LOADVIDEOLIST=2;//加载U盘信息
    private final  static int REFRESH=3;//刷新
    private final  static int UDISK_DETACHED=4;//U盘拔出
    private final  static int RELOADVIDEOLIST=5;//重新加载
    private final  static int NOUDISK=6;//没U盘插入
    private static Context mContext;
    private static List<Video> videoList=new ArrayList<>();//播放的视频列表
  private   static String UDISK_MOUNT_ADDRESS=null;
  private static boolean isLoading=false;//是否正在加载
    private final static String SD_DIRECTORY="/storage";
    private static boolean isStart=false;//判断是否启动了
    @SuppressLint("HandlerLeak")
    private static Handler mhandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case READSUCCESS:
                    oneToast.showMessage(mContext,"获取权限成功");
                    break;
                case READFAIL:
                    loadingLayout.setVisibility(View.GONE);
                    uDiskTipMessage.setText("加载失败，请下拉刷新");
                    uDiskTipMessage.setVisibility(View.VISIBLE);
                    oneToast.showMessage(mContext,"读取U盘失败");
                    uDiskVideoRefresh.setRefreshing(false);
                    break;
                case NOUDISK:
                    loadingLayout.setVisibility(View.GONE);
                    uDiskTipMessage.setText("请插入U盘,点击刷新");
                    uDiskTipMessage.setVisibility(View.VISIBLE);

                    uDiskVideoRefresh.setRefreshing(false);
                    break;
                case LOADVIDEOLIST:
                    loadingLayout.setVisibility(View.GONE);
                    if(videoList.size()>0){
                        uDiskTipMessage.setVisibility(View.GONE);
                        if(udiskAdapter==null){
                            udiskAdapter = new UdiskAdapter(videoList,mContext);
                            videoListView.setAdapter(udiskAdapter);
                        }else{
                            udiskAdapter.setVideoList(videoList);
                            udiskAdapter.notifyDataSetChanged();
                        }
                    }else{
                        uDiskTipMessage.setText("空空如也");
                        uDiskTipMessage.setVisibility(View.VISIBLE);
                    }
                    uDiskVideoRefresh.setRefreshing(false);
                    break;
                case REFRESH:
                    loadingLayout.setVisibility(View.GONE);
                    if(videoList.size()>0){
                        uDiskTipMessage.setVisibility(View.GONE);
                        if(udiskAdapter==null){
                            udiskAdapter = new UdiskAdapter(videoList,mContext);
                            videoListView.setAdapter(udiskAdapter);
                        }else{
                            udiskAdapter.setVideoList(videoList);
                            udiskAdapter.notifyDataSetChanged();
                        }
                    }else{
                        uDiskTipMessage.setText("空空如也");
                        uDiskTipMessage.setVisibility(View.VISIBLE);
                    }
                    oneToast.showMessage(mContext,"刷新成功");
                    uDiskVideoRefresh.setRefreshing(false);
                    break;
                case RELOADVIDEOLIST:

                    uDiskTipMessage.setVisibility(View.GONE);
                    loadingLayout.setVisibility(View.VISIBLE);

                    break;
                case UDISK_DETACHED:
                    uDiskTipMessage.setText("请插入U盘,点击刷新");
                    uDiskTipMessage.setVisibility(View.VISIBLE);
                    default:
                        break;

            }
        }
    };
    private static SwipeRefreshLayout uDiskVideoRefresh;
    private static ListView videoListView;
    private static UdiskAdapter udiskAdapter;
    private static LinearLayout loadingLayout;
    private static Runnable getMountAddressRunnable;

    public static void setmContext(Context mContext) {
        UDiskFragment.mContext = mContext;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.u_disk_fragment,container,false);
        initView(view);
        initOnclickListener();
        if(isLoading){
            mhandler.sendEmptyMessage(RELOADVIDEOLIST);
            mhandler.post(new Runnable() {
                @Override
                public void run() {
                    if(isLoading){
                         mhandler.postDelayed(this,100);
                    }else{
                        mhandler.removeCallbacks(this);
                        mhandler.sendEmptyMessage(LOADVIDEOLIST);
                    }
                }
            });
        }else{
            if(videoList.size()>0){
                uDiskTipMessage.setVisibility(View.GONE);
                udiskAdapter = new UdiskAdapter(videoList,this.getContext());
                videoListView.setAdapter(udiskAdapter);
            }else{
                uDiskTipMessage.setVisibility(View.VISIBLE);
                uDiskTipMessage.callOnClick();
            }
        }
        isStart=true;
        return view;
    }

    private void initOnclickListener() {
      uDiskVideoRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
          @Override
          public void onRefresh() {
              new Thread(new Runnable() {
                  @Override
                  public void run() {
                          if (isMountSuccess()) {
                              if(getUdiskAudioList()){
                                  mhandler.sendEmptyMessage(REFRESH);
                              }else{
                                  mhandler.sendEmptyMessage(READFAIL);
                              }
                          } else {
                              mhandler.sendEmptyMessage(READFAIL);
                          }
                  }
              }).start();
          }
      });
      videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              List<Video> videoList= udiskAdapter.getVideoList();
              PlayActivity.setVideoList(videoList);
              Intent intent=new Intent(getActivity(), PlayActivity.class);
              intent.putExtra("position",position);
              startActivity(intent);
          }
      });
        uDiskTipMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (this){
                        mhandler.sendEmptyMessage(RELOADVIDEOLIST);
                        if(isExistUdisk(getContext())){
                            if(isMountSuccess()){
                                if(getUdiskAudioList()){
                                    mhandler.sendEmptyMessageDelayed(REFRESH,500);
                                }else{
                                    mhandler.sendEmptyMessageDelayed(READFAIL,500);
                                }
                            }else{
                                mhandler.sendEmptyMessageDelayed(NOUDISK,500);
                            }
                        }else{
                            mhandler.sendEmptyMessageDelayed(NOUDISK,500);
                        }

                    }
                    }
                }).start();
            }
        });
    }


    private void initView(View view) {
        uDiskTipMessage = (TextView)view.findViewById(R.id.uDisk_tip);
        uDiskVideoRefresh = (SwipeRefreshLayout)view.findViewById(R.id.udiskVideoRefresh);
        videoListView = (ListView)view.findViewById(R.id.videoListView);
        uDiskVideoRefresh.setProgressBackgroundColorSchemeResource(R.color.RefreshProgressBackground);
        uDiskVideoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,android.R.color.holo_green_light,
                android.R.color.holo_orange_light,android.R.color.holo_red_light);
        loadingLayout = (LinearLayout)view.findViewById(R.id.loading_layout);
    }


    /**
     * 检测USB的广播
     */
   public static   class UsbBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: "+intent.getAction());
            if(mContext==null)
             mContext=context;
            String action=intent.getAction();
            if(action.equals(USB_DEVICE_ATTACHED)){
                  showUsbList(context);
                 //  updateMessage();
            }else if(action.equals(USB_DEVICE_DETACHED)){
                mhandler.removeCallbacks(getMountAddressRunnable);
                 UDISK_MOUNT_ADDRESS=null;

                videoList.clear();

                if(isStart)
                    mhandler.sendEmptyMessage(UDISK_DETACHED);

                isLoading=false;

                updateMessage();
                oneToast.showMessage(context,"设备拔出");

                showUsbList(context);//再重新加载
            }
        }
    }


//更新sd和收藏界面视图
    public static void  updateMessage(){
        CollectFragment collectFragment= (CollectFragment) SaveCollectFragment.getCollectFragment();
        SdCardFragment sdCardFragment= (SdCardFragment) SaveCollectFragment.getSdCardFragment();
        if(collectFragment!=null){
            collectFragment.updateCollectFragmentView();
            collectFragment.updateView();
        }
        if(sdCardFragment!=null){
            Log.i("dsfsadf", "updateMessage: ");
            sdCardFragment.downLoadVideoUpdateView();
            sdCardFragment.UpdateView();
        }
    }




    private static   synchronized void showUsbList(Context context) {
        usbManager= (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String,UsbDevice> deviceHashMap=usbManager.getDeviceList();//获取设备列表
        if(deviceHashMap==null||deviceHashMap.size()==0){
            return;
        }
        Iterator<UsbDevice> deviceIterator=deviceHashMap.values().iterator();
        while (deviceIterator.hasNext()){
            UsbDevice usbDevice=deviceIterator.next();
            Log.i(TAG, "deviceName: "+usbDevice.getDeviceName());
            Log.i(TAG, "DeviceId: "+usbDevice.getDeviceId());
            Log.i(TAG, "VendorId: "+usbDevice.getVendorId());
            Log.i(TAG, "ProductId: "+usbDevice.getProductId());
            Log.i(TAG, ""+usbDevice.getManufacturerName()+"//"+usbDevice.getProductName()+"//"+usbDevice.getSerialNumber()+"//"
            +usbDevice.getVersion()+"//");
            int deviceClass=usbDevice.getDeviceClass();
            if(deviceClass==0){
                UsbInterface usbInterface=usbDevice.getInterface(deviceClass);
                int InterfaceClass=usbInterface.getInterfaceClass();
                if(InterfaceClass==8){
                    oneToast.showMessage(context,"U盘插入");
                     // getPermission(context,usbDevice);
                    readDevice(context,usbDevice);
                  break;//目前只获取一个U盘内容
                }else if(InterfaceClass==255){
                    Log.i(TAG, "此设备是手机\n ");
                }else if(InterfaceClass==3){
                    Log.i(TAG, "此设备是鼠标或者键盘\n ");
                }else{
                    Log.i(TAG, "其他设备\n ");
                }
            }
        }
    }


    //判断是否存在U盘
   public boolean isExistUdisk(Context context){
       usbManager= (UsbManager) context.getSystemService(Context.USB_SERVICE);
       HashMap<String,UsbDevice> deviceHashMap=usbManager.getDeviceList();//获取设备列表
       if(deviceHashMap==null||deviceHashMap.size()==0){
           return false;
       }
       Iterator<UsbDevice> deviceIterator=deviceHashMap.values().iterator();
       while (deviceIterator.hasNext()){
           UsbDevice usbDevice=deviceIterator.next();
           Log.i(TAG, "deviceName: "+usbDevice.getDeviceName());
           Log.i(TAG, "DeviceId: "+usbDevice.getDeviceId());
           Log.i(TAG, "VendorId: "+usbDevice.getVendorId());
           Log.i(TAG, "ProductId: "+usbDevice.getProductId());
           Log.i(TAG, ""+usbDevice.getManufacturerName()+"//"+usbDevice.getProductName()+"//"+usbDevice.getSerialNumber()+"//"
                   +usbDevice.getVersion()+"//");
           int deviceClass=usbDevice.getDeviceClass();
           if(deviceClass==0){
               UsbInterface usbInterface=usbDevice.getInterface(deviceClass);
               int InterfaceClass=usbInterface.getInterfaceClass();
               if(InterfaceClass==8){
                return  true;
               }
           }
       }
       return  false;
   }

    //读取U盘信息(开启子线程读取）
    private synchronized static void readDevice(final Context context, final UsbDevice usbDevice) {
                    //获取挂载U盘地址
                  if(isStart){
                      mhandler.sendEmptyMessage(RELOADVIDEOLIST);
                  }
                   handMountUdiskAddress(context);
    }



    //挂载U盘地址
    private static void handMountUdiskAddress(Context context){
           isLoading=true;
          getMountAddressRunnable = new Runnable() {
            @Override
            public void run() {
                if(!isMountSuccess()){
                    mhandler.postDelayed(this,100);
                }else{
                    mhandler.removeCallbacks(this);

                    if(getUdiskAudioList()){
                        if(isStart){
                            mhandler.sendEmptyMessage(LOADVIDEOLIST);
                        }
                    }else{
                        if(isStart){
                            mhandler.sendEmptyMessage(READFAIL);
                        }
                    }
                    isLoading=false;
                }
            }
        };
      mhandler.post(getMountAddressRunnable);
    }







    //判断是否挂载成功
    private static boolean isMountSuccess(){
       String ROOT_PATH="/storage/";
        String UDISK_MOUNT_POINT="/dev/block/vold/public";//只支持android8.0  //  /dev/block/vold/public:8,1
        InputStream is=null;
        InputStreamReader inputStreamReader=null;
        BufferedReader br=null;
        try{
            Runtime runtime=Runtime.getRuntime();
            Process proc=runtime.exec("mount");
            is=proc.getInputStream();
            inputStreamReader=new InputStreamReader(is);
            String line=null;
            br=new BufferedReader(inputStreamReader);
            while((line=br.readLine())!=null){
                if(line.split(" ")[0].toLowerCase().contains(UDISK_MOUNT_POINT)){//如果挂载点存在
//                   String[] split=line.split(" ")[2].split("/");
//                   String dirName=split[split.length-1];
//                   String storagePath=ROOT_PATH+dirName;
//                    Log.i("video111", "isMountSuccess: "+storagePath);
//                    File file1=new File(storagePath);
//                    if(file1.exists()&&file1.isDirectory()){
//                        UDISK_MOUNT_ADDRESS=storagePath;
//                        return true;
//                    }

                    UDISK_MOUNT_ADDRESS= line.split(" ")[2];
                    return true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStreamReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return false;
    }



//获取系统权限
       public static  boolean getRoot(){
       try{
           Runtime runtime=Runtime.getRuntime();
           Log.i("video111", "getRoot: 333");
           final Process process=runtime.exec("su root");
           Log.i("video111", "getRoot: 222");
           try {
               new Thread(new Runnable() {
                   @Override
                   public void run() {

                       InputStream inputStream=process.getErrorStream();
                       BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
                       String line=null;
                       try {
                           while((line=bufferedReader.readLine())!=null){
                               Log.i(TAG, "handMountUdiskAddress: 1000");
                           }
                       }catch (Exception e){
                           e.printStackTrace();
                       }finally {
                           try {
                               inputStream.close();
                           } catch (IOException e) {
                               e.printStackTrace();
                           }
                       }

                   }
               }).start();
               int result= process.waitFor();//等待输出结果
               return true;
           } catch (InterruptedException e) {
               e.printStackTrace();
               return  false;
           }

       }catch (Exception e){
           e.printStackTrace();
           Log.i("video111", "getRoot: 11");
           return false;
       }
       }




    //获取U盘里面的视频列表
    private  synchronized  static  boolean getUdiskAudioList(){

       if(UDISK_MOUNT_ADDRESS==null){
           return false;
       }
        Log.i("video111", "getUdiskAudioList: "+UDISK_MOUNT_ADDRESS);
       // /mnt/media_rw/A63E-2DC8
//        if(!getRoot()){
//           return false;
//        }
        videoList.clear();
        String result=null;
       File file=new File(UDISK_MOUNT_ADDRESS);
       if(!file.exists()||file.isFile()){

       return false;
       }else{
           try{
               File[] files=file.listFiles();

               for(File file1:files){
                   Log.i("video111", "getUdiskAudioList: 11");
                   if(file1.isFile()&& FileManger.isVideo(file1))
                   { Log.i("video111", "getUdiskAudioList: 22");
                       Video video=new Video();
                       video.setVideoPath(file1.getAbsolutePath());
                       video.setVideoName(file1.getName());
                       video.setSize(FileManger.getSize(file1.getAbsolutePath()));
                       video.setDate(FileManger.getDate(file1.getAbsolutePath()));
                       video.setDuration(FileManger.getVideoDuration(file1.getAbsolutePath()));
                       video.setThumbnail(FileManger.getVideoThumbnailThree(file1.getAbsolutePath(), MediaStore.Images.Thumbnails.MICRO_KIND));
                       videoList.add(video);
                       Log.i("video111", "getUdiskAudioList: 33");
                   }
                   else if(file1.isDirectory()){
                       Log.i("video111", "getUdiskAudioList: 44");
                       List<File> fileList=getAllFiles(file1);//获取一个目录下的所有可支持的视频文件,且视频不存在列表中
                       if(fileList==null){
                           continue;
                       }
                       Log.i("video111", "getUdiskAudioList: 55");
                       for(File file2:fileList){
                           Video video=new Video();
                           video.setVideoPath(file2.getAbsolutePath());
                           video.setVideoName(file2.getName());
                           video.setSize(FileManger.getSize(file2.getAbsolutePath()));
                           video.setDate(FileManger.getDate(file2.getAbsolutePath()));
                           video.setDuration(FileManger.getVideoDuration(file2.getAbsolutePath()));
                           video.setThumbnail(FileManger.getVideoThumbnailThree(file2.getAbsolutePath(), MediaStore.Images.Thumbnails.MICRO_KIND));
                           videoList.add(video);
                       }
                       Log.i("video111", "getUdiskAudioList: 66");
                   }
               }
           }catch (Exception e){
               Log.i("video111", e.getMessage());

               return false;
           }

       }
       return true;
    }

    public static List<File> getAllFiles(File file){//获取一个目录下的所有可支持的视频文件
        try {
            if(!file.exists()||file.isFile()){
                return null;
            }
            List<File> files=new ArrayList<>();
            File[] files1=file.listFiles();

            for(File file1:files1){
                if(file1.isFile()&&FileManger.isVideo(file1)){//如果为可支持的视频文件
                    files.add(file1);
                }else if(file1.isDirectory()){
                    List<File> fileList=getAllFiles(file1);
                    if(fileList!=null)
                        files.addAll(fileList);
                }
            }
            return files;
        }catch (Exception e){
            Log.i("movie1113", e.getMessage());
            return null;
        }

    }

}
