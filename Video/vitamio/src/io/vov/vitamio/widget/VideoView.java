/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2013 YIXIA.COM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vov.vitamio.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import io.vov.vitamio.MediaFormat;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.MediaPlayer.OnSeekCompleteListener;
import io.vov.vitamio.MediaPlayer.OnTimedTextListener;
import io.vov.vitamio.MediaPlayer.OnVideoSizeChangedListener;
import io.vov.vitamio.MediaPlayer.TrackInfo;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.bean.Video;
import io.vov.vitamio.provider.VideoCollectOperation;
import io.vov.vitamio.toast.oneToast;
import io.vov.vitamio.utils.CommonUtils;
import io.vov.vitamio.utils.Log;
import io.vov.vitamio.utils.ScreenResolution;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Displays a video file. The VideoView class can load images from various
 * sources (such as resources or content providers), takes care of computing its
 * measurement from the video so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting.
 * <p/>
 * VideoView also provide many wrapper methods for
 * {@link io.vov.vitamio.MediaPlayer}, such as {@link #getVideoWidth()},
 * {@link #setTimedTextShown(boolean)}
 */
public class VideoView extends SurfaceView implements MediaController.MediaPlayerControl {
  public static final int VIDEO_LAYOUT_ORIGIN = 0;
  public static final int VIDEO_LAYOUT_SCALE = 1;
  public static final int VIDEO_LAYOUT_STRETCH = 2;
  public static final int VIDEO_LAYOUT_ZOOM = 3;
  public static final int VIDEO_LAYOUT_FIT_PARENT = 4;
  private static final int STATE_ERROR = -1;
  private static final int STATE_IDLE = 0;
  private static final int STATE_PREPARING = 1;
  private static final int STATE_PREPARED = 2;
  private static final int STATE_PLAYING = 3;
  private static final int STATE_PAUSED = 4;
  private static final int STATE_PLAYBACK_COMPLETED = 5;
  private static final int STATE_SUSPEND = 6;
  private static final int STATE_RESUME = 7;
  private static final int STATE_SUSPEND_UNSUPPORTED = 8;
  public final static String TAG="movie";
  private  List<Video> videoList=new ArrayList<>();
  private int position=0;
  OnVideoSizeChangedListener mSizeChangedListener = new OnVideoSizeChangedListener() {
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
      Log.d("onVideoSizeChanged: (%dx%d)", width, height);
      mVideoWidth = mp.getVideoWidth();
      mVideoHeight = mp.getVideoHeight();
      mVideoAspectRatio = mp.getVideoAspectRatio();
      if (mVideoWidth != 0 && mVideoHeight != 0)
        setVideoLayout(mVideoLayout, mAspectRatio);
    }
  };
  OnPreparedListener mPreparedListener = new OnPreparedListener() {
    public void onPrepared(MediaPlayer mp) {
      Log.d("onPrepared");
      mCurrentState = STATE_PREPARED;
      // mTargetState = STATE_PLAYING;

      // Get the capabilities of the player for this stream
      //TODO mCanPause

     if(videoList.size()>0){
       if(!CommonUtils.isNetUrl(videoList.get(position).getVideoPath())) {
         if(videoList.get(position).getNetworkVideoAddress()==null) {//如果不是网络地址
           if(mMediaController!=null){
             mMediaController.hideDownloadAndShareIcon();
           }
           if(videoCollect!=null)
             videoCollect.deleteOnInfoAndOnBufferingUpdate();//删除缓冲
         }else{
           if(mMediaController!=null){
             mMediaController.setDownloadNoAvailableShareIconEnable();
           }
           if(videoCollect!=null)
             videoCollect.deleteOnInfoAndOnBufferingUpdate();//删除缓冲
         }
          setBufferSize(0);
       }else{
           setBufferSize(100);
         if(mMediaController!=null) {
           mMediaController.showDownloadAndShareIcon();
         }
         if(videoCollect!=null){
           videoCollect.addOnInfoAndOnBufferingUpdate();//添加缓冲
           if(!videoCollect.ifDownloadLocalPlay(position)){//此时网络不可用并且此视频为在线视频
             return;
           }
         }

       }
         mMediaPlayer.seekTo((long) (1.0*videoList.get(position).getProgress()*videoList.get(position).getDuration()/1000));
         if(mMediaController!=null){
           mMediaController.setProgressRightSlide(videoList.get(position).getProgress());
         }
         android.util.Log.i("movie2", "start: "+videoList.get(position).getProgress()+"//"+videoList.get(position).getDuration());
     }



      if (mOnPreparedListener != null)
        mOnPreparedListener.onPrepared(mMediaPlayer);
      if (mMediaController != null)
        mMediaController.setEnabled(true);
      mVideoWidth = mp.getVideoWidth();
      mVideoHeight = mp.getVideoHeight();
      mVideoAspectRatio = mp.getVideoAspectRatio();

      long seekToPosition = mSeekWhenPrepared;
      if (seekToPosition != 0)
        seekTo(seekToPosition);

      if (mVideoWidth != 0 && mVideoHeight != 0) {
        setVideoLayout(mVideoLayout, mAspectRatio);
        if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
          if (mTargetState == STATE_PLAYING) {
            start();
            if (mMediaController != null)
              mMediaController.show();
          } else if (!isPlaying() && (seekToPosition != 0 || getCurrentPosition() > 0)) {
            if (mMediaController != null)
              mMediaController.show(0);
          }
        }
      } else if (mTargetState == STATE_PLAYING) {
        start();
      }
    }
  };
  SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
      mSurfaceWidth = w;
      mSurfaceHeight = h;
      boolean isValidState = (mTargetState == STATE_PLAYING);
      boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
      if (mMediaPlayer != null && isValidState && hasValidSize) {
        if (mSeekWhenPrepared != 0)
          seekTo(mSeekWhenPrepared);
        start();
        if (mMediaController != null) {
          if (mMediaController.isShowing())
            mMediaController.hide();
          mMediaController.show();
        }
      }
    }

    public void surfaceCreated(SurfaceHolder holder) {
      mSurfaceHolder = holder;
      if (mMediaPlayer != null && mCurrentState == STATE_SUSPEND && mTargetState == STATE_RESUME) {
        mMediaPlayer.setDisplay(mSurfaceHolder);
        resume();
      } else {
        openVideo();
      }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
      mSurfaceHolder = null;
      if (mMediaController != null) mMediaController.hide();
      release(true);
    }
  };
  private Uri mUri;
  private long mDuration;
  private int mCurrentState = STATE_IDLE;
  private int mTargetState = STATE_IDLE;
  private float mAspectRatio = 0;
  private int mVideoLayout = VIDEO_LAYOUT_SCALE;
  private SurfaceHolder mSurfaceHolder = null;
  private MediaPlayer mMediaPlayer = null;
  private int mVideoWidth;
  private int mVideoHeight;
  private float mVideoAspectRatio;
  private int mVideoChroma = MediaPlayer.VIDEOCHROMA_RGBA;
  private boolean mHardwareDecoder = false;
  private int mSurfaceWidth;
  private int mSurfaceHeight;
  private MediaController mMediaController;
  private View mMediaBufferingIndicator;
  private OnCompletionListener mOnCompletionListener;
  private OnPreparedListener mOnPreparedListener;
  private OnErrorListener mOnErrorListener;
  private OnSeekCompleteListener mOnSeekCompleteListener;
  private OnTimedTextListener mOnTimedTextListener;
  private OnInfoListener mOnInfoListener;
  private OnBufferingUpdateListener mOnBufferingUpdateListener;
  private int mCurrentBufferPercentage;
  private long mSeekWhenPrepared; // recording the seek position while preparing
  private Context mContext;
  private Map<String, String> mHeaders;
  private int mBufSize;
  private GestureDetector mGestureDetector;//手势监听器
  private String meettingPath=null;
  private OnCompletionListener mCompletionListener = new OnCompletionListener() {
    public void onCompletion(MediaPlayer mp) {
      Log.d("onCompletion");
      mCurrentState = STATE_PLAYBACK_COMPLETED;
      mTargetState = STATE_PLAYBACK_COMPLETED;
      if (mMediaController != null)
        mMediaController.hide();
      if (mOnCompletionListener != null)
        mOnCompletionListener.onCompletion(mMediaPlayer);
    }
  };
  private OnErrorListener mErrorListener = new OnErrorListener() {
    public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
      Log.d("Error: %d, %d", framework_err, impl_err);
      mCurrentState = STATE_ERROR;
      mTargetState = STATE_ERROR;
      if (mMediaController != null)
        mMediaController.hide();

      if (mOnErrorListener != null) {
        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err))
          return true;
      }

      if (getWindowToken() != null) {
        int message = framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK ? getResources().getIdentifier("VideoView_error_text_invalid_progressive_playback", "string", mContext.getPackageName()): getResources().getIdentifier("VideoView_error_text_unknown", "string", mContext.getPackageName());

        new AlertDialog.Builder(mContext).setTitle(getResources().getIdentifier("VideoView_error_title", "string", mContext.getPackageName())).setMessage(message).setPositiveButton(getResources().getIdentifier("VideoView_error_button", "string", mContext.getPackageName()), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            if (mOnCompletionListener != null)
              mOnCompletionListener.onCompletion(mMediaPlayer);
          }
        }).setCancelable(false).show();
      }
      return true;
    }
  };
  private OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
      mCurrentBufferPercentage = percent;
      if (mOnBufferingUpdateListener != null)
        mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
    }
  };
  private OnInfoListener mInfoListener = new OnInfoListener() {
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
      Log.d("onInfo: (%d, %d)", what, extra);
      if (mOnInfoListener != null) {
        mOnInfoListener.onInfo(mp, what, extra);
      } else if (mMediaPlayer != null) {
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
          mMediaPlayer.pause();
          if (mMediaBufferingIndicator != null)
            mMediaBufferingIndicator.setVisibility(View.VISIBLE);
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
          mMediaPlayer.start();
          if (mMediaBufferingIndicator != null)
            mMediaBufferingIndicator.setVisibility(View.GONE);
        }
      }
      return true;
    }
  };
  private OnSeekCompleteListener mSeekCompleteListener = new OnSeekCompleteListener() {
    @Override
    public void onSeekComplete(MediaPlayer mp) {
      Log.d("onSeekComplete");
      if (mOnSeekCompleteListener != null)
        mOnSeekCompleteListener.onSeekComplete(mp);
    }
  };
  private OnTimedTextListener mTimedTextListener = new OnTimedTextListener() {
    @Override
    public void onTimedTextUpdate(byte[] pixels, int width, int height) {
      Log.i("onSubtitleUpdate: bitmap subtitle, %dx%d", width, height);
      if (mOnTimedTextListener != null)
        mOnTimedTextListener.onTimedTextUpdate(pixels, width, height);
    }

    @Override
    public void onTimedText(String text) {
      Log.i("onSubtitleUpdate: %s", text);
      if (mOnTimedTextListener != null)
        mOnTimedTextListener.onTimedText(text);
    }
  };
    private VideoCollectOperation videoCollectOperation;


    public VideoView(Context context) {
    super(context);
    initVideoView(context);
  }

  public VideoView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
    initVideoView(context);
  }

  public VideoView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initVideoView(context);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
    int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
    setMeasuredDimension(width, height);
  }

  /**
   * Set the display options
   *
   * @param layout      <ul>
   *                    <li>{@link #VIDEO_LAYOUT_ORIGIN}
   *                    <li>{@link #VIDEO_LAYOUT_SCALE}
   *                    <li>{@link #VIDEO_LAYOUT_STRETCH}
   *                    <li>{@link #VIDEO_LAYOUT_ZOOM}
   *                    <li>{@link #VIDEO_LAYOUT_FIT_PARENT}
   *                    </ul>
   * @param aspectRatio video aspect ratio, will audo detect if 0.
   */
  public void setVideoLayout(int layout, float aspectRatio) {
    LayoutParams lp = getLayoutParams();
    Pair<Integer, Integer> res = ScreenResolution.getResolution(mContext);
    int windowWidth = res.first.intValue(), windowHeight = res.second.intValue();
    float windowRatio = windowWidth / (float) windowHeight;
    float videoRatio = aspectRatio <= 0.01f ? mVideoAspectRatio : aspectRatio;
    mSurfaceHeight = mVideoHeight;
    mSurfaceWidth = mVideoWidth;
    if (VIDEO_LAYOUT_ORIGIN == layout && mSurfaceWidth < windowWidth && mSurfaceHeight < windowHeight) {
      lp.width = (int) (mSurfaceHeight * videoRatio);
      lp.height = mSurfaceHeight;
    } else if (layout == VIDEO_LAYOUT_ZOOM) {
      lp.width = windowRatio > videoRatio ? windowWidth : (int) (videoRatio * windowHeight);
      lp.height = windowRatio < videoRatio ? windowHeight : (int) (windowWidth / videoRatio);
    } else if (layout == VIDEO_LAYOUT_FIT_PARENT) {
      ViewGroup parent = (ViewGroup) getParent();
      float parentRatio = ((float) parent.getWidth()) / ((float) parent.getHeight());
      lp.width = (parentRatio < videoRatio) ? parent.getWidth() : Math.round(((float) parent.getHeight()) * videoRatio);
      lp.height = (parentRatio > videoRatio) ? parent.getHeight() : Math.round(((float) parent.getWidth()) / videoRatio);
    } else {
      boolean full = layout == VIDEO_LAYOUT_STRETCH;
      lp.width = (full || windowRatio < videoRatio) ? windowWidth : (int) (videoRatio * windowHeight);
      lp.height = (full || windowRatio > videoRatio) ? windowHeight : (int) (windowWidth / videoRatio);
    }
    setLayoutParams(lp);
    getHolder().setFixedSize(mSurfaceWidth, mSurfaceHeight);
    Log.d("VIDEO: %dx%dx%f, Surface: %dx%d, LP: %dx%d, Window: %dx%dx%f", mVideoWidth, mVideoHeight, mVideoAspectRatio, mSurfaceWidth, mSurfaceHeight, lp.width, lp.height, windowWidth, windowHeight, windowRatio);
    mVideoLayout = layout;
    mAspectRatio = aspectRatio;
  }

  @SuppressWarnings("deprecation")
  private void initVideoView(Context ctx) {
    mContext = ctx;
    mVideoWidth = 0;
    mVideoHeight = 0;
    getHolder().setFormat(PixelFormat.RGBA_8888); // PixelFormat.RGB_565
    getHolder().addCallback(mSHCallback);
    // this value only use Hardware decoder before Android 2.3
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB && mHardwareDecoder) {
      getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    setFocusable(true);
    setFocusableInTouchMode(true);
    requestFocus();
    mCurrentState = STATE_IDLE;
    mTargetState = STATE_IDLE;
    if (ctx instanceof Activity)
      ((Activity) ctx).setVolumeControlStream(AudioManager.STREAM_MUSIC);

    //手势
      mGestureDetector = new GestureDetector(simpleOnGestureListener);
      setFocusable(true);
      setClickable(true);
      setLongClickable(true);
      videoCollectOperation = new VideoCollectOperation(ctx);
  }



  public boolean isValid() {
    return (mSurfaceHolder != null && mSurfaceHolder.getSurface().isValid());
  }

  public void setMeettingPath(String meettingPath) {
    this.meettingPath = meettingPath;
  }

  public void setVideoPath(String path) {

    if(meettingPath!=null&&meettingPath.contentEquals(path)){//如果地址为直播地址
      if(videoCollect!=null)
      videoCollect.addOnInfoAndOnBufferingUpdate();
    }

    setVideoURI(Uri.parse(path));
  }

  public void setVideoURI(Uri uri) {
    setVideoURI(uri, null);
  }

  public void setVideoURI(Uri uri, Map<String, String> headers) {
    mUri = uri;
    mHeaders = headers;
    mSeekWhenPrepared = 0;
    openVideo();
      try{
        requestLayout();
      }catch (Exception e){
        e.printStackTrace();
      }
    try{
      invalidate();
    }catch (Exception e){
      e.printStackTrace();
    }
    
  }

  public void stopPlayback() {
    if (mMediaPlayer != null) {
      mMediaPlayer.stop();
      mMediaPlayer.release();
      mMediaPlayer = null;
      mCurrentState = STATE_IDLE;
      mTargetState = STATE_IDLE;
    }
  }

  private void openVideo() {
    if (mUri == null || mSurfaceHolder == null || !Vitamio.isInitialized(mContext))
      return;
    Intent i = new Intent("com.android.music.musicservicecommand");
    i.putExtra("command", "pause");
    mContext.sendBroadcast(i);
    release(false);
    try {
      mDuration = -1;
      mCurrentBufferPercentage = 0;
      mMediaPlayer = new MediaPlayer(mContext, mHardwareDecoder);
      mMediaPlayer.setOnPreparedListener(mPreparedListener);
      mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
      mMediaPlayer.setOnCompletionListener(mCompletionListener);
      mMediaPlayer.setOnErrorListener(mErrorListener);
      mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
      mMediaPlayer.setOnInfoListener(mInfoListener);
      mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
      mMediaPlayer.setOnTimedTextListener(mTimedTextListener);
      mMediaPlayer.setDataSource(mContext, mUri, mHeaders);
      mMediaPlayer.setDisplay(mSurfaceHolder);
      mMediaPlayer.setBufferSize(mBufSize);
      mMediaPlayer.setVideoChroma(mVideoChroma == MediaPlayer.VIDEOCHROMA_RGB565 ? MediaPlayer.VIDEOCHROMA_RGB565 : MediaPlayer.VIDEOCHROMA_RGBA);
      mMediaPlayer.setScreenOnWhilePlaying(true);
      mMediaPlayer.prepareAsync();
      mCurrentState = STATE_PREPARING;
      attachMediaController();
    } catch (IOException ex) {
      Log.e("Unable to open content: " + mUri, ex);
      mCurrentState = STATE_ERROR;
      mTargetState = STATE_ERROR;
      mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
      return;
    } catch (IllegalArgumentException ex) {
      Log.e("Unable to open content: " + mUri, ex);
      mCurrentState = STATE_ERROR;
      mTargetState = STATE_ERROR;
      mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
      return;
    }
  }

  public void setMediaController(MediaController controller) {
    if (mMediaController != null)
      mMediaController.hide();
    mMediaController = controller;
    attachMediaController();
  }

  public void setMediaBufferingIndicator(View mediaBufferingIndicator) {
    if (mMediaBufferingIndicator != null)
      mMediaBufferingIndicator.setVisibility(View.GONE);
    mMediaBufferingIndicator = mediaBufferingIndicator;
  }

  private void attachMediaController() {
    if (mMediaPlayer != null && mMediaController != null) {
      mMediaController.setMediaPlayer(this);
      View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
      mMediaController.setAnchorView(anchorView);
      mMediaController.setEnabled(isInPlaybackState());
      //设置视频的名称
      if (mUri != null&&isHaveVideoList()) {
//        List<String> paths = mUri.getPathSegments();
//        String name = paths == null || paths.isEmpty() ? "null" : paths.get(paths.size() - 1);
        mMediaController.setFileName(videoList.get(position).getVideoName());
      }
    }
  }

  public void setOnPreparedListener(OnPreparedListener l) {
    mOnPreparedListener = l;
  }

  public void setOnCompletionListener(OnCompletionListener l) {
    mOnCompletionListener = l;
  }

  public void setOnErrorListener(OnErrorListener l) {
    mOnErrorListener = l;
  }

  public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {
    mOnBufferingUpdateListener = l;
  }

  public void setOnSeekCompleteListener(OnSeekCompleteListener l) {
    mOnSeekCompleteListener = l;
  }

  public void setOnTimedTextListener(OnTimedTextListener l) {
    mOnTimedTextListener = l;
  }

  public void setOnInfoListener(OnInfoListener l) {
    mOnInfoListener = l;
  }

  private void release(boolean cleartargetstate) {
    if (mMediaPlayer != null) {
      mMediaPlayer.reset();
      mMediaPlayer.release();
      mMediaPlayer = null;
      mCurrentState = STATE_IDLE;
      if (cleartargetstate)
        mTargetState = STATE_IDLE;
    }
  }

  //手势监听器的处理函数
  private GestureDetector.SimpleOnGestureListener simpleOnGestureListener=new GestureDetector.SimpleOnGestureListener(){
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      return super.onSingleTapUp(e);
    }

    @Override
    public void onLongPress(MotionEvent e) {
      if(mMediaController!=null){
        mMediaController.setVoideType();
      }
      super.onLongPress(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      return super.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public void onShowPress(MotionEvent e) {
      super.onShowPress(e);
    }

    @Override
    public boolean onDown(MotionEvent e) {
      return super.onDown(e);
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
      if (isInPlaybackState()){
        if (mMediaPlayer.isPlaying()) {
          pause();
          if(mMediaController!=null){
            mMediaController.onlyShowTipIcon(3);
            mMediaController.autoHideShowTipIcon();
          }
        } else {
          continuePlay();
          if(mMediaController!=null&&mMediaController.isShowing()){
            mMediaController.hideAllTip();
          }
        }
        if(mMediaController!=null){
 mMediaController.changePauseButton();
        }
      }

      return super.onDoubleTap(e);
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
      return super.onDoubleTapEvent(e);
    }


    //单击
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
      if (isInPlaybackState() && mMediaController != null){
       if(mMediaController.isShowing()){
         mMediaController.hide();
       }else{
         mMediaController.hidePlayModeListView();
         mMediaController.show();
       }
      }
      return super.onSingleTapConfirmed(e);
    }
  };


  private float mStartY,mStartX,mDownVoice,totalDistance,mDownProgress,mCurrentProgress;
  private int isShowTipTye=-1;//正在显示的提示信息，0代表的是音量，1代表的是亮度，2代表的播放进度，-1代表没显示
  @Override
  public boolean onTouchEvent(MotionEvent ev) {
   // android.util.Log.i("movie2", "onTouchEvent: 11");
    if(mMediaController==null){//如果没有显示控制按钮则没滑动没反应
      return true;
    }
    //android.util.Log.i("movie2", "onTouchEvent: 22");
      if(videoCollect!=null&&!videoCollect.isTouchUse()){
       mMediaController.hide();
       return true;
      }

    final int  mScreenWidth= CommonUtils.getScreenWidth(getContext());
    final int mScreentHeight=CommonUtils.getScreenHeight(getContext());
    final AudioManager audioManager= (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    final float mMaxVoice=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    mGestureDetector.onTouchEvent(ev);
    switch (ev.getAction()){
      case MotionEvent.ACTION_DOWN:

        mStartX=ev.getX();
        mStartY=ev.getY();
        mDownProgress=mMediaPlayer.getCurrentPosition();
        mDownVoice=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        totalDistance=Math.min(mScreenWidth,mScreentHeight);
        break;

        case MotionEvent.ACTION_MOVE:

          float mEndX=ev.getX();
          float mEndY=ev.getY();
          float distance=mStartY-mEndY;
          final double FLINGVERTICAL_MIN_DISTANCE=10;
          final  double FLING_HORIZONTAL_MIN_DISTANCE=5;
          float horizontalDistance=mEndX-mStartX;
          final double FLING_MIN_DISTANCE=3;

         // android.util.Log.i(TAG, "onTouchEvent: -----------"+horizontalDistance+"//"+distance);
          if(Math.abs(horizontalDistance)<FLING_MIN_DISTANCE&&Math.abs(distance)<FLING_MIN_DISTANCE) {//表示单击
           // android.util.Log.i(TAG, "onTouchEvent: 单击");
            break;
          }

          if(isShowTipTye!=-1){

            switch (isShowTipTye){
              case 0:
                if(mEndX>mScreenWidth/2){
                  float delVoice=(distance/totalDistance)*mMaxVoice;
                  float mCurrentVoice=Math.min(mMaxVoice,Math.max(0,mDownVoice+delVoice));
                  isShowTipTye=0;
                  if(mMediaController!=null){
                    mMediaController.changeVolumeByVoiceValue((int)mCurrentVoice);
                    mMediaController.setTipIconVocieProgress((int)mCurrentVoice);
                    mMediaController.onlyShowTipIcon(isShowTipTye);
                  }
                }
                break;
              case 1:
                if(mEndX<=mScreenWidth/2){
                  isShowTipTye=1;
                  if(mMediaController!=null){
                    float mBright=Math.min(1,Math.max(0,mMediaController.getCurrentBrigtness()+(distance/totalDistance)));
                    mMediaController.changeAppBrightness(mBright);
                    mMediaController.setTipIconBrightnessProgress(mBright);
                    mMediaController.onlyShowTipIcon(isShowTipTye);
                  }
                }
                break;
              case 2:
                float delProgress=(horizontalDistance/totalDistance)*mMediaPlayer.getDuration();
                 mCurrentProgress=Math.min(mMediaPlayer.getDuration(),Math.max(0,mDownProgress+delProgress));
                isShowTipTye=2;
                if(mMediaController!=null){
                  mMediaController.setProgressRightSlide((int)mCurrentProgress);
                  mMediaController.setTipPlayProgress(mCurrentProgress);
                  mMediaController.onlyShowTipIcon(isShowTipTye);
                }

                break;
            }
          }
          else{

            if(Math.abs(horizontalDistance)>FLING_HORIZONTAL_MIN_DISTANCE&&Math.abs(distance)<FLINGVERTICAL_MIN_DISTANCE){
              float delProgress=(horizontalDistance/totalDistance)*mMediaPlayer.getDuration();
               mCurrentProgress=Math.min(mMediaPlayer.getDuration(),Math.max(0,mDownProgress+delProgress));
              isShowTipTye=2;
              if(mMediaController!=null){
                mMediaController.setProgressRightSlide((int)mCurrentProgress);
                mMediaController.setTipPlayProgress(mCurrentProgress);
                mMediaController.onlyShowTipIcon(isShowTipTye);
              }

              break;
            }

            //左半边上滑改变亮度，右半边上滑改变音量，水平滑动改变进度
            if(mEndX>mScreenWidth/2){
              float delVoice=(distance/totalDistance)*mMaxVoice;
              float mCurrentVoice=Math.min(mMaxVoice,Math.max(0,mDownVoice+delVoice));
              isShowTipTye=0;
              if(mMediaController!=null){
                mMediaController.changeVolumeByVoiceValue((int)mCurrentVoice);
                mMediaController.setTipIconVocieMax();
                mMediaController.setTipIconVocieProgress((int)mCurrentVoice);
                mMediaController.onlyShowTipIcon(isShowTipTye);
              }

            }
            else{
              isShowTipTye=1;
              if(mMediaController!=null){
                float mBright=Math.min(1,Math.max(0,mMediaController.getCurrentBrigtness()+(distance/totalDistance)));
                mMediaController.changeAppBrightness(mBright);
                mMediaController.setTipIconBrightnessMax();
                mMediaController.setTipIconBrightnessProgress(mBright);
                mMediaController.onlyShowTipIcon(isShowTipTye);
              }
            }

          }
          break;
          case MotionEvent.ACTION_UP:
            if(isShowTipTye==2){
              mMediaPlayer.seekTo((int)mCurrentProgress);
            }
            isShowTipTye=-1;
           if(mMediaController!=null){
             mMediaController.autoHideShowTipIcon();
           }
            break;

    }
  return true;//返回值必须为true否则双击无效
  }

  @Override
  public boolean onTrackballEvent(MotionEvent ev) {
    if (isInPlaybackState() && mMediaController != null)
      toggleMediaControlsVisiblity();
    return false;
  }



  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {

    boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_MENU && keyCode != KeyEvent.KEYCODE_CALL && keyCode != KeyEvent.KEYCODE_ENDCALL;
    if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
      if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE) {
        if (mMediaPlayer.isPlaying()) {
          pause();
          if(mMediaController!=null){
            mMediaController.hidePlayModeListView();
            mMediaController.show();
          }


        } else {
          start();
          if(mMediaController!=null) {
            mMediaController.hide();
          }


        }
        return true;
      } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
        if (!mMediaPlayer.isPlaying()) {
            start();
          if(mMediaController!=null) {
            mMediaController.hide();
          }
        }
        return true;
      } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
        if (mMediaPlayer.isPlaying()) {
          pause();
          if(mMediaController!=null) {
            mMediaController.hidePlayModeListView();
            mMediaController.show();
          }
        }
        return true;
      } else if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN||keyCode==KeyEvent.KEYCODE_VOLUME_UP){
        if(mMediaController!=null) {

          mMediaController.changeVolume(keyCode);
        }

      }
      else {
        toggleMediaControlsVisiblity();
      }
    }

    return super.onKeyDown(keyCode, event);
  }

  private void toggleMediaControlsVisiblity() {
    if (mMediaController.isShowing()) {
      mMediaController.hide();
    } else {
      mMediaController.hidePlayModeListView();

      mMediaController.show();
    }
  }



  //设置音乐视频列表
  public void setVideoList(List<Video> videoList) {
    this.videoList = videoList;

      while(videoList.size()>0)
      {
        if(!CommonUtils.isNetUrl(videoList.get(position).getVideoPath())){//如果不是网络地址
          if(!new File(videoList.get(position).getVideoPath()).exists()){//如果该路径不存在
            oneToast.showMessage(this.getContext(),"该视频不存在");
            if(videoCollect!=null)
            videoCollect.deleteOneVideoUpateView(position,videoList.get(position).getVideoPath());
            videoList.remove(position);
            if(videoList.size()>0){
              position=position==0?(videoList.size()-1):(position-1);
            }else{
              return;
            }
            continue;
          }else{
            //android.util.Log.i(TAG, "setVideoList: ");
            if(mMediaController!=null){
              mMediaController.hideDownloadAndShareIcon();//隐藏下载和分享图标
            }
            if(videoCollect!=null)
            videoCollect.deleteOnInfoAndOnBufferingUpdate();//删除缓冲
            break;
          }
        }else{
          if(mMediaController!=null){
            mMediaController.showDownloadAndShareIcon();//显示下载和分享图标
          }
          if(videoCollect!=null)
          videoCollect.addOnInfoAndOnBufferingUpdate();//添加缓冲
          break;
        }
      }
          if(videoList.size()>0)
      setVideoURI(Uri.parse(videoList.get(position).getVideoPath()));
  }



