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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.transition.Slide;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.lang.reflect.Method;

import io.vov.vitamio.R;
import io.vov.vitamio.adapter.PlayModeAdapter;
import io.vov.vitamio.utils.CommonUtils;
import io.vov.vitamio.utils.Log;
import io.vov.vitamio.utils.StringUtils;

/**
 * A view containing controls for a MediaPlayer. Typically contains the buttons
 * like "Play/Pause" and a progress slider. It takes care of synchronizing the
 * controls with the state of the MediaPlayer.
 * <p/>
 * The way to use this class is to a) instantiate it programatically or b)
 * create it in your xml layout.
 * <p/>
 * a) The MediaController will create a default set of controls and put them in
 * a window floating above your application. Specifically, the controls will
 * float above the view specified with setAnchorView(). By default, the window
 * will disappear if left idle for three seconds and reappear when the user
 * touches the anchor view. To customize the MediaController's style, layout and
 * controls you should extend MediaController and override the {#link
 * {@link #makeControllerView()} method.
 * <p/>
 * b) The MediaController is a FrameLayout, you can put it in your layout xml
 * and get it through {@link #findViewById(int)}.
 * <p/>
 * NOTES: In each way, if you want customize the MediaController, the SeekBar's
 * id must be mediacontroller_progress, the Play/Pause's must be
 * mediacontroller_pause, current time's must be mediacontroller_time_current,
 * total time's must be mediacontroller_time_total, file name's must be
 * mediacontroller_file_name. And your resources must have a pause_button
 * drawable and a play_button drawable.
 * <p/>
 * Functions like show() and hide() have no effect when MediaController is
 * created in an xml layout.
 */
public class MediaController extends FrameLayout {
  private static final int sDefaultTimeout = 3000;
  private static final int FADE_OUT = 1;
  private static final int SHOW_PROGRESS = 2;
  private static final  int HIDE_TIPICON=3;
  private static final int HIDE_TIP=4;
  private MediaPlayerControl mPlayer;
  private Context mContext;
  private PopupWindow mWindow;
  private int mAnimStyle;
  private View mAnchor;
  private View mRoot;
  private SeekBar mProgress;
  private TextView mEndTime, mCurrentTime;
  private TextView mFileName;
  private OutlineTextView mInfoView;
  private String mTitle;
  private long mDuration;
  private boolean mShowing;
  private boolean mDragging;
  private boolean mInstantSeeking = false;
  private boolean mFromXml = false;
  private ImageButton mPauseButton;
  private AudioManager mAM;
  private OnShownListener mShownListener;
  private OnHiddenListener mHiddenListener;
  private final  String[] data={"快进","2倍快进","正常","后退","2倍后退"};
  private static  boolean isFullScreen=false;//判断是否全屏
  private int mScreenWidth;//屏幕的宽度
  private int mScreentHeight;//屏幕的高度
  private int DEFAULT_SCREEN=0,FULL_SCREEN=1;
  private final String TAG="movie";

  @SuppressLint("HandlerLeak")
  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      long pos;
      switch (msg.what) {
        case FADE_OUT:
          hide();
          break;
        case SHOW_PROGRESS:
          pos = setProgress();
          if (!mDragging && mShowing) {
            msg = obtainMessage(SHOW_PROGRESS);
            sendMessageDelayed(msg, 1000 - (pos % 1000));
            updatePausePlay();
          }
          break;
        case HIDE_TIPICON:
          hideAllTip();
          break;
        case HIDE_TIP:
          hide();
          isOnlyShowTipCon=false;
          break;

      }
    }
  };
  private View.OnClickListener mPauseListener = new View.OnClickListener() {
    public void onClick(View v) {
      doPauseResume();
      show(sDefaultTimeout);
    }
  };



  //播放上一部视频
  private View.OnClickListener mPrevListener=new View.OnClickListener(){

    @Override
    public void onClick(View view) {
    doPrevResume();
      show(sDefaultTimeout);

    }
  };

  //播放下一部视频
  private View.OnClickListener mNextListener=new View.OnClickListener(){

    @Override
    public void onClick(View view) {
      doNextResume();
      show(sDefaultTimeout);
    }
  };
  private ImageView tipBrightnessIcon;
  private LinearLayout mediacontroller_topLinerLayout;
  private RelativeLayout mediacontroller_relativeLayout;


  //设置是否喜欢
    public void setLove(boolean islove) {
        if(islove){
  mLoveButton.setBackgroundResource(R.drawable.mediacontrol_love_selected);
        }else{
            mLoveButton.setBackgroundResource(R.drawable.mediacontrol_love);
        }
    }

  //将当前播放的视频添加为喜欢
  private View.OnClickListener mLoveListener=new View.OnClickListener(){

    @Override
    public void onClick(View view) {
      doLoveResume();
      show(sDefaultTimeout);

    }
  };


  //点击播放模式
  private View.OnClickListener  mPlayModeListener=new View.OnClickListener(){

    @Override
    public void onClick(View view) {
      //android.util.Log.i(TAG, "onClick: 播放模式"+(mPlayModeListView != null)+"//"+(mPlayModeListView.getVisibility()==GONE));

      if (mPlayModeListView != null) {
    if(mPlayModeListView.getVisibility()==GONE)
    {
      mPlayModeListView.setVisibility(View.VISIBLE);
    }
    else{
     mPlayModeListView.setVisibility(GONE);
    }
        show(sDefaultTimeout);
      }
    }
  };

  //下载的视频
  private View.OnClickListener mDownloadListener=new View.OnClickListener(){

    @Override
    public void onClick(View view) {
      doDownloadResume();
      show(sDefaultTimeout);
    }
  };

  //分享的视频
  private View.OnClickListener mShareListener=new View.OnClickListener(){

    @Override
    public void onClick(View view) {
      doShareResume();
      show(sDefaultTimeout);
    }
  };

  //点击声音图标
  private View.OnClickListener mVoiceButtonListener=new View.OnClickListener(){

    @Override
    public void onClick(View view) {
      android.util.Log.i("movie", "onClick:mMedia_pb_voice ");
      mMedia_pb_voice.setProgress(0);
      mAM.setStreamVolume(AudioManager.STREAM_MUSIC,0,0);
      show(sDefaultTimeout);
    }
  };
  //滑动声音的进度条时，改变声音大小
  private OnSeekBarChangeListener mMediaVoiceListener=new OnSeekBarChangeListener() {
    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
      mAM.setStreamVolume(AudioManager.STREAM_MUSIC,mMedia_pb_voice.getProgress(),0);
      showNoAnimal();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
      show(sDefaultTimeout);
    }
  };

  //返回
  private View.OnClickListener mMediaExitListener=new View.OnClickListener(){

    @Override
    public void onClick(View view) {
      mContext.unregisterReceiver(mBatteryReceiver);
      ((Activity)mContext).finish();
    }
  };


  //点击全屏按钮
  private View.OnClickListener mMediacontroller_video_siwch_screenListener=new View.OnClickListener(){

    @Override
    public void onClick(View view) {
      setVoideType();
      show(sDefaultTimeout);
    }
  };

  //点击播放模式
  private AdapterView.OnItemClickListener mPlayModeListViewLinstener=new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
      final float[] speed={1.5f,2.0f,1.0f,0.5f,0.8f};//没有实现后退
      mPlayer.setPlaybackSpeed(speed[i]);
      hidePlayModeListView();
      show(sDefaultTimeout);
    }
  };
  private void setVideoType(int defaultScreen){
    if(defaultScreen==DEFAULT_SCREEN){
      //视频真实的宽和高
      int mVideoWidth=mPlayer.getVideoWidth();
      int mVideoHeigh=mPlayer.getVideoHeight();
      int width=mScreenWidth,height=mScreentHeight;
      if(mVideoWidth*mScreentHeight<mScreenWidth*mVideoHeigh){
      width=mVideoWidth*mScreentHeight/mVideoHeigh;
      }else if(mVideoWidth*mScreentHeight>mScreenWidth*mVideoHeigh){
        height=mScreenWidth*mVideoHeigh/mVideoWidth;
      }
     // android.util.Log.i("movie", "----------------- "+width+"//"+height);
    mPlayer.setVideoSize(width,height);
    }else if(defaultScreen==FULL_SCREEN){
     // android.util.Log.i("movie", "////////////// "+mScreenWidth+"//"+mScreentHeight);
     mPlayer.setVideoSize(mScreenWidth,mScreentHeight);
    }
  }

