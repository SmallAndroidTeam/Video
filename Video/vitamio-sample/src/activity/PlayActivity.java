package activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fragment.CollectFragment;
import fragment.SdCardFragment;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.bean.Video;
import io.vov.vitamio.demo.R;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import saveDate.SaveCollectFragment;
import toast.oneToast;

public class PlayActivity extends Activity implements VideoView.VideoCollect, View.OnTouchListener {
    private VideoView videoView;
    private String path="http://www.modrails.com/videos/passenger_nginx.mov";
    private ProgressBar progressBar;
    private TextView downloadRateView;
    private TextView loadRateView;
    private static List<Video> videoList=new ArrayList<>();//播放的视频列表
    private  int position=0;//要播放视频的下标
    private final String TAG="movie";
    private RelativeLayout playRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        //无标题
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//         //全屏
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Vitamio.isInitialized(this);
        setContentView(R.layout.play_video_fragment);
        if(getIntent()!=null){
             position= getIntent().getIntExtra("position",0);
        }
        ininView();
        initVideo();

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
        videoView.setVideoList(videoList);

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
                oneToast.showMessage(PlayActivity.this,"视频打开失败");
                videoView.next();
                return true;
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
        // 注册一个回调函数，在有警告或错误信息时调用。例如：开始缓冲、缓冲结束、下载速度变化。
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what){
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            videoView.pause();
                            progressBar.setVisibility(View.VISIBLE);
                            loadRateView.setText("");
                            downloadRateView.setText("");
                            downloadRateView.setVisibility(View.VISIBLE);
                            loadRateView.setVisibility(View.VISIBLE);
                        break;
                    case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                        downloadRateView.setText(" "+extra+"kb/s"+" ");
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
            }
        });
    }

    //如果是本地视频则删除缓冲
    @Override
    public void deleteOnInfoAndOnBufferingUpdate() {
    videoView.setOnInfoListener(null);
    videoView.setOnBufferingUpdateListener(null);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        videoView.onTouchEvent(event);
        return true;
    }
}
