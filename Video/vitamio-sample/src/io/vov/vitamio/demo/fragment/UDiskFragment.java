package io.vov.vitamio.demo.fragment;

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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
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
import io.vov.vitamio.demo.R;
import io.vov.vitamio.demo.activity.PlayActivity;
import io.vov.vitamio.demo.adapter.UdiskAdapter;
import io.vov.vitamio.demo.localData.FileManger;
import io.vov.vitamio.demo.utils.VideoDownload;
import io.vov.vitamio.toast.oneToast;


/**
 * Created by MR.XIE on 2018/9/7.
 */
public class UDiskFragment extends Fragment {

    private static TextView uDiskTipMessage;
    public static final String TAG="movie2";

    public final  static String  USB_DEVICE_ATTACHED="android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public final  static String USB_DEVICE_DETACHED="android.hardware.usb.action.USB_DEVICE_DETACHED";
    public final  static  String  USB_MEDIA_MOUNTED="android.intent.action.MEDIA_MOUNTED";
    public final  static String USB_MEDIA_UNMOUNTED="android.intent.action.MEDIA_UNMOUNTED";

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
    private final  static String UDISK_MOUNT_ADDRESS="/mnt/usb";//u盘的挂载地址（手动挂载地址）"/mnt/sdcard/mnt/usb"
    private static String UDISK_MOUNT_POINT=null;//U盘的挂载点
    private final static String UDISK_PROC_MOUNTS="/proc/mounts";//系统中挂载的所有目录
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

    public static void setmContext(Context mContext) {
        UDiskFragment.mContext = mContext;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.u_disk_fragment,container,false);
        initView(view);
        initOnclickListener();

        if(videoList.size()>0){
            uDiskTipMessage.setVisibility(View.GONE);
            udiskAdapter = new UdiskAdapter(videoList,this.getContext());
            videoListView.setAdapter(udiskAdapter);
        }else{

            uDiskTipMessage.setVisibility(View.VISIBLE);
            uDiskTipMessage.callOnClick();
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
                          if (getUdiskAudioList()) {
                              mhandler.sendEmptyMessage(REFRESH);
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
                        if(getUdiskAudioList()){
                            mhandler.sendEmptyMessageDelayed(REFRESH,500);
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
            }else if(action.equals(USB_DEVICE_DETACHED)){
                mhandler.sendEmptyMessage(UDISK_DETACHED);
                oneToast.showMessage(context,"设备拔出");
            }
            else if(action.equals(ACTION_USB_PERMISSION)){
                synchronized (this){
                    UsbDevice usbDevice=intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                     if(usbDevice!=null){
                         if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false)){

                             readDevice(context,usbDevice);
                         }else{
                             oneToast.showMessage(context,"获取权限失败");
                         }
                     }
                }
            }
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

    //获取权限
//    private synchronized static void getPermission(final Context context, final UsbDevice usbDevice) {
//       if(usbManager.hasPermission(usbDevice)){//如果有读取权限
//           readDevice(context,usbDevice);//读取U盘内容
//           return;
//       }else{
//           if(alertDialog==null){
//               AlertDialog.Builder builder=new AlertDialog.Builder(context).setTitle("U盘读取权限不可用").setMessage("由于Video需要读取U盘信息;\n否则，无法加载U盘里面的歌曲")
//                       .setCancelable(false).setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
//                           @Override
//                           public void onClick(DialogInterface dialog, int which) {
//                               PendingIntent pendingIntent=PendingIntent.getBroadcast(context,1,new Intent(ACTION_USB_PERMISSION),0);
//                               usbManager.requestPermission(usbDevice,pendingIntent);
//                           }
//                       }).setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
//                           @Override
//                           public void onClick(DialogInterface dialog, int which) {
//                               if(alertDialog!=null&&alertDialog.isShowing()){
//                                   alertDialog.cancel();
//                               }
//                           oneToast.showMessage(context,"获取权限失败");
//                           }
//                       });
//               alertDialog=builder.create();
//               alertDialog.show();
//           }else{
//               alertDialog.show();
//           }
//
//       }
//    }