public void setVoideType(){
  if(isFullScreen==true){
    setVideoType(DEFAULT_SCREEN);
    mMediacontroller_video_siwch_screen.setBackgroundResource(R.drawable.btn_video_siwch_screen_full_selector);

    isFullScreen=false;
  }else{
    setVideoType(FULL_SCREEN);
    mMediacontroller_video_siwch_screen.setBackgroundResource(R.drawable.btn_video_siwch_screen_default_selector);
    isFullScreen=true;
  }
}

//改变音量
  public void changeVolume(int keyCode){
    int mCurrentVolume=mMedia_pb_voice.getProgress();
    if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
      mCurrentVolume--;
      if(mCurrentVolume==1){
        mCurrentVolume=0;
      }
    }else if(keyCode==KeyEvent.KEYCODE_VOLUME_UP){
      mCurrentVolume++;
      if(mCurrentVolume==mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC)-1){
        mCurrentVolume=mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
      }
    }
    show(sDefaultTimeout);
    mMedia_pb_voice.setProgress(mCurrentVolume);
    setTipIconVocieMax();
    setTipIconVocieProgress(mCurrentVolume);
    showOneTipByIndex(0);
    mHandler.removeMessages(HIDE_TIPICON);
    mHandler.sendEmptyMessageDelayed(HIDE_TIPICON,2000);

  }
  public void changeVolumeByVoiceValue(int Voice){
    mAM.setStreamVolume(AudioManager.STREAM_MUSIC,Voice,0);
    mMedia_pb_voice.setProgress(Voice);
  }