//设置要播放的视频下标
  public void setPosition(int position) {
    this.position = position;
  }

  private long clickTime=0;
  private final  static int maxTimeInterval=2000;//二次按下最大的时间间隔
  @Override
  public void quit() {
    oneToast.hideToast();

    if (isInPlaybackState()) {
       if(clickTime==0){
         clickTime=System.currentTimeMillis();
         oneToast.showMessage(this.getContext(),"再按一次退出");
       }else{
         long currentTime=System.currentTimeMillis();
         if((currentTime-clickTime)>=maxTimeInterval){
           oneToast.showMessage(this.getContext(),"再按一次退出");
           clickTime=currentTime;
         }else{
           if(mMediaController!=null){
             mMediaController.unregisterReceiver();
           }
           videoList.get(position).setProgress((int) (1000.0* mMediaPlayer.getCurrentPosition() / mMediaPlayer.getDuration()));
             ((Activity)mContext).finish();
         }

       }
    }else{
      ((Activity)mContext).finish();
    }
  }

  public void start() {
    if (isInPlaybackState()) {
      while(videoList.size()>0)
      {
        if(!CommonUtils.isNetUrl(videoList.get(position).getVideoPath())){
          if(videoList.get(position).getNetworkVideoAddress()==null){//如果不是网络地址
            if(!new File(videoList.get(position).getVideoPath()).exists()){//如果该路径不存在
              oneToast.showMessage(this.getContext(),"该视频不存在");
              if(videoCollect!=null)
                videoCollect.deleteOneVideoUpateView(position,videoList.get(position).getVideoPath());
              videoList.remove(position);
              if(videoList.size()>0){
                position=position==0?(videoList.size()-1):(position-1);
              }else{
                return;
              }
              continue;
            }else{
                setBufferSize(0);

                if(mMediaController!=null){
                mMediaController.hideDownloadAndShareIcon();//设置下载和分享图标不可用
              }
              if(videoCollect!=null)
                videoCollect.deleteOnInfoAndOnBufferingUpdate();//删除缓冲
              break;
            }
          }else {
              setBufferSize(0);

              //是在线视频不过已下载
            if(mMediaController!=null){
              mMediaController.setDownloadNoAvailableShareIconEnable();// 设置下载图标不可用,分享图标可用
            }
            if(videoCollect!=null)
              videoCollect.deleteOnInfoAndOnBufferingUpdate();//删除缓冲;
            break;
          }

        }else{
            setBufferSize(100);

            if(mMediaController!=null){
            mMediaController.showDownloadAndShareIcon();//设置下载和分享图标可用
          }
          if(videoCollect!=null)
          videoCollect.addOnInfoAndOnBufferingUpdate();//添加缓冲
          if(videoCollect!=null){
            if(!videoCollect.ifDownloadLocalPlay(position)){//此时网络不可用并且此视频为在线视频
              return;
            }
          }
          break;
        }
      }
        if(videoList.size()>0){
          mMediaPlayer.seekTo((long) (1.0*videoList.get(position).getProgress()*videoList.get(position).getDuration()/1000));
          if(mMediaController!=null){

            mMediaController.setProgressRightSlide(videoList.get(position).getProgress());
          }
          android.util.Log.i("movie2", "start: "+videoList.get(position).getProgress()+"//"+videoList.get(position).getDuration());
        }
      mMediaPlayer.start();
      mCurrentState = STATE_PLAYING;
    }
    mTargetState = STATE_PLAYING;
  }

  //重新设置播放路径
  public void resetSetVideoUri(){
    setVideoURI(Uri.parse(videoList.get(position).getVideoPath()));
  }

  //隐藏下载和分享图标，删除缓冲(播放在线视频的时候判断是否下载，如果下载则本地播放）
  public void localPlay(Video video,int position){
    if(isHaveVideoList()){
      if(videoCollect!=null){
        videoCollect.deleteOnInfoAndOnBufferingUpdate();//删除缓冲
      }
      if(mMediaController!=null){
        mMediaController.setDownloadNoAvailableShareIconEnable();//设置下载图标不可用,分享图标可用
      }
      videoList.set(position, video);
    }
  }

  //隐藏下载和分享图标，删除缓冲(下载后本地播放）
  public void downLoadLocalPlay(Video video,int position){
    if(isHaveVideoList()){
      if(videoCollect!=null){
        videoCollect.deleteOnInfoAndOnBufferingUpdate();//删除缓冲
      }
      if(mMediaController!=null){
        mMediaController.setDownloadNoAvailableShareIconEnable();//设置下载图标不可用,分享图标可用
      }
      video.setProgress((int) (1000.0*mMediaPlayer.getCurrentPosition()/mMediaPlayer.getDuration()));
      videoList.set(position,video);
      if(this.position==position){//如果当前播放的歌曲为下载的歌曲下标
        //oneToast.showMessage(mContext,"视频已下载完开始本地播放");
        if(videoList.size()>0) {
          android.util.Log.i("movie2", "视频已下载完开始本地播放"+videoList.get(position).getVideoPath());
           start();
        }
      }
    }
  }

  public void continuePlay(){
    if (isInPlaybackState()) {
        mMediaPlayer.start();
        mCurrentState = STATE_PLAYING;
    }
    mTargetState = STATE_PLAYING;

  }
  public void pause() {
    if (isInPlaybackState()) {

      if (mMediaPlayer.isPlaying()) {
        videoList.get(position).setProgress((int) (1000.0*mMediaPlayer.getCurrentPosition()/mMediaPlayer.getDuration()));
        mMediaPlayer.pause();
        mCurrentState = STATE_PAUSED;
      }
    }
    mTargetState = STATE_PAUSED;
  }

  private boolean isHaveVideoList(){
    if(videoList.size()>0){
      return true;
    }else
    {
      return  false;
    }
  }
  private int VideoListSize(){
    return videoList.size();
  }
  //设置播放进度
  public void setProgress(){
    if(isHaveVideoList()) {
      int index = (position - 1) < 0 ? VideoListSize() - 1 : (position - 1);
      videoList.get(index).setProgress(0);
    }
  }
  public void next() {

     if(isHaveVideoList()){
       android.util.Log.i("movie2", "next: "+mMediaPlayer.getCurrentPosition()+"//"+mMediaPlayer.getDuration());
       if(videoList.get(position).getDuration()!=mMediaPlayer.getDuration()){
         videoList.get(position).setDuration(mMediaPlayer.getDuration());
       }
       if(mMediaPlayer.getCurrentPosition()>=mMediaPlayer.getDuration()){
         videoList.get(position).setProgress(0);
       }else{
         videoList.get(position).setProgress((int) (1000.0*mMediaPlayer.getCurrentPosition()/mMediaPlayer.getDuration()));
       }

    position=(position+1)>=VideoListSize()?0:(position+1);
       while(videoList.size()>0)
       {
         if(!CommonUtils.isNetUrl(videoList.get(position).getVideoPath())){//如果不是网络地址
           if(!new File(videoList.get(position).getVideoPath()).exists()){//如果该路径不存在
             oneToast.showMessage(this.getContext(),"该视频不存在");
             if(videoCollect!=null)
             videoCollect.deleteOneVideoUpateView(position,videoList.get(position).getVideoPath());
             videoList.remove(position);
             if(videoList.size()>0){
              position=position>=videoList.size()?(0):(position);
             }else{
               return;
             }
             continue;
           }else{
             break;
           }
         }else{
           break;
         }
       }

       if(videoList.size()>0) {
         setVideoURI(Uri.parse(videoList.get(position).getVideoPath()));
         start();
         if(mMediaController!=null){
           mMediaController.hide();
         }
       }

  }
  }

  public void prev() {
    if(isHaveVideoList()){
      if(videoList.get(position).getDuration()!=mMediaPlayer.getDuration()){
        videoList.get(position).setDuration(mMediaPlayer.getDuration());
      }
      if(mMediaPlayer.getCurrentPosition()>=mMediaPlayer.getDuration()){
        videoList.get(position).setProgress(0);
      }else{
        videoList.get(position).setProgress((int) (1000.0*mMediaPlayer.getCurrentPosition()/mMediaPlayer.getDuration()));
      }
      position=(position-1)<0?VideoListSize()-1:(position-1);
      while(videoList.size()>0)
      {
        if(!CommonUtils.isNetUrl(videoList.get(position).getVideoPath())){//如果不是网络地址
          if(!new File(videoList.get(position).getVideoPath()).exists()){//如果该路径不存在
            oneToast.showMessage(this.getContext(),"该视频不存在");
            if(videoCollect!=null)
            videoCollect.deleteOneVideoUpateView(position,videoList.get(position).getVideoPath());
            videoList.remove(position);
            if(videoList.size()>0){
              position=position>=videoList.size()?(0):(position);
            }else{
              return;
            }
            continue;
          }else{
            break;
          }
        }else{
          break;
        }
      }
      if(videoList.size()>0)
      {
        setVideoURI(Uri.parse(videoList.get(position).getVideoPath()));
        start();
        if(mMediaController!=null){
          mMediaController.hide();
        }
      }

    }
  }

  private VideoCollect videoCollect;
  private boolean isLove=false;

  public void setVideoCollect(VideoCollect videoCollect) {
    this.videoCollect = videoCollect;
  }

  @Override
  public void love() {

   // android.util.Log.i("movie", "----------------------------love ");
    String videoPath=null;
    if(videoList.get(position).getNetworkVideoAddress()==null){
      videoPath=videoList.get(position).getVideoPath();
    }else{
      videoPath=videoList.get(position).getNetworkVideoAddress();
    }

    if(isLove){
      //在数据库删除
      if(videoCollectOperation.cancelCollectVide(videoPath)==true){
        oneToast.showMessage(this.getContext(),"取消成功");
      }else{
        oneToast.showMessage(this.getContext(),"取消失败");
      }
      isLove=false;
    }else{
      if(videoList.get(position).getDuration()!=mMediaPlayer.getDuration()){
        videoList.get(position).setDuration(mMediaPlayer.getDuration());
      }

        Video video=videoList.get(position);
        video.setVideoPath(videoPath);
        video.setDate(System.currentTimeMillis());
        video.setProgress((int) (1000.0*mMediaPlayer.getCurrentPosition()/mMediaPlayer.getDuration()));
      //在数据库添加
      if(videoCollectOperation.saveVideoCollect(video)==true)
      {oneToast.showMessage(this.getContext(),"收藏成功");

      }else{
        oneToast.showMessage(this.getContext(),"收藏失败");
      }
      isLove=true;
    }
     if(mMediaController!=null){
      mMediaController.setLove(isLove);
     }
    if(videoCollect!=null){
      videoCollect.updateView();
    }
  }

  public void download() {
   android.util.Log.i("movie", "----------------------------download ");
   if(isHaveVideoList()){
     if(videoCollect!=null){
       videoCollect.downloadVideo(position);
     }
   }
  }

  @Override
  public void share() {
    android.util.Log.i("movie", "----------------------------share");
    if(CommonUtils.net_avaiable(mContext))
    {
      oneToast.showMessage(getContext(),"分享");
      if(videoCollect!=null){
        videoCollect.showShare();
      }
    }

    else{
      oneToast.showMessage(getContext(),"当前网络不可用");
    }
  }

  //获取当前视频的下标
  public int getCurrentVideoPosition(){
    if(isHaveVideoList()){
      return position;
    }else{
      return 0;//播放一个视频
    }
  }
  //判断当前的播放的视频是在线视频且没有本地下载
  public boolean currentVideoIsOnLineVideoAndNetUnavaiable(){
    Video video=videoList.get(position);
    if(video.getNetworkVideoAddress()==null&&CommonUtils.isNetUrl(video.getVideoPath())){
      return true;
    }else{
      return false;
    }
  }
  @Override
  public void setPlaybackSpeed(float speed) {
    mMediaPlayer.setPlaybackSpeed(speed);
  }


  public void hideMediaControlAllTip(){
    if(mMediaController!=null){
      mMediaController.hideAllTip();
    }
  }
  //隐藏控制按钮
  public void hideMediaControl(){
    if(mMediaController!=null){
      mMediaController.hide();
    }
  }
  public void suspend() {
    if (isInPlaybackState()) {
      release(false);
      mCurrentState = STATE_SUSPEND_UNSUPPORTED;
      Log.d("Unable to suspend video. Release MediaPlayer.");
    }
  }

  public void resume() {
    if (mSurfaceHolder == null && mCurrentState == STATE_SUSPEND) {
      mTargetState = STATE_RESUME;
    } else if (mCurrentState == STATE_SUSPEND_UNSUPPORTED) {
      openVideo();
    }
  }

  public long getDuration() {
    if (isInPlaybackState()) {
      if (mDuration > 0)
        return mDuration;
      mDuration = mMediaPlayer.getDuration();
      return mDuration;
    }
    mDuration = -1;
    return mDuration;
  }

  public long getCurrentPosition() {
    if (isInPlaybackState())
      return mMediaPlayer.getCurrentPosition();
    return 0;
  }

  public void seekTo(long msec) {
    if (isInPlaybackState()) {
      mMediaPlayer.seekTo(msec);
      mSeekWhenPrepared = 0;
    } else {
      mSeekWhenPrepared = msec;
    }
  }

  public boolean isPlaying() {
    return isInPlaybackState() && mMediaPlayer.isPlaying();
  }

  @Override
  public boolean isLove() {
    if(videoList.get(position).getNetworkVideoAddress()==null){
      isLove=videoCollectOperation.isExistsByPath(videoList.get(position).getVideoPath());//从数据中获取
    }else{
      isLove=videoCollectOperation.isExistsByPath(videoList.get(position).getNetworkVideoAddress());//从数据中获取
    }
    return isLove;
  }

  public int getBufferPercentage() {
    if (mMediaPlayer != null)
      return mCurrentBufferPercentage;
    return 0;
  }

  @Override
  public boolean IsOnLineVideoAndNetUnavaiable() {

    return currentVideoIsOnLineVideoAndNetUnavaiable();
  }

  public void setVolume(float leftVolume, float rightVolume) {
    if (mMediaPlayer != null)
      mMediaPlayer.setVolume(leftVolume, rightVolume);
  }

  public int getVideoWidth() {
    return mVideoWidth;
  }

  @Override
  public void setVideoSize(int width, int height) {

    if(mMediaPlayer!=null){
     ViewGroup.LayoutParams layoutParams=getLayoutParams();
     layoutParams.width=width;
     layoutParams.height=height;
     setLayoutParams(layoutParams);
    }
  }

  public int getVideoHeight() {
    return mVideoHeight;
  }

  public float getVideoAspectRatio() {
    return mVideoAspectRatio;
  }

  /**
   * Must set before {@link #setVideoURI}
   * @param chroma
   */
  public void setVideoChroma(int chroma) {
    getHolder().setFormat(chroma == MediaPlayer.VIDEOCHROMA_RGB565 ? PixelFormat.RGB_565 : PixelFormat.RGBA_8888); // PixelFormat.RGB_565
    mVideoChroma = chroma;
  }

  public void setHardwareDecoder(boolean hardware) {
    mHardwareDecoder= hardware;
  }

  public void setVideoQuality(int quality) {
    if (mMediaPlayer != null)
      mMediaPlayer.setVideoQuality(quality);
  }

  public void setBufferSize(int bufSize) {
    mBufSize = bufSize;
  }

  public boolean isBuffering() {
    if (mMediaPlayer != null)
      return mMediaPlayer.isBuffering();
    return false;
  }

  public String getMetaEncoding() {
    if (mMediaPlayer != null)
      return mMediaPlayer.getMetaEncoding();
    return null;
  }

  public void setMetaEncoding(String encoding) {
    if (mMediaPlayer != null)
      mMediaPlayer.setMetaEncoding(encoding);
  }

  public SparseArray<MediaFormat> getAudioTrackMap(String encoding) {
    if (mMediaPlayer != null)
      return mMediaPlayer.findTrackFromTrackInfo(TrackInfo.MEDIA_TRACK_TYPE_AUDIO, mMediaPlayer.getTrackInfo(encoding));
    return null;
  }

  public int getAudioTrack() {
    if (mMediaPlayer != null)
      return mMediaPlayer.getAudioTrack();
    return -1;
  }

  public void setAudioTrack(int audioIndex) {
    if (mMediaPlayer != null)
      mMediaPlayer.selectTrack(audioIndex);
  }

  public void setTimedTextShown(boolean shown) {
    if (mMediaPlayer != null)
      mMediaPlayer.setTimedTextShown(shown);
  }

  public void setTimedTextEncoding(String encoding) {
    if (mMediaPlayer != null)
      mMediaPlayer.setTimedTextEncoding(encoding);
  }

  public int getTimedTextLocation() {
    if (mMediaPlayer != null)
      return mMediaPlayer.getTimedTextLocation();
    return -1;
  }

  public void addTimedTextSource(String subPath) {
    if (mMediaPlayer != null)
      mMediaPlayer.addTimedTextSource(subPath);
  }

  public String getTimedTextPath() {
    if (mMediaPlayer != null)
      return mMediaPlayer.getTimedTextPath();
    return null;
  }

  public void setSubTrack(int trackId) {
    if (mMediaPlayer != null)
      mMediaPlayer.selectTrack(trackId);
  }

  public int getTimedTextTrack() {
    if (mMediaPlayer != null)
      return mMediaPlayer.getTimedTextTrack();
    return -1;
  }

  public SparseArray<MediaFormat> getSubTrackMap(String encoding) {
    if (mMediaPlayer != null)
      return mMediaPlayer.findTrackFromTrackInfo(TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT, mMediaPlayer.getTrackInfo(encoding));
    return null;
  }

  protected boolean isInPlaybackState() {
    return (mMediaPlayer != null && mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
  }


  public interface  VideoCollect{
    void updateView();//添加或收藏更新视图
    void deleteOneVideoUpateView(int position,String path);//删除一个视频更新视图
    void addOnInfoAndOnBufferingUpdate();//如果是网络视频则添加缓冲
    void deleteOnInfoAndOnBufferingUpdate();//如果是本地视频则删除缓冲
    void downloadVideo(int position);//下载视频
    boolean ifDownloadLocalPlay(int position);//如果下载了就本地播放
    boolean isTouchUse();//判断滑动是否可用
    void showShare();
  }
}