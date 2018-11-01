package com.of.vmvideo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.of.vmvideo.R;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;

import io.vov.vitamio.toast.oneToast;
import io.vov.vitamio.widget.VideoView;

/**
 * 直播节目
 * Created by MR.XIE on 2018/9/19.
 */
public class DirectBroadActivity extends Activity implements VideoView.VideoCollect {

    private VideoView meettingVideoView;
    private ProgressBar progressBar;
    private TextView downloadRateView;
    private TextView loadRateView;
   //  private final  static String meettingPath="http://221.228.226.23/11/t/j/v/b/tjvbwspwhqdmgouolposcsfafpedmb/sh.yinyuetai.com/691201536EE4912BF7E4F1E2C67B8119.mp4";

//    rtmp://live.hkstv.hk.lxdns.com/live/hks
//    rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov
//    http://movie.ks.js.cn/flv/other/1_0.flv
//    http://live.hkstv.hk.lxdns.com/live/hks/playlist.m3u8
   //http://live.hkstv.hk.lxdns.com/live/hks/playlist.m3u8
    //http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8
    //rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov
    private final  static String meettingPath="rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Vitamio.isInitialized(this);
        setContentView(R.layout.activity_meeting);
        initView();
        initVideo();
    }

    private void initView() {
        meettingVideoView = (VideoView)this.findViewById(R.id.meettingVideoView);
        progressBar = (ProgressBar)this.findViewById(R.id.probar);
        downloadRateView = (TextView)this.findViewById(R.id.download_rate);
        loadRateView = (TextView)this.findViewById(R.id.load_rate);
        progressBar.setVisibility(View.VISIBLE);
        downloadRateView.setText("拼命加载中...");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void initVideo() {
        meettingVideoView.setMeettingPath(meettingPath);//设置直播的路径
        meettingVideoView.setVideoCollect(this);//为了更新收藏视图
        meettingVideoView.setBufferSize(100);
        meettingVideoView.requestFocus();
        meettingVideoView.setMediaController(null);
       meettingVideoView.setVideoPath(meettingPath);

     // meettingVideoView.setMediaController(new MediaController(this));
    meettingVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.setPlaybackSpeed(1.0f);
        }
    });
        //注册一个回调函数，在异步操作调用过程中发生错误时调用。例如视频打开失败。
        meettingVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                oneToast.showMessage(DirectBroadActivity.this,"直播连接失败");
                onBackPressed();//返回
                return true;
            }
        });
    }

    @Override
    public void updateView() {

    }

    @Override
    public void deleteOneVideoUpateView(int position, String path) {

    }

    @Override
    public void addOnInfoAndOnBufferingUpdate() {
        // 注册一个回调函数，在有警告或错误信息时调用。例如：开始缓冲、缓冲结束、下载速度变化。
        meettingVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what){
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                             meettingVideoView.pause();
                            progressBar.setVisibility(View.VISIBLE);
                            loadRateView.setText("");
                            downloadRateView.setText("");
                            downloadRateView.setVisibility(View.VISIBLE);
                            loadRateView.setVisibility(View.VISIBLE);
                        meettingVideoView.hideMediaControlAllTip();
                        break;
                    case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                        downloadRateView.setText(" "+extra+"kb/s"+" ");
                        meettingVideoView.hideMediaControlAllTip();
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        meettingVideoView.continuePlay();
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
        meettingVideoView.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                loadRateView.setText(percent+"%");
                meettingVideoView.hideMediaControlAllTip();
            }
        });
    }

    @Override
    public void deleteOnInfoAndOnBufferingUpdate() {
        progressBar.setVisibility(View.GONE);
        downloadRateView.setVisibility(View.GONE);
        loadRateView.setVisibility(View.GONE);
        meettingVideoView.setOnInfoListener(null);
        meettingVideoView.setOnBufferingUpdateListener(null);
    }

    @Override
    public void downloadVideo(int position) {

    }

    @Override
    public boolean ifDownloadLocalPlay(int position) {
      return  false;
    }

    @Override
    public boolean isTouchUse() {
        return false;
    }

    @Override
    public void showShare() {

    }


}
