package io.vov.vitamio.demo.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mob.MobSDK;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.bean.Video;
import io.vov.vitamio.demo.R;
import io.vov.vitamio.demo.adapter.ShareAdapter;
import io.vov.vitamio.demo.bean.ShareApplication;
import io.vov.vitamio.demo.broadcast.NetWorkChangeReceiver;
import io.vov.vitamio.demo.fragment.CollectFragment;
import io.vov.vitamio.demo.fragment.FilmFragment;
import io.vov.vitamio.demo.fragment.SdCardFragment;
import io.vov.vitamio.demo.localData.FileManger;
import io.vov.vitamio.demo.saveDate.SaveCollectFragment;
import io.vov.vitamio.demo.utils.HttpUtil;
import io.vov.vitamio.provider.VideoCollectOperation;
import io.vov.vitamio.toast.oneToast;
import io.vov.vitamio.utils.CommonUtils;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class PlayActivity extends Activity implements VideoView.VideoCollect, View.OnTouchListener, HttpUtil.LocalPlay, NetWorkChangeReceiver.NetWorkChange {
    private VideoView videoView;
    private ProgressBar progressBar;
    private TextView downloadRateView;
    private TextView loadRateView;
    private static List<Video> videoList=new ArrayList<>();//播放的视频列表
    private  int position=0;//要播放视频的下标
    private final String TAG="movie2";
    private RelativeLayout playRelativeLayout;
    private static boolean isExists=false;//判断播放界面是否存在
    private NetWorkChangeReceiver netWorkChangeReceiver;
    private boolean net_avaiable=false;//判断网络是否可用
    private RelativeLayout net_unavailable_layout;
    private TextView net_unavailable_tipText;
    private Button net_unavailable_button;
    private ImageView net_unavailable_back;
    private boolean isFirstLoad=true;//判断此活动是否第一次加载
    private Dialog bottomDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        //无标题
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//         //全屏
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Vitamio.isInitialized(this);
        MobSDK.init(this);
        setContentView(R.layout.play_video_fragment);
        if(getIntent()!=null){
             position= getIntent().getIntExtra("position",0);
        }
        //注册网络变化广播
        netWorkChangeReceiver = new NetWorkChangeReceiver();
        netWorkChangeReceiver.setNetWorkChange(this);
        net_avaiable= CommonUtils.net_avaiable(this);//获取当前的网络是否可用
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(FilmFragment.NET_CONNNECT_CHANGE);
         registerReceiver(netWorkChangeReceiver,intentFilter);
        ininView();
        initVideo();
        isExists=true;
        Log.i("movie2", "onCreate: 创建playAcitvity播放界面");
    }


    //网络可用
    @Override
    public void netAvailable() {
        net_avaiable=true;
    }

    //网络不可用
    @Override
    public void netUnAvailable() {
        net_avaiable=false;
        //判断当前的播放的视频是在线视频且没有本地下载,且此页面不是第一次加载（判断是否第一次加载是因为如果网络不可用时点击收藏界面的视频时，还没等搜索数据库判断在线视频是否已下载就执行此函数）

      if(videoView.currentVideoIsOnLineVideoAndNetUnavaiable()&&!isFirstLoad)
      {
          netUnAvailableShowView();
      }
    }

    //网络可用时需要显示的视图
    private  void netAvailableShowView(){
        net_unavailable_layout.setVisibility(View.GONE);
    }

    //网络不可用时需要显示的视图
    private  void netUnAvailableShowView(){
        //隐藏缓冲信息
        progressBar.setVisibility(View.GONE);
        downloadRateView.setVisibility(View.GONE);
        loadRateView.setVisibility(View.GONE);
       net_unavailable_layout.setVisibility(View.VISIBLE);
        //当前的播放的视频是在线视频且没有本地下载,且当前的网络不可用
        if(videoView.currentVideoIsOnLineVideoAndNetUnavaiable()){
            //隐藏控制按钮
            videoView.hideMediaControlAllTip();
            videoView.hideMediaControl();
            videoView.pause();
        }
        oneToast.showMessage(this,"当前网络不可用");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isExists=false;
        unregisterReceiver(netWorkChangeReceiver);
        Log.i("movie2", "onCreate: 摧毁playAcitvity播放界面");
    }

    public static void setVideoList(List<Video> videoList) {
        PlayActivity.videoList = videoList;
    }



    private void ininView() {
            videoView = (VideoView)this.findViewById(R.id.videoView);
            progressBar = (ProgressBar)this.findViewById(R.id.probar);
            downloadRateView = (TextView)this.findViewById(R.id.download_rate);
          loadRateView = (TextView)this.findViewById(R.id.load_rate);
        playRelativeLayout = (RelativeLayout)this.findViewById(R.id.playRelativeLayout);
        playRelativeLayout.setOnTouchListener(this);

        progressBar.setVisibility(View.VISIBLE);
        downloadRateView.setText("拼命加载中....");
        downloadRateView.setVisibility(View.VISIBLE);

        net_unavailable_layout = (RelativeLayout)this.findViewById(R.id.net_unavailable_layout);
        net_unavailable_back = (ImageView)this.findViewById(R.id.net_unavailable_back);
        net_unavailable_tipText = (TextView)this.findViewById(R.id.net_unavailable_tipText);
        net_unavailable_button = (Button)this.findViewById(R.id.net_unavailable_button);

    }
    private void initVideo() {
        // path= Environment.getExternalStorageDirectory().getPath()+"/Movies/"+"test.swf";
      //  Log.i(MainActivity.TAG, "initVideo:------------ "+path);
        // Uri uri=Uri.parse(path);
       /// videoView.setVideoURI(uri);
        if(videoList==null&&videoList.size()<=0){
            return;
        }
         videoView.setVideoCollect(this);//为了更新收藏视图
        videoView.setPosition(position);
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoList(new ArrayList<Video>(videoList));

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.next();
                videoView.setProgress();
            }
        });
        videoView.requestFocus();

        //注册一个回调函数，在视频预处理完成后调用。在视频预处理完成后被调用。此时视频的宽度、高度、宽高比信息已经获取到，此时可调用seekTo让视频从指定位置开始播放。
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setPlaybackSpeed(1.0f);
            }
        });

        //注册一个回调函数，在异步操作调用过程中发生错误时调用。例如视频打开失败。
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                //判断当前的播放的视频是在线视频且没有本地下载
                if(videoView.currentVideoIsOnLineVideoAndNetUnavaiable())
                {
                    if(CommonUtils.net_avaiable(getApplicationContext())){//如果当前网络可用
                        oneToast.showMessage(PlayActivity.this,"视频打开失败");
                        videoView.next();
                    }else{

                        videoView.pause();
                        netUnAvailableShowView();
                    }

                }else{
                    oneToast.showMessage(PlayActivity.this,"视频打开失败");
                    videoView.next();
                }

                return true;
            }
        });

        net_unavailable_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            finish();//关闭此活动
            }
        });

        net_unavailable_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CommonUtils.net_avaiable(getApplication())){
                    netAvailableShowView();
                    videoView.resetSetVideoUri();//重新设置播放路径

                }
            }
        });

    }


    //更新视图
    @Override
    public void updateView() {
       CollectFragment collectFragment= (CollectFragment) SaveCollectFragment.getCollectFragment();
        if(collectFragment!=null){
         collectFragment.updateCollectFragmentView();
        }
    }

    //删除一个视频更新视图
    @Override
    public void deleteOneVideoUpateView(int position, String path) {
        CollectFragment collectFragment= (CollectFragment) SaveCollectFragment.getCollectFragment();
        SdCardFragment sdCardFragment= (SdCardFragment) SaveCollectFragment.getSdCardFragment();
        if(collectFragment!=null){
               collectFragment.deleteOneVideoUpateView(position,path);
        }
        if(sdCardFragment!=null){
           sdCardFragment.deleteOneVideoUpateView(position,path);
        }
    }

    //如果是网络视频则添加缓冲
    @Override
    public void addOnInfoAndOnBufferingUpdate() {
        if(!CommonUtils.net_avaiable(getApplication())){//如果当前没有网络
        return;
        }
        // 注册一个回调函数，在有警告或错误信息时调用。例如：开始缓冲、缓冲结束、下载速度变化。
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what){
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            videoView.bufferPause();
                            progressBar.setVisibility(View.VISIBLE);
                            loadRateView.setText("");
                            downloadRateView.setText("");
                            downloadRateView.setVisibility(View.VISIBLE);
                            loadRateView.setVisibility(View.VISIBLE);
                            videoView.hideMediaControlAllTip();//隐藏提示信息
                        break;
                    case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                        downloadRateView.setText(" "+extra+"kb/s"+" ");
                        videoView.hideMediaControlAllTip();
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        videoView.continuePlay();
                        progressBar.setVisibility(View.GONE);
                        downloadRateView.setVisibility(View.GONE);
                        loadRateView.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        //注册一个回调函数，在网络视频流缓冲变化时调用。
        videoView.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                loadRateView.setText(percent+"%");
                videoView.hideMediaControlAllTip();
            }
        });
    }

    //如果是本地视频则删除缓冲
    @Override
    public void deleteOnInfoAndOnBufferingUpdate() {
        progressBar.setVisibility(View.GONE);
        downloadRateView.setVisibility(View.GONE);
        loadRateView.setVisibility(View.GONE);
    videoView.setOnInfoListener(null);
    videoView.setOnBufferingUpdateListener(null);
    }

    @Override
    public void downloadVideo(int position) {
        startDownLoadVideo(position);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //当前的播放的视频是在线视频且没有本地下载,且当前的网络不可用
        //videoView.currentVideoIsOnLineVideoAndNetUnavaiable()&&!CommonUtils.net_avaiable(getApplicationContext())
        if(net_unavailable_layout.getVisibility()==View.VISIBLE){
            //隐藏控制按钮
            videoView.hideMediaControl();
            return true;
        }
        else{
            videoView.onTouchEvent(event);
        }

        return true;
    }


    /**
     * 请求用户给予悬浮窗的权限
     */
    public void askForPermission(int position){
        if(!Settings.canDrawOverlays(this)){
            oneToast.showMessage(this,"当前没有悬浮窗的权限，请授权");
            Intent intent=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));
            intent.putExtra("position",position);
            startActivityForResult(intent,1);
        }else{
            startDownLoadVideo(position);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1){
            if(!Settings.canDrawOverlays(this)){
                oneToast.showMessage(this,"权限授予失败，无法开启悬浮窗");
            }else{
              int position=data.getIntExtra("position",0);
                Log.i(TAG, "onActivityResult: "+position);
                startDownLoadVideo(position);
            }
        }
    }



    //返回值为true为此视频是本地视频，或者是在线视频但是本地已下载，返回值为false则此视频为在线视频但是此时网络不可用
    @Override
    public boolean ifDownloadLocalPlay(int position) {
        VideoCollectOperation videoCollectOperation=new VideoCollectOperation(this);
        String   videoSavePath= videoCollectOperation.getSavePathByDownloadPath(videoList.get(position).getVideoPath());
        Log.i(TAG, "ifDownloadLocalPlay: 下载的视频地址为:"+videoList.get(position).getVideoPath()+"//保存的地址为:"+videoSavePath);
        if(videoSavePath!=null&&new File(videoSavePath).exists()){//如果在线视频本地已下载,且存在
            File videoSavePathFile=new File(videoSavePath);
            // oneToast.showMessage(this,"此视频已下载");
            Video video=new Video();
            video.setVideoPath(videoSavePathFile.getAbsolutePath());
            video.setVideoName(videoSavePathFile.getName());
            video.setSize(FileManger.getSize(videoSavePathFile.getAbsolutePath()));
            video.setDate(FileManger.getDate(videoSavePathFile.getAbsolutePath()));
            video.setDuration(FileManger.getVideoDuration(videoSavePathFile.getAbsolutePath()));
            video.setProgress(videoList.get(position).getProgress());
            video.setThumbnail(FileManger.getVideoThumbnailThree(videoSavePathFile.getAbsolutePath(), MediaStore.Images.Thumbnails.MICRO_KIND));
            video.setNetworkVideoAddress(videoList.get(position).getVideoPath());
             videoView.localPlay(video,position);
            isFirstLoad=false;
             oneToast.showMessage(this,"此视频已下载,正在本地播放");
        }else{

            if(!net_avaiable){//如果网络不可用
                isFirstLoad=false;
                netUnAvailableShowView();
                return false;
            }
        }
        isFirstLoad=false;
        return true;
    }


    @Override
    public boolean isTouchUse() {
        if(net_unavailable_layout.getVisibility()==View.VISIBLE){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public void showShare() {

        bottomDialog = bottomDialog();
        bottomDialog.show();
    }


    /**
     * 点击分享
     *
     * @param position
     * 0为微信好友，1为朋友圈
     */
    public void share_click(int position){
      switch (position){
          case 0:
              share_WxFriend();
               if(bottomDialog!=null){
                   bottomDialog.cancel();
          }
              break;
          case 1:
              share_WxCircleFriend();
              if(bottomDialog!=null){
                  bottomDialog.cancel();
              }
              break;
              default:
                  break;
        }
    }
    private final static int SHARESUCCESS=1;
    private final static int SHAREERROR=2;
    private final static int SHARECANCEL=3;
   private Handler mhander=new Handler(){
       @Override
       public void handleMessage(Message msg) {
           switch (msg.what){
               case SHARESUCCESS:
                   oneToast.showMessage(getApplication(),"分享成功");
                   break;
               case SHAREERROR:
                   oneToast.showMessage(getApplication(),"分享错误");
                   break;
               case SHARECANCEL:
                   oneToast.showMessage(getApplication(),"分享取消");
                   break;
                   default:
                       break;

           }
       }
   };

    //分享到微信好友
    public void share_WxFriend(){

        // e21d939357cab3ca950c6b45e33bf88a
       int videoPosition=videoView.getCurrentVideoPosition();
        Wechat.ShareParams shareParams=new Wechat.ShareParams();
        shareParams.setShareType(Platform.SHARE_WEBPAGE);
        shareParams.setTitle(videoList.get(videoPosition).getVideoName());
        shareParams.setText("视频分享");

        shareParams.setUrl(videoList.get(videoPosition).getVideoPath());
        if(videoList.get(videoPosition).getThumbnailPath()!=null){
            shareParams.setImageUrl(videoList.get(videoPosition).getThumbnailPath());
        }else{
            shareParams.setImageData(videoList.get(videoPosition).getThumbnail());
        }
        Platform platform=ShareSDK.getPlatform(Wechat.NAME);

//        platform.setPlatformActionListener(new PlatformActionListener() {
//            @Override
//            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
//                   mhander.sendEmptyMessage(SHARESUCCESS);
//            }
//
//            @Override
//            public void onError(Platform platform, int i, Throwable throwable) {
//                mhander.sendEmptyMessage(SHAREERROR);
//            }
//
//            @Override
//            public void onCancel(Platform platform, int i) {
//                mhander.sendEmptyMessage(SHARECANCEL);
//            }
//        });

        platform.share(shareParams);
        Log.i(TAG, "share_WxFriend: "+"//"+"微信好友"+"//"+videoPosition+"//"+videoList.get(videoPosition).getVideoName()
                +"//"+videoList.get(videoPosition).getVideoPath()+"//"+videoList.get(videoPosition).getThumbnailPath());
    }
    //分享到微信朋友圈
    public void share_WxCircleFriend(){
        int videoPosition=videoView.getCurrentVideoPosition();
        WechatMoments.ShareParams shareParams=new WechatMoments.ShareParams();
        shareParams.setShareType(Platform.SHARE_WEBPAGE);
        shareParams.setTitle(videoList.get(videoPosition).getVideoName());
        shareParams.setText("视频分享");
        shareParams.setUrl(videoList.get(videoPosition).getVideoPath());
        if(videoList.get(videoPosition).getThumbnailPath()!=null){
            shareParams.setImageUrl(videoList.get(videoPosition).getThumbnailPath());
        }else{
            shareParams.setImageData(videoList.get(videoPosition).getThumbnail());
        }
        Platform platform=ShareSDK.getPlatform(WechatMoments.NAME);

//        platform.setPlatformActionListener(new PlatformActionListener() {
//            @Override
//            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
//                mhander.sendEmptyMessage(SHARESUCCESS);
//            }
//
//            @Override
//            public void onError(Platform platform, int i, Throwable throwable) {
//                mhander.sendEmptyMessage(SHAREERROR);
//            }
//
//            @Override
//            public void onCancel(Platform platform, int i) {
//                mhander.sendEmptyMessage(SHARECANCEL);
//            }
//        });
        platform.share(shareParams);

        Log.i(TAG, "share_WxFriend: "+"//"+"微信好友"+"//"+videoPosition+"//"+videoList.get(videoPosition).getVideoName()
                +"//"+videoList.get(videoPosition).getVideoPath()+"//"+videoList.get(videoPosition).getThumbnailPath());
    }
     //分享弹出窗
    public Dialog bottomDialog(){
        View view = LayoutInflater.from(this).inflate(R.layout.buttom_dialog, null);
        RecyclerView share_recyclerview=(RecyclerView)view.findViewById(R.id.share_recyclerview);
        final  int count=4;//设置一行显示几个应用
        final List<ShareApplication> shareApplications=new ArrayList<>();
        final  ShareApplication wechat=new ShareApplication(R.drawable.ssdk_oks_classic_wechat,"微信好友");
        final ShareApplication wechatmoments=new ShareApplication(R.drawable.ssdk_oks_classic_wechatmoments,"微信朋友圈");
        shareApplications.add(wechat);
        shareApplications.add(wechatmoments);

        ShareAdapter shareAdapter=new ShareAdapter(this,shareApplications);
        shareAdapter.setOnItemclickListener(new ShareAdapter.onItemclickListener() {
            @Override
            public void onClick(View view, int position) {
                share_click(position);
            }
        });
        StaggeredGridLayoutManager  staggeredGridLayoutManager=new StaggeredGridLayoutManager(count,StaggeredGridLayoutManager.VERTICAL);
        share_recyclerview.setLayoutManager(staggeredGridLayoutManager);
        share_recyclerview.setAdapter(shareAdapter);
        Dialog dialog = new Dialog(this, R.style.MyDialog);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.dialogAnimation);
        window.setContentView(view);

        WindowManager.LayoutParams lp = window.getAttributes(); // 获取对话框当前的参数值
        lp.width = getResources().getDisplayMetrics().widthPixels/2;//宽度占屏幕一半
        lp.height =WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        return dialog;
    }

    public void startDownLoadVideo(int position){
        if(!net_avaiable){//如果网络不可用
            netUnAvailableShowView();
            return;
        }
        if(HttpUtil.downloadingVideoPath.contains(videoList.get(position).getVideoPath())){
            oneToast.showMessage(this,"此视频正在下载");
            return;
        }
        VideoCollectOperation videoCollectOperation=new VideoCollectOperation(this);
       String   videoSavePath= videoCollectOperation.getSavePathByDownloadPath(videoList.get(position).getVideoPath());
        Log.i(TAG, "startDownLoadVideo: 下载的视频地址为:"+videoList.get(position).getVideoPath()+"//保存的地址为:"+videoSavePath);
        if(videoSavePath!=null&&new File(videoSavePath).exists()){//如果在线视频本地已下载,且存在
            File videoSavePathFile=new File(videoSavePath);
           // oneToast.showMessage(this,"此视频已下载");
            Video video=new Video();
            video.setVideoPath(videoSavePathFile.getAbsolutePath());
            video.setVideoName(videoSavePathFile.getName());
            video.setSize(FileManger.getSize(videoSavePathFile.getAbsolutePath()));
            video.setDate(FileManger.getDate(videoSavePathFile.getAbsolutePath()));
            video.setDuration(FileManger.getVideoDuration(videoSavePathFile.getAbsolutePath()));
            video.setThumbnail(FileManger.getVideoThumbnailThree(videoSavePathFile.getAbsolutePath(), MediaStore.Images.Thumbnails.MICRO_KIND));
            video.setNetworkVideoAddress(videoList.get(position).getVideoPath());
            videoView.downLoadLocalPlay(video,position);
          oneToast.showMessage(this,"此视频已下载,正在本地播放");
        }else{
            oneToast.showMessage(this,"开始下载");
            HttpUtil httpUtil=new HttpUtil(this,position);
            httpUtil.setLocalPlay(this);
            httpUtil.sendHttpRequest(videoList.get(position).getVideoPath(),videoList.get(position).getVideoName());
        }
    }
    //下载完之后开始本地播放
    public void startLocalPlay(int position){
        if(!isExists){//如果不存在播放界面
            return;
        }
        VideoCollectOperation videoCollectOperation=new VideoCollectOperation(this);
        String   videoSavePath= videoCollectOperation.getSavePathByDownloadPath(videoList.get(position).getVideoPath());
        Log.i(TAG, "下载完之后开始本地播放地址: "+videoSavePath);
        if(videoSavePath!=null&&new File(videoSavePath).exists()){//如果在线视频本地已下载,且存在
            File videoSavePathFile=new File(videoSavePath);
            // oneToast.showMessage(this,"此视频已下载");
            Video video=new Video();
            video.setVideoPath(videoSavePathFile.getAbsolutePath());
            video.setVideoName(videoSavePathFile.getName());
            video.setSize(FileManger.getSize(videoSavePathFile.getAbsolutePath()));
            video.setDate(FileManger.getDate(videoSavePathFile.getAbsolutePath()));
            video.setDuration(FileManger.getVideoDuration(videoSavePathFile.getAbsolutePath()));
            video.setThumbnail(FileManger.getVideoThumbnailThree(videoSavePathFile.getAbsolutePath(), MediaStore.Images.Thumbnails.MICRO_KIND));
            video.setNetworkVideoAddress(videoList.get(position).getVideoPath());
            videoView.downLoadLocalPlay(video,position);
        }
    }




    @Override
    public void localPlay(int position) {
        startLocalPlay(position);
    }


}