    //读取U盘信息(开启子线程读取）
    private synchronized static void readDevice(final Context context, final UsbDevice usbDevice) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //获取U盘的挂载点
                UDISK_MOUNT_POINT=getUdiskMountPoint();

                //手动挂载U盘地址
                if(UDISK_MOUNT_POINT!=null){
                    if(handMountUdiskAddress(context)){
                        Log.i(TAG, "U盘挂载成功");
                        if(getUdiskAudioList()){
                            Log.i(TAG, "获取U盘信息成功");
                            for(Video video:videoList){
                                Log.i(TAG, video.toString());
                            }
                            if(isStart){
                                mhandler.sendEmptyMessage(LOADVIDEOLIST);
                            }

                        }else{
                            Log.i(TAG, "获取U盘信息失败");
                        }
                    }else{
                        Log.i(TAG, "U盘挂载失败");
                    }
                }
            }
        }).start();


       // getUDiskMountAddress(context);
//       new Thread(new Runnable() {
//           @Override
//           public void run() {
//               readDeviceList(context);//用别人方法读取U盘列表
//               UsbMassStorageDevice usbMassStorageDevice=getUsbMass(usbDevice);
//               if(usbMassStorageDevice==null){
//                   mhandler.sendEmptyMessage(readFail);
//               }else{
//                   if(readDeviceMessage(usbMassStorageDevice)){
//                       mhandler.sendEmptyMessage(readSuccess);
//                   }else{
//                       mhandler.sendEmptyMessage(readFail);
//                   }
//
//               }
//           }
//       }).start();
    }

    //获取U盘的挂载点
    private static String getUdiskMountPoint(){

       final String address="/proc/partitions";
       final String prefixAddress="/dev/block/";
       String uAddress=null;
        File file=new File(address);
        if(!file.exists()){
            return null;
        }else{
            InputStreamReader reader=null;
            BufferedReader bufferedReader=null;
            try {
                reader=new InputStreamReader(new FileInputStream(file));
               bufferedReader=new BufferedReader(reader);
               String lineText=null;
               while((lineText=bufferedReader.readLine())!=null){
                   if(lineText.contains("sd")){
                       uAddress=lineText.substring(lineText.lastIndexOf("sd")).trim();
                   }
               }
               return prefixAddress+uAddress;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
       return null;
    }

    //手动挂载U盘地址
    private static boolean handMountUdiskAddress(Context context){
     if(UDISK_MOUNT_POINT==null){
         return false;
     }
     return  true;
//         try {
//
//              Runtime runtime=Runtime.getRuntime();
////             final Process process=runtime.exec("su root");
////             try {
////                 new Thread(new Runnable() {
////                     @Override
////                     public void run() {
////
////                         InputStream inputStream=process.getErrorStream();
////                         BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
////                         String line=null;
////                         try {
////                             while((line=bufferedReader.readLine())!=null){
////                                 Log.i(TAG, "handMountUdiskAddress: 1000");
////                             }
////                         }catch (Exception e){
////                             e.printStackTrace();
////                         }finally {
////                             try {
////                                 inputStream.close();
////                             } catch (IOException e) {
////                                 e.printStackTrace();
////                             }
////                         }
////
////                     }
////                 }).start();
////
////                int result= process.waitFor();//等待输出结果
////                 Log.i(TAG, result+"/////");
////             } catch (InterruptedException e) {
////                 e.printStackTrace();
////                 return  false;
////             }
//
//             if(!new File(UDISK_MOUNT_ADDRESS).exists()){
//
//                 if(!new File(UDISK_MOUNT_ADDRESS).mkdirs()){
//                     return false;
//                 }
//             }
//             Log.i(TAG, "handMountUdiskAddress: 22");
//            Process process1= runtime.exec("mount "+UDISK_MOUNT_POINT+" "+UDISK_MOUNT_ADDRESS);
//             Log.i(TAG, "handMountUdiskAddress: 33");
//          InputStream inputStream1=process1.getErrorStream();
//          BufferedReader bufferedReader1=new BufferedReader(new InputStreamReader(inputStream1));
//          String line1=null;
//          while((line1=bufferedReader1.readLine())!=null){
//              Log.i(TAG, line1);
//          }
//             process1.waitFor();
//             Log.i(TAG, "mount "+UDISK_MOUNT_POINT+" "+UDISK_MOUNT_ADDRESS);
//             while(!isMountSuccess()){
//
//             }
//             return true;
//         } catch (IOException e) {
//             e.printStackTrace();
//             return false;
//         } catch (InterruptedException e) {
//             e.printStackTrace();
//             return false;
//         }
    }


    //判断是否挂载成功(需要判断 ”proc/mounts“ 文件中是否有U盘的挂载点路径）
    private static boolean isMountSuccess(){
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
            if(line.split(" ")[0].toLowerCase().contentEquals(UDISK_MOUNT_POINT)){//如果挂载点存在
                return  true;
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


    //获取U盘里面的视频列表
    private  synchronized  static  boolean getUdiskAudioList(){
        videoList.clear();
       File file=new File(UDISK_MOUNT_ADDRESS);
       if(!file.exists()||file.isFile()){
       return false;
       }else{
         File[] files=file.listFiles();
         for(File file1:files){
          if(file1.isFile()&&FileManger.isVideo(file1))
          {
              Video video=new Video();
              video.setVideoPath(file1.getAbsolutePath());
              video.setVideoName(file1.getName());
              video.setSize(FileManger.getSize(file1.getAbsolutePath()));
              video.setDate(FileManger.getDate(file1.getAbsolutePath()));
              video.setDuration(FileManger.getVideoDuration(file1.getAbsolutePath()));
              video.setThumbnail(FileManger.getVideoThumbnailThree(file1.getAbsolutePath(), MediaStore.Images.Thumbnails.MICRO_KIND));
              videoList.add(video);
          }
          else if(file1.isDirectory()){
              List<File> fileList=getAllFiles(file1);//获取一个目录下的所有可支持的视频文件,且视频不存在列表中
              if(fileList==null){
                  continue;
              }
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

          }
         }
       }
       return true;
    }

    public static List<File> getAllFiles(File file){//获取一个目录下的所有可支持的视频文件
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
    }



























    //获取U盘的挂载地址
    private static String getUDiskMountAddress(Context context){

       String address=null;
      StorageManager storageManager= (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
      try{
        Class storeManagerClazz=Class.forName("android.os.storage.StorageManager");
        Method getVolumesMethod=storeManagerClazz.getMethod("getVolumes");
        List<?>  volumeInfos= (List<?>) getVolumesMethod.invoke(storageManager);
        Class volumeInfoClazz=Class.forName("android.os.storage.VolumeInfo");
        Method getTypeMethod=volumeInfoClazz.getMethod("getType");
        Method getFsUuidMethod=volumeInfoClazz.getMethod("getFsUuid");

        Field fsTypeField=volumeInfoClazz.getDeclaredField("fsType");
        Field fsLabelField=volumeInfoClazz.getDeclaredField("fsLabel");
          Field pathField=volumeInfoClazz.getDeclaredField("path");
          Field  intetnalPath=volumeInfoClazz.getDeclaredField("internalPath");
          if(volumeInfos!=null){
              for(Object volumeInfo:volumeInfos){
                  Log.i(TAG, "getUDiskMountAddress: "+volumeInfo.toString());
                  String uuid= (String) getFsUuidMethod.invoke(volumeInfo);
                  Log.i(TAG, "getUDiskMountAddress: "+(volumeInfo==null));
                  if(uuid!=null){
                      String fsTypeString= (String) fsTypeField.get(volumeInfo);//U盘类型
                      String fsLabelString= (String) fsLabelField.get(volumeInfo);//U盘名称
                      String pathString= (String) pathField.get(volumeInfo);//U盘路径
                      String internalPathString= (String) intetnalPath.get(volumeInfo);//U盘路径
                      StatFs statFs=new StatFs(pathString);
                      long avainleSize=statFs.getAvailableBytes();//U盘可用空间
                      long totalSize=statFs.getTotalBytes();//U盘总空间
                      Log.i(TAG, "U盘类型:"+fsTypeString+"\n"+"U盘名称:"+fsLabelString+"\n"+"U盘路径:"+pathString+"\n"+"U盘路径:"+internalPathString+"\n"+
                              "U盘可用空间:"+avainleSize+"\n"+  "U盘总空间:"+totalSize+"\n");
                  }
              }
          }
      }catch (Exception e){
          e.printStackTrace();
          return null;
      }
      return address;
    }


    private  synchronized  static void readDeviceList(Context context){
        storageDevices=UsbMassStorageDevice.getMassStorageDevices(context);
    }

    //获取U盘列表中的UsbMassStorageDevice对象
    private static  UsbMassStorageDevice getUsbMass(UsbDevice usbDevice){
       if(storageDevices==null||storageDevices.length==0){
           return  null;
       }else{
           for(UsbMassStorageDevice device:storageDevices){
               if(device.getUsbDevice().equals(usbDevice)){
                   return device;
               }
           }
           return null;
       }
    }






    //读取U盘的信息
    private static boolean readDeviceMessage(UsbMassStorageDevice device){
       device=null;
       if(device==null){
           return false;
       }
       try{

           device.init();//初始化
            //获取分区
           List<Partition> partitions=device.getPartitions();
           if(partitions.size()==0){//读取分区失败，只支持FAT格式，不支持NTFS格式，exfat格式不知道
               Log.i(TAG, "读取分区失败");
               return false;
           }
           //仅使用第一分区
           FileSystem fileSystem=partitions.get(0).getFileSystem();
           UsbFile root=fileSystem.getRootDirectory();//设置当前文件对象为根目录
           if(readFile(root)){
               return true;
           }else{
               return false;
           }
       }catch (Exception e){
           e.printStackTrace();
           return false;
       }finally {
           device.getPartitions().stream().close();
       }
    }

    //读取文件
    private static boolean readFile(UsbFile root) {
        try{
            ArrayList<UsbFile> usbFiles = new ArrayList<>(Arrays.asList(root.listFiles()));
            Collections.sort(usbFiles,new Comparator<UsbFile>(){//简单排序，文件夹在前，文件在后
                @Override
                public int compare(UsbFile o1, UsbFile o2) {
                    if(o1.isDirectory()){
                        return -1;
                    }else
                    {
                        return 1;
                    }
                }
            });
            videoList.clear();//播放列表清空
          final String savePathPrefix= VideoDownload.initVideoPathPrefix()+"USB/";
           if(!new File(savePathPrefix).exists()){
               new File(savePathPrefix).mkdirs();
           }
            for(UsbFile usbFile:usbFiles){
              if(usbFile.isDirectory()){
                ArrayList<UsbFile> usbFileArrayList=getDirectoryUsbFile(usbFile);
                if(usbFileArrayList==null){
                    continue;
                }else{
                    for(UsbFile usbFile1:usbFileArrayList){
                        String path=getUsbFilePath(usbFile1);
                        if(path!=null){
                          //  int index=1;
                            Log.i(TAG, usbFile.getLength()+"");
                         String savePath= savePathPrefix+path+usbFile1.getName();
                         while(new File(savePath).exists()){
                             continue;
//                           savePath=savePathPrefix+path+index+"_"+usbFile1.getName();
//                           index++;
                         }

                         if(!new File(savePathPrefix+path).exists()){
                             new File(savePathPrefix+path).mkdirs();
                         }
                         Video video=new Video();
                         video.setVideoPath(savePath);
                         video.setVideoName(usbFile1.getName());
                         videoList.add(video);
                            if(copyFile(savePath,usbFile1)){//复制视频到本地成功
                                Log.i(TAG, savePath+" 复制成功");
                            }else{
                                Log.i(TAG, savePath+" 复制失败");
                            }
                        }
                    }
                }
              }else{
                  if(FileManger.isVideoPath(usbFile.getName())){
                      String path=getUsbFilePath(usbFile);
                      if(path!=null){
                         // int index=1;
                          String savePath= savePathPrefix+path+usbFile.getName();
                          while(new File(savePath).exists()){
                              continue;
                            // savePath=savePathPrefix+path+index+"_"+usbFile.getName();
                            //  index++;
                          }
                          if(!new File(savePathPrefix+path).exists()){
                              new File(savePathPrefix+path).mkdirs();
                          }
                          Video video=new Video();
                          video.setVideoPath(savePath);
                          video.setVideoName(usbFile.getName());
                          videoList.add(video);
                          if(copyFile(savePath,usbFile)){//复制视频到本地成功
                              Log.i(TAG, savePath+" 复制成功");
                          }else{
                              Log.i(TAG, savePath+" 复制失败");
                          }
                      }
                      }
                  }

              }
            return true;
        }catch (Exception e){
         e.printStackTrace();
         return false;
        }
    }

    //获取目录下的所有视频文件
    private static   ArrayList<UsbFile> getDirectoryUsbFile(UsbFile usbFile){
       if(usbFile==null||!usbFile.isDirectory()){
           return null;
       }else{
              ArrayList<UsbFile> usbFileArrayList=new ArrayList<>();
           try {
               ArrayList<UsbFile> usbFiles=new ArrayList<>(Arrays.asList(usbFile.listFiles()));
               for(UsbFile usbFile1:usbFiles){
                   if(usbFile1.isDirectory()){//如果是目录
                       usbFileArrayList.addAll(getDirectoryUsbFile(usbFile1));
                   }else{
                       if(FileManger.isVideoPath(usbFile1.getName()))//如果是可支持的视频文件
                       usbFileArrayList.add(usbFile1);
                   }
               }
               return  usbFileArrayList;
           } catch (IOException e) {
               e.printStackTrace();
               return null;
           }
       }
    }


    //文件获取相对于根目录的路径
    private static String getUsbFilePath(UsbFile usbFile){
       StringBuilder path= new StringBuilder();
     if(usbFile.isDirectory()){
         return null;
     }
     else{
         List<String> dirName=new ArrayList<>();
         UsbFile usbFile1=usbFile;
         while (usbFile1.getParent()!=null&&!usbFile1.getParent().isRoot())
         {
             dirName.add(usbFile1.getParent().getName());
             usbFile1=usbFile1.getParent();
         }

         for(int i=dirName.size()-1;i>=0;i--){
             path.append(dirName.get(i)).append("/");
         }
return path.toString();
     }
    }

    private static boolean copyFile(String saveLocalPath,UsbFile usbFile){
       if(usbFile.isDirectory()|| TextUtils.isEmpty(saveLocalPath)){
           return false;
       }
        FileOutputStream os=null;
        InputStream inputStream=null;
        File saveFile=new File(saveLocalPath);
        try {
            if(!saveFile.exists()){
               saveFile.createNewFile();
            }

            os=new FileOutputStream(saveFile,false);
             inputStream=new UsbFileInputStream(usbFile);
             int byresRead=0;
             byte[] buffer=new byte[1024*8];
             while((byresRead=inputStream.read(buffer))!=-1){
                 os.write(buffer,0,byresRead);
             }
             os.flush();
        } catch (Exception e) {
            e.printStackTrace();
            saveFile.delete();
            return false;
        }finally {
            if(os!=null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    //关闭流
    private void closeStream(){
       if(storageDevices==null||storageDevices.length==0){
           return;
       }
       else{
           for(UsbMassStorageDevice s:storageDevices){
               s.getPartitions().stream().close();
           }
       }
    }
}