//隐藏播放模式
  public void hidePlayModeListView(){
    if(mPlayModeListView.getVisibility()==VISIBLE)
      mPlayModeListView.setVisibility(GONE);
  }

  //改变播放按钮
  public void changePauseButton(){
           updatePausePlay();
  }

  private boolean isOnlyShowTipCon=false;//是否只显示了提示信息
  //手势监听器的处理函数
  private GestureDetector.SimpleOnGestureListener simpleOnGestureListener=new GestureDetector.SimpleOnGestureListener(){
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      return super.onSingleTapUp(e);
    }

    @Override
    public void onLongPress(MotionEvent e) {

      setVoideType();
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

      if(getVisibility()==VISIBLE)
      {

        //表示只有提示信息显示，那么双击会播放视频
        if(isOnlyShowTipCon&&getVisibility()==VISIBLE)
        {
          doPauseResume();

        }
        else{
          isOnlyShowTipCon=false;
          doPauseResume();
          show(sDefaultTimeout);
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

       // android.util.Log.i(TAG, "onSingleTapConfirmed: /////////"+isOnlyShowTipCon);
      //表示只有提示信息显示，那么点击隐藏
      if(isOnlyShowTipCon&&getVisibility()==VISIBLE)
      {
//        hide();
//        hidePlayModeListView();
        hideAllTip();
        show(sDefaultTimeout);
        isOnlyShowTipCon=false;
      }
      else{
         isOnlyShowTipCon=false;
        if(getVisibility()==View.VISIBLE)
        {
          hide();
          hidePlayModeListView();
        }else if(getVisibility()==View.GONE){
          show(sDefaultTimeout);
        }
      }

      return super.onSingleTapConfirmed(e);
    }
  };
//显示之后不会自动隐藏
public void showNoAnimal(){
  if (!mShowing && mAnchor != null && mAnchor.getWindowToken() != null) {
    if (mPauseButton != null)
      mPauseButton.requestFocus();

    if (mFromXml) {
      setVisibility(View.VISIBLE);
    } else {
      int[] location = new int[2];

      mAnchor.getLocationOnScreen(location);
      Rect anchorRect = new Rect(location[0], location[1], location[0] + mAnchor.getWidth(), location[1] + mAnchor.getHeight());

      mWindow.setAnimationStyle(mAnimStyle);
      setWindowLayoutType();
      mWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY, anchorRect.left, anchorRect.bottom);
    }
    mShowing = true;
    if (mShownListener != null)
      mShownListener.onShown();
  }
  showControl();
  updatePausePlay();
  mHandler.sendEmptyMessage(SHOW_PROGRESS);
  mHandler.removeMessages(FADE_OUT);
  mHandler.removeMessages(HIDE_TIP);
}


  /**
   * 显示除提示信息的控件
   */
  private void showControl(){

    if(mediacontroller_topLinerLayout!=null&&mediacontroller_topLinerLayout.getVisibility()==GONE){
      mediacontroller_topLinerLayout.setVisibility(VISIBLE);
    }

    if(mediacontroller_relativeLayout!=null&&mediacontroller_relativeLayout.getVisibility()==GONE){
      mediacontroller_relativeLayout.setVisibility(VISIBLE);
    }


  }


  /**
   * 隐藏内部所有的控件
   */
 private void  hideAllControl(){
   showNoAnimal();
   if(mediacontroller_topLinerLayout!=null){
     mediacontroller_topLinerLayout.setVisibility(GONE);
   }
   if(mediacontroller_relativeLayout!=null){
     mediacontroller_relativeLayout.setVisibility(GONE);
   }

   if(mPlayModeListView!=null){
     mPlayModeListView.setVisibility(GONE);
   }
   hideAllTip();
 }
  /**
   * 只显示提示图标不会自动隐藏 0代表的是音量，1代表的是亮度，2代表的播放进度,3代表暂停
   * @param index
   */
  public void onlyShowTipIcon(int index){
    hideAllControl();
    mHandler.removeMessages(HIDE_TIP);
    switch (index){
      case 0:
        showOneTipByIndex(0);
        isOnlyShowTipCon=true;
        break;
      case 1:
        showOneTipByIndex(1);
        isOnlyShowTipCon=true;
        break;
      case 2:
        showOneTipByIndex(2);
        isOnlyShowTipCon=true;
        break;
      case 3:
        showOneTipByIndex(3);
        isOnlyShowTipCon=true;
        break;
        default:
          break;
    }
  }

  /**
   * 自动隐藏提示图标
   */
  public void autoHideShowTipIcon(){
    mHandler.removeMessages(HIDE_TIP);
    mHandler.sendEmptyMessageDelayed(HIDE_TIP,2000);
  }

  private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
    public void onStartTrackingTouch(SeekBar bar) {
      mDragging = true;
      show(3600000);
      mHandler.removeMessages(SHOW_PROGRESS);
      if (mInstantSeeking)
        mAM.setStreamMute(AudioManager.STREAM_MUSIC, true);
      if (mInfoView != null) {
        mInfoView.setText("");
        mInfoView.setVisibility(View.VISIBLE);
      }
    }

    public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
      if (!fromuser)
        return;
      long newposition = (mDuration * progress) / 1000;
      String time = StringUtils.generateTime(newposition);
      if (mInstantSeeking)
        mPlayer.seekTo(newposition);
      if (mInfoView != null)
        mInfoView.setText(time);
      if (mCurrentTime != null)
        mCurrentTime.setText(time);
    }

    public void onStopTrackingTouch(SeekBar bar) {
      if (!mInstantSeeking)
        mPlayer.seekTo((mDuration * bar.getProgress()) / 1000);
      if (mInfoView != null) {
        mInfoView.setText("");
        mInfoView.setVisibility(View.GONE);
      }
      show(sDefaultTimeout);
      mHandler.removeMessages(SHOW_PROGRESS);
      mAM.setStreamMute(AudioManager.STREAM_MUSIC, false);
      mDragging = false;
      mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 1000);
    }
  };

  private ImageButton mPrevButton;
  private ImageButton mNextButton;
  private ImageButton mLoveButton;
  private Button mDoubleSpeedButton;
  private ImageButton mDownloadButton;
  private ImageButton mShareButton;
  private ListView mPlayModeListView;
  private static boolean playModeButtonClick=false;//判断点击播放模式，点击二次就会隐藏播放模式
  private ImageView mMedia_im_battery;
  private BatteryReceiver mBatteryReceiver;
  private TextView mMedia_tv_systemTime;
  private SeekBar mMedia_pb_voice;
  private Button mMedia_bt_voice;
  private ImageButton mMediacontroller_exit;
  private ImageButton mMediacontroller_video_siwch_screen;
  private GestureDetector mGestureDetector;

  private ImageView tipVoiceIcon;
  private SeekBar tipSeekBar;
  private TextView tipPlayProgress;
  private ImageButton tipPause;
  private LinearLayout tipLinearLayout;
  public MediaController(Context context, AttributeSet attrs) {
    super(context, attrs);
    mRoot = this;
    mFromXml = true;
    initController(context);
  }

  public MediaController(Context context) {
    super(context);
    if (!mFromXml && initController(context))
      initFloatingWindow();
  }

  private boolean initController(Context context) {
    mContext = context;
    mAM = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    mScreenWidth=CommonUtils.getScreenWidth(this.getContext());
    mScreentHeight=CommonUtils.getScreenHeight(this.getContext());
    //注册电量广播
    mBatteryReceiver= new BatteryReceiver();
    IntentFilter intentFilter=new IntentFilter();
    intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
    context.registerReceiver(mBatteryReceiver,intentFilter);
    mGestureDetector=new GestureDetector(this.getContext(),simpleOnGestureListener);
    mCurrentBrigtness=getSystemBrightness()/255.0f;
    changeAppBrightness(mCurrentBrigtness);//设置当前APP亮度为系统亮度

    return true;
  }

  @SuppressLint("MissingSuperCall")
  @Override
  public void onFinishInflate() {
    if (mRoot != null)
      initControllerView(mRoot);
  }

  private void initFloatingWindow() {
    mWindow = new PopupWindow(mContext);
    mWindow.setFocusable(false);
    mWindow.setBackgroundDrawable(null);
    mWindow.setOutsideTouchable(true);
    mAnimStyle = android.R.style.Animation;
  }
  
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void setWindowLayoutType() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			try {
				mAnchor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
				Method setWindowLayoutType = PopupWindow.class.getMethod("setWindowLayoutType", new Class[] { int.class });
				setWindowLayoutType.invoke(mWindow, WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG);
			} catch (Exception e) {
				Log.e("setWindowLayoutType", e);
			}
		}
	}

  /**
   * Set the view that acts as the anchor for the control view. This can for
   * example be a VideoView, or your Activity's main view.
   *
   * @param view The view to which to anchor the controller when it is visible.
   */
  public void setAnchorView(View view) {

    mAnchor = view;
    if (!mFromXml) {
      removeAllViews();
      mRoot = makeControllerView();
      mWindow.setContentView(mRoot);
      mWindow.setWidth(LayoutParams.MATCH_PARENT);
      mWindow.setHeight(LayoutParams.MATCH_PARENT);//这地方真坑
    }
    initControllerView(mRoot);
  
  }

  /**
   * Create the view that holds the widgets that control playback. Derived
   * classes can override this to create their own.
   *
   * @return The controller view.
   */
  protected View makeControllerView() {
    return ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(getResources().getIdentifier("mediacontroller", "layout", mContext.getPackageName()), this);
  }

  private void initControllerView(View v) {
    mPauseButton = (ImageButton) v.findViewById(getResources().getIdentifier("mediacontroller_play_pause", "id", mContext.getPackageName()));
    if (mPauseButton != null) {
      mPauseButton.requestFocus();
      mPauseButton.setOnClickListener(mPauseListener);
    }

    mProgress = (SeekBar) v.findViewById(getResources().getIdentifier("mediacontroller_seekbar", "id", mContext.getPackageName()));
    if (mProgress != null) {
      if (mProgress instanceof SeekBar) {
        SeekBar seeker = (SeekBar) mProgress;
        seeker.setOnSeekBarChangeListener(mSeekListener);
      }
      mProgress.setMax(1000);
    }

    mEndTime = (TextView) v.findViewById(getResources().getIdentifier("mediacontroller_time_total", "id", mContext.getPackageName()));
    mCurrentTime = (TextView) v.findViewById(getResources().getIdentifier("mediacontroller_time_current", "id", mContext.getPackageName()));
    mFileName = (TextView) v.findViewById(getResources().getIdentifier("mediacontroller_file_name", "id", mContext.getPackageName()));
    if (mFileName != null)
      mFileName.setText(mTitle);


    mPrevButton = (ImageButton)v.findViewById(getResources().getIdentifier("mediacontroller_prev","id",mContext.getPackageName()));
    if(mPrevButton!=null){
      mPrevButton.requestFocus();
      mPrevButton.setOnClickListener(mPrevListener);
    }

    mNextButton = (ImageButton)v.findViewById(getResources().getIdentifier("mediacontroller_next","id",mContext.getPackageName()));
    if(mNextButton!=null){
      mNextButton.requestFocus();
      mNextButton.setOnClickListener(mNextListener);
    }

    mLoveButton = (ImageButton)v.findViewById(getResources().getIdentifier("mediacontroller_love","id",mContext.getPackageName()));
    if(mLoveButton!=null){
      //
        setLove(mPlayer.isLove());

      mLoveButton.requestFocus();
      mLoveButton.setOnClickListener(mLoveListener);
    }

    mDownloadButton = (ImageButton)v.findViewById(getResources().getIdentifier("mediacontroller_download","id",mContext.getPackageName()));
    if(mDownloadButton!=null){
      mDownloadButton.requestFocus();
      mDownloadButton.setOnClickListener(mDownloadListener);
    }

    mShareButton = (ImageButton)v.findViewById(getResources().getIdentifier("mediacontroller_share","id",mContext.getPackageName()));

    if(mShareButton!=null){
      mShareButton.requestFocus();
      mShareButton.setOnClickListener(mShareListener);
    }

    mPlayModeListView = (ListView)v.findViewById(getResources().getIdentifier("PlayModeListView","id",mContext.getPackageName()));
   if(mPlayModeListView!=null){
    final  PlayModeAdapter playModeAdapter=new PlayModeAdapter(data);
     mPlayModeListView.setAdapter(playModeAdapter);
     mPlayModeListView.setOnItemClickListener(mPlayModeListViewLinstener);
   }

    mDoubleSpeedButton = (Button)v.findViewById(getResources().getIdentifier("DoubleSpeed","id",mContext.getPackageName()));
    if(mDoubleSpeedButton!=null){
      mDoubleSpeedButton.requestFocus();
      mDoubleSpeedButton.setOnClickListener(mPlayModeListener);
    }
    mMedia_im_battery = (ImageView)v.findViewById(getResources().getIdentifier("media_im_battery","id",mContext.getPackageName()));
    if(mMedia_im_battery!=null){
     final BatteryManager batteryManager=(BatteryManager)mContext.getSystemService(Context.BATTERY_SERVICE);
     setBattery(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
    }

    mMedia_tv_systemTime = (TextView)v.findViewById(getResources().getIdentifier("media_tv_systemTime","id",mContext.getPackageName()));

    mMedia_pb_voice = (SeekBar)v.findViewById(getResources().getIdentifier("media_pb_voice","id",mContext.getPackageName()));

    //得到音量

    if(mMedia_pb_voice!=null){
      mMedia_pb_voice.setMax(mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
      mMedia_pb_voice.setProgress(mAM.getStreamVolume(AudioManager.STREAM_MUSIC));
     mMedia_pb_voice.setOnSeekBarChangeListener(mMediaVoiceListener);
    }

    //声音图标
    mMedia_bt_voice = (Button)v.findViewById(getResources().getIdentifier("media_bt_voice","id",mContext.getPackageName()));

    if(mMedia_bt_voice!=null){
      mMedia_bt_voice.setOnClickListener(mVoiceButtonListener);
    }

    //返回按钮
    mMediacontroller_exit = (ImageButton)v.findViewById(getResources().getIdentifier("mediacontroller_exit","id",mContext.getPackageName()));
    if(mMediacontroller_exit!=null){
    mMediacontroller_exit.setOnClickListener(mMediaExitListener);
    }
    //全屏获取默认
    mMediacontroller_video_siwch_screen = (ImageButton)v.findViewById(getResources().getIdentifier("mediacontroller_video_siwch_screen","id",mContext.getPackageName()));
    if(mMediacontroller_video_siwch_screen!=null){
     mMediacontroller_video_siwch_screen.setOnClickListener(mMediacontroller_video_siwch_screenListener);
    }

    tipVoiceIcon = (ImageView)v.findViewById(getResources().getIdentifier("TipVoiceIcon","id",mContext.getPackageName()));
    tipBrightnessIcon = (ImageView)v.findViewById(getResources().getIdentifier("TipBrightnessIcon","id",mContext.getPackageName()));
    tipSeekBar = (SeekBar)v.findViewById(getResources().getIdentifier("TipSeekBar","id",mContext.getPackageName()));
    tipPlayProgress = (TextView)v.findViewById(getResources().getIdentifier("TipPlayProgress","id",mContext.getPackageName()));
    tipPause = (ImageButton)v.findViewById(getResources().getIdentifier("TipPause","id",mContext.getPackageName()));
    if(tipPause!=null){
      tipPause.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          doPauseResume();
        }
      });
    }
    tipLinearLayout = (LinearLayout)v.findViewById(getResources().getIdentifier("TipLinearLayout","id",mContext.getPackageName()));

    mediacontroller_topLinerLayout = (LinearLayout)v.findViewById(getResources().getIdentifier("mediacontroller_topLinerLayout","id",mContext.getPackageName()));

    mediacontroller_relativeLayout = (RelativeLayout) v.findViewById(getResources().getIdentifier("mediacontroller_relativeLayout","id",mContext.getPackageName()));


  }

 

  /**
   * 设置播放模式列表的位置
   */
  private void setmPlayModeListViewPosition(){
    if(mDoubleSpeedButton!=null&&mPlayModeListView!=null){
      int[] location = new int[2];
     mDoubleSpeedButton.getLocationOnScreen(location);
     int leftMargin=location[0];
      android.util.Log.i(TAG, "setmPlayModeListViewPosition: "+leftMargin+"//"+location[1]);
      RelativeLayout.LayoutParams mPlayModeListView_layoutParams= (RelativeLayout.LayoutParams) mPlayModeListView.getLayoutParams();
    mPlayModeListView_layoutParams.leftMargin=leftMargin;
    mPlayModeListView.setLayoutParams(mPlayModeListView_layoutParams);

    }

  }


  //隐藏所有提示信息，如音量，亮度
  public   void hideAllTip(){

    if(tipVoiceIcon!=null&&tipVoiceIcon.getVisibility()==View.VISIBLE){
      tipVoiceIcon.setVisibility(GONE);
    }
    if(tipBrightnessIcon!=null&&tipBrightnessIcon.getVisibility()==View.VISIBLE){
      tipBrightnessIcon.setVisibility(GONE);
    }

    if(tipSeekBar!=null&&tipSeekBar.getVisibility()==View.VISIBLE){
      tipSeekBar.setVisibility(GONE);
    }
    if(tipPlayProgress!=null&&tipPlayProgress.getVisibility()==View.VISIBLE){
      tipPlayProgress.setVisibility(GONE);
    }
    if(tipPause!=null&&tipPause.getVisibility()==View.VISIBLE){
      tipPause.setVisibility(GONE);
    }
    if(tipLinearLayout!=null&&tipLinearLayout.getVisibility()==View.VISIBLE){
     tipLinearLayout.setVisibility(GONE);
    }
  }

    /**
     * 显示进度条
     */
  public void show_progress(){
      mHandler.sendEmptyMessage(SHOW_PROGRESS);
  }

  //电量广播
  class BatteryReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
      int level=intent.getIntExtra("level",0);
      setBattery(level);
    }
  }

  private void setBattery(int level) {
    if(mMedia_im_battery==null)
    {
      return;
    }
    if(level<=0){
      mMedia_im_battery.setImageResource(R.drawable.ic_battery_0);
    }else if(level<=10){
      mMedia_im_battery.setImageResource(R.drawable.ic_battery_10);
    }else if(level<=20){
      mMedia_im_battery.setImageResource(R.drawable.ic_battery_20);
    }else if(level<=40){
      mMedia_im_battery.setImageResource(R.drawable.ic_battery_40);
    }else if(level<=60){
      mMedia_im_battery.setImageResource(R.drawable.ic_battery_60);
    }else if(level<=80){
      mMedia_im_battery.setImageResource(R.drawable.ic_battery_80);
    }else if(level<=100){
      mMedia_im_battery.setImageResource(R.drawable.ic_battery_100);
    }
  }

  public void setMediaPlayer(MediaPlayerControl player) {
    mPlayer = player;
    updatePausePlay();
  }

  /**
   * Control the action when the seekbar dragged by user
   *
   * @param seekWhenDragging True the media will seek periodically
   */
  public void setInstantSeeking(boolean seekWhenDragging) {
    mInstantSeeking = seekWhenDragging;
  }

  public void show() {
    show(sDefaultTimeout);
  }

  /**
   * Set the content of the file_name TextView
   *
   * @param name
   */
  public void setFileName(String name) {
    mTitle = name;
    if (mFileName != null)
      mFileName.setText(mTitle);
  }

  /**
   * Set the View to hold some information when interact with the
   * MediaController
   *
   * @param v
   */
  public void setInfoView(OutlineTextView v) {
    mInfoView = v;
  }

  /**
   * <p>
   * Change the animation style resource for this controller.
   * </p>
   * <p/>
   * <p>
   * If the controller is showing, calling this method will take effect only the
   * next time the controller is shown.
   * </p>
   *
   * @param animationStyle animation style to use when the controller appears
   *                       and disappears. Set to -1 for the default animation, 0 for no animation, or
   *                       a resource identifier for an explicit animation.
   */
  public void setAnimationStyle(int animationStyle) {
    mAnimStyle = animationStyle;
  }

  /**
   * Show the controller on screen. It will go away automatically after
   * 'timeout' milliseconds of inactivity.
   *
   * @param timeout The timeout in milliseconds. Use 0 to show the controller
   *                until hide() is called.
   */

  public void show(int timeout) {
    if (!mShowing && mAnchor != null && mAnchor.getWindowToken() != null) {
      if (mPauseButton != null)
        mPauseButton.requestFocus();

      if (mFromXml) {
        setVisibility(View.VISIBLE);
      } else {
        int[] location = new int[2];

        mAnchor.getLocationOnScreen(location);
        Rect anchorRect = new Rect(location[0], location[1], location[0] + mAnchor.getWidth(), location[1] + mAnchor.getHeight());

        mWindow.setAnimationStyle(mAnimStyle);
        setWindowLayoutType();
        mWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY, anchorRect.left, anchorRect.bottom);
      }
      mShowing = true;
      if (mShownListener != null)
        mShownListener.onShown();
    }
    showControl();
    updatePausePlay();
    mHandler.sendEmptyMessage(SHOW_PROGRESS);
    mHandler.removeMessages(HIDE_TIP);
    if (timeout != 0) {
      mHandler.removeMessages(FADE_OUT);
      mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT), timeout);
    }
  }

  public boolean isShowing() {
    return mShowing;
  }

  public void hide() {
    if (mAnchor == null)
      return;

    if (mShowing) {
      try {
        mHandler.removeMessages(SHOW_PROGRESS);
        if (mFromXml)
          setVisibility(View.GONE);
        else
          mWindow.dismiss();
      } catch (IllegalArgumentException ex) {
        Log.d("MediaController already removed");
      }
      mShowing = false;
      if (mHiddenListener != null)
        mHiddenListener.onHidden();
      hideAllTip();
    }
  }

  public void setOnShownListener(OnShownListener l) {
    mShownListener = l;
  }

  public void setOnHiddenListener(OnHiddenListener l) {
    mHiddenListener = l;
  }

  public long setProgress() {
    if (mPlayer == null || mDragging)
      return 0;
    long position=mPlayer.getCurrentPosition();
    mCurrentProgress=position;
  //  android.util.Log.i(TAG, "onTouchEvent: ///"+position);
    long duration = mPlayer.getDuration();
    if (mProgress != null) {
      if (duration > 0) {
        long pos = 1000L * position / duration;
        mProgress.setProgress((int) pos);
      }
      int percent = mPlayer.getBufferPercentage();
      mProgress.setSecondaryProgress(percent * 10);
    }

    mDuration = duration;

    if (mEndTime != null)
      mEndTime.setText(StringUtils.generateTime(mDuration));
    if (mCurrentTime != null)
      mCurrentTime.setText(StringUtils.generateTime(position));
    return position;
  }

public void setProgressRightSlide(int position){
      mHandler.removeMessages(SHOW_PROGRESS);
    if (mPlayer == null || mDragging)
        return;
    long duration = mPlayer.getDuration();
    if (mProgress != null) {
        if (duration > 0) {
            long pos = 1000L * position / duration;
            mProgress.setProgress((int) pos);
        }
        int percent = mPlayer.getBufferPercentage();
        mProgress.setSecondaryProgress(percent * 10);
    }
    mDuration = duration;
    if (mEndTime != null)
        mEndTime.setText(StringUtils.generateTime(mDuration));
    if (mCurrentTime != null)
        mCurrentTime.setText(StringUtils.generateTime(position));
}

  /**
   * 隐藏下载和分享图标
   */
  public void hideDownloadAndShareIcon(){
    android.util.Log.i(TAG, "hideDownloadAndShareIcon: ");
   if(mDownloadButton!=null&&mShareButton!=null){
     if(mDownloadButton.getVisibility()==VISIBLE){
       mDownloadButton.setVisibility(GONE);
     }
     if(mShareButton.getVisibility()==VISIBLE){
       mShareButton.setVisibility(GONE);
     }
   }
  }
  /**
   * 显示下载和分享图标
   */
  public void showDownloadAndShareIcon(){
    android.util.Log.i(TAG, "showDownloadAndShareIcon: ");
    if(mDownloadButton!=null&&mShareButton!=null){
      android.util.Log.i(TAG, "showDownloadAndShareIcon: ```");
      if(mDownloadButton.getVisibility()==GONE){
        mDownloadButton.setVisibility(VISIBLE);
      }
      if(mShareButton.getVisibility()==GONE){
        mShareButton.setVisibility(VISIBLE);
      }
    }
  }

  /**
   * 获取系统亮度
   */
  private int getSystemBrightness(){
    int systemBrightness=0;
    try{
      systemBrightness= Settings.System.getInt(((Activity)mContext).getContentResolver(),Settings.System.SCREEN_BRIGHTNESS);
    }
    catch (Exception e){
      e.printStackTrace();
    }
    return systemBrightness;
  }

  /**
   * 改变APP亮度
   */
  public void changeAppBrightness(float brightness){
    Window window=((Activity)mContext).getWindow();
    WindowManager.LayoutParams lp=window.getAttributes();
    if(brightness==-1){//恢复为系统亮度
lp.screenBrightness=WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
    }else{
      if (brightness> 1) {
        lp.screenBrightness = 1;
      } else if (brightness< 0.2) {
        lp.screenBrightness = (float) 0.2;
      }else{
        lp.screenBrightness=brightness;
      }
    }
    mCurrentBrigtness=brightness;
    window.setAttributes(lp);
  }
  //显示相应的提示图标 0代表的是音量，1代表的是亮度，2代表的播放进度,3代表暂停
  private void showOneTipByIndex(int index)
  {
    mHandler.removeMessages(HIDE_TIP);
    hideAllTip();//先隐藏掉所有的图标
    switch (index){
      case 0:
        if(tipLinearLayout!=null&&tipVoiceIcon!=null&&tipSeekBar!=null)
        {
          tipLinearLayout.setVisibility(VISIBLE);
          tipVoiceIcon.setVisibility(VISIBLE);
          tipSeekBar.setVisibility(VISIBLE);
        }
        break;
      case 1:
        if(tipLinearLayout!=null&&tipBrightnessIcon!=null&&tipSeekBar!=null)
        {
          tipLinearLayout.setVisibility(VISIBLE);
          tipBrightnessIcon.setVisibility(VISIBLE);
          tipSeekBar.setVisibility(VISIBLE);
        }
        break;
      case 2:
        if(tipLinearLayout!=null&&tipPlayProgress!=null)
        {
          tipLinearLayout.setVisibility(VISIBLE);
          tipPlayProgress.setVisibility(VISIBLE);
        }
        break;
      case 3:
        if(tipLinearLayout!=null&&tipPause!=null)
        {
          tipLinearLayout.setVisibility(VISIBLE);
          tipPause.setVisibility(VISIBLE);
        }
        default:
          break;
    }
  }



  //设置提示声音的进度
  public void setTipIconVocieProgress(int value){
    if(tipSeekBar!=null){
      tipSeekBar.setProgress(value);
    }
  }
  //设置提示声音的最大值
  public  void  setTipIconVocieMax(){
      if(tipSeekBar!=null){
        tipSeekBar.setMax(mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
      }
  }


  //设置提示亮度的当前值
  public void setTipIconBrightnessProgress(float value){
    if(tipSeekBar!=null){
      tipSeekBar.setProgress((int) (value*1000));
    }
  }
  //设置提示声亮度的最大值
  public  void  setTipIconBrightnessMax(){
    if(tipSeekBar!=null){
      tipSeekBar.setMax(1000);
    }
  }

  //设置播放进度的值
  public void setTipPlayProgress(float  value){
if(tipPlayProgress!=null){
  tipPlayProgress.setText(StringUtils.generateTime((long)value)+"/"+StringUtils.generateTime(mPlayer.getDuration()));
}
  }

  //获取当期的亮度
public float getCurrentBrigtness(){
    return mCurrentBrigtness;
}
  private float mStartY,mStartX,mDownVoice,totalDistance,mDownProgress,mCurrentBrigtness,mCurrentProgress=-1;
  private int isShowTipTye=-1;//正在显示的提示信息，0代表的是音量，1代表的是亮度，2代表的播放进度，-1代表没显示
  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    final float mMaxVoice=mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    mGestureDetector.onTouchEvent(ev);

    switch (ev.getAction()){
      case MotionEvent.ACTION_DOWN:
        mStartX=ev.getX();
        mStartY=ev.getY();
        mDownProgress=mPlayer.getCurrentPosition();
        mDownVoice=mAM.getStreamVolume(AudioManager.STREAM_MUSIC);
        totalDistance=Math.min(mScreenWidth,mScreentHeight);
        mHandler.removeMessages(FADE_OUT);
        break;
      case MotionEvent.ACTION_MOVE:

        float mEndX=ev.getX();
        float mEndY=ev.getY();
        float distance=mStartY-mEndY;
        final double FLINGVERTICAL_MIN_DISTANCE=10;
        final  double FLING_HORIZONTAL_MIN_DISTANCE=5;
        float horizontalDistance=mEndX-mStartX;
         final double FLING_MIN_DISTANCE=3;
        if(Math.abs(horizontalDistance)<FLING_MIN_DISTANCE&&Math.abs(distance)<FLING_MIN_DISTANCE) {//表示单击

         break;
        }
        if(isShowTipTye!=-1){

          switch (isShowTipTye){
            case 0:
              if(mEndX>mScreenWidth/2){
                float delVoice=(distance/totalDistance)*mMaxVoice;
                float mCurrentVoice=Math.min(mMaxVoice,Math.max(0,mDownVoice+delVoice));
                changeVolumeByVoiceValue((int)mCurrentVoice);
                setTipIconVocieProgress((int)mCurrentVoice);
                isShowTipTye=0;
                if(isOnlyShowTipCon){//如果只有提示信息显示
                  hideAllControl();
                  showOneTipByIndex(isShowTipTye);
                }else{
                  showOneTipByIndex(isShowTipTye);
                }
              }
            break;
            case 1:
               if(mEndX<=mScreenWidth/2){
                 float mBright=Math.min(1,Math.max(0,mCurrentBrigtness+(distance/totalDistance)));
                 changeAppBrightness(mBright);
                 setTipIconBrightnessProgress(mBright);
                 isShowTipTye=1;
                 showOneTipByIndex(isShowTipTye);
               }
              break;
            case 2:
              float delProgress=(horizontalDistance/totalDistance)*mPlayer.getDuration();
               mCurrentProgress=Math.min(mPlayer.getDuration(),Math.max(0,mDownProgress+delProgress));
              setProgressRightSlide((int)mCurrentProgress);
              setTipPlayProgress(mCurrentProgress);
              isShowTipTye=2;
              showOneTipByIndex(isShowTipTye);
              break;
          }
        }
        else{

          if(Math.abs(horizontalDistance)>FLING_HORIZONTAL_MIN_DISTANCE&&Math.abs(distance)<FLINGVERTICAL_MIN_DISTANCE){
            float delProgress=(horizontalDistance/totalDistance)*mPlayer.getDuration();
             mCurrentProgress=Math.min(mPlayer.getDuration(),Math.max(0,mDownProgress+delProgress));
              setProgressRightSlide((int)mCurrentProgress);
            setTipPlayProgress(mCurrentProgress);
            isShowTipTye=2;
            showOneTipByIndex(isShowTipTye);
            break;
          }

          //左半边上滑改变亮度，右半边上滑改变音量，水平滑动改变进度
          if(mEndX>mScreenWidth/2){
            float delVoice=(distance/totalDistance)*mMaxVoice;
            float mCurrentVoice=Math.min(mMaxVoice,Math.max(0,mDownVoice+delVoice));
            changeVolumeByVoiceValue((int)mCurrentVoice);
            setTipIconVocieMax();
            setTipIconVocieProgress((int)mCurrentVoice);
            isShowTipTye=0;
            if(isOnlyShowTipCon){//如果只有提示信息显示
              hideAllControl();
              showOneTipByIndex(isShowTipTye);
            }else{
              showOneTipByIndex(isShowTipTye);
            }


          }
          else{

            float mBright=Math.min(1,Math.max(0,mCurrentBrigtness+(distance/totalDistance)));
            //android.util.Log.i(TAG, "onTouchEvent: "+mBright);
            changeAppBrightness(mBright);
            setTipIconBrightnessMax();
            setTipIconBrightnessProgress(mBright);
            isShowTipTye=1;
            showOneTipByIndex(isShowTipTye);
          }

        }

        break;
      case MotionEvent.ACTION_UP:
        if(isShowTipTye==2){
          mPlayer.seekTo((int)mCurrentProgress);
   // android.util.Log.i(TAG, "onTouchEvent: ///"+StringUtils.generateTime((long)mCurrentProgress)+"//"+StringUtils.generateTime(mPlayer.getCurrentPosition())+"//");
        }
        isShowTipTye=-1;
      //  android.util.Log.i(TAG, "onTouchEvent: +++++++++++"+isOnlyShowTipCon);
        if(!isOnlyShowTipCon){
          show();
        }
        mHandler.removeMessages(HIDE_TIPICON);
        mHandler.sendEmptyMessageDelayed(HIDE_TIPICON,2000);//2000毫秒之后自动隐藏掉提示图标
        break;
    }

    return true;
  }

  @Override
  public boolean onTrackballEvent(MotionEvent ev) {
    show(sDefaultTimeout);
    return false;
  }



  //点击键盘上的按钮
  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    int keyCode = event.getKeyCode();
    if (event.getRepeatCount() == 0 && (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE)) {
      doPauseResume();
      show(sDefaultTimeout);
      if (mPauseButton != null)
        mPauseButton.requestFocus();
      return true;
    } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
      if (mPlayer.isPlaying()) {
        mPlayer.pause();
        updatePausePlay();
      }
      return true;
    } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
      hide();
      return true;
    } else {
      show(sDefaultTimeout);
    }
    return super.dispatchKeyEvent(event);
  }

  private void updatePausePlay() {
    if (mRoot == null || mPauseButton == null)
      return;

    if (mPlayer.isPlaying())
      mPauseButton.setImageResource(getResources().getIdentifier("mediacontroller_pause", "drawable", mContext.getPackageName()));
    else
      mPauseButton.setImageResource(getResources().getIdentifier("mediacontroller_play", "drawable", mContext.getPackageName()));

    if(mMedia_tv_systemTime!=null){
      mMedia_tv_systemTime.setText(CommonUtils.getSystemTime());
    }

  }

  private void doPauseResume() {
    if (mPlayer.isPlaying())
    {
      mPlayer.pause();
      showOneTipByIndex(3);
      mHandler.removeMessages(HIDE_TIPICON);
      mHandler.sendEmptyMessageDelayed(HIDE_TIPICON,2000);//2000毫秒之后自动隐藏掉提示图标
    }
    else
    {
      hideAllTip();
      mPlayer.start();
      mPlayer.setPlaybackSpeed(1.0f);
    }
    updatePausePlay();
  }

  private void doPrevResume(){
    mPlayer.prev();
  }

  private void doNextResume(){
    mPlayer.next();
  }

  private void doLoveResume(){
    mPlayer.love();
  }

  private void doDownloadResume(){
    mPlayer.download();
  }

  private void doShareResume(){
    mPlayer.share();
  }





  @Override
  public void setEnabled(boolean enabled) {
    if (mPauseButton != null)
      mPauseButton.setEnabled(enabled);
    if (mProgress != null)
      mProgress.setEnabled(enabled);
    super.setEnabled(enabled);
  }

  public interface OnShownListener {
    public void onShown();
  }

  public interface OnHiddenListener {
    public void onHidden();
  }

  public interface MediaPlayerControl {
    void start();

    void pause();

    void next();

    void prev();

    void love();

    void download();

    void share();
    void setPlaybackSpeed(float speed);

    long getDuration();

    long getCurrentPosition();

    int getVideoWidth();

    void setVideoSize(int width,int height);

    int getVideoHeight();

    void seekTo(long pos);

    boolean isPlaying();

    boolean isLove();

    int getBufferPercentage();
  }

}
