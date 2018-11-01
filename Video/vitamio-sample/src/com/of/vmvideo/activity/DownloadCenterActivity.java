package com.of.vmvideo.activity;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.of.vmvideo.R;
import com.of.vmvideo.adapter.DownloadAdapter;
import com.of.vmvideo.service.DownLoadService;
import com.of.vmvideo.utils.DownLoadingState;
import com.of.vmvideo.utils.HttpUtil;
import com.of.vmvideo.utils.SaveVideoDownloadStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import io.vov.vitamio.toast.oneToast;

/**
 * Created by MR.XIE on 2018/10/17.
 */
public class DownloadCenterActivity extends Activity implements View.OnClickListener {

    private ImageView download_centre_back;
    private TextView downlaod_control;
    private final static String[] messages=new String[]{"编辑","取消"};
    private ListView download_videolist;
    private DownloadAdapter downloadAdapter;
    private static List<SaveVideoDownloadStatus> downloadVideos=new ArrayList<>();
    private LinearLayout selectDelete;
    private TextView selectAll;
    private TextView deleteSelect;
    private static boolean isExists=false;//判断界面是否存在
    private final  static int UPTATE_DOWNLOAD_VIEW=0;//更新下载视图
    @SuppressLint("HandlerLeak")
  private Handler mhander=new Handler(){
      @Override
      public void handleMessage(Message msg) {
        switch (msg.what){
            case UPTATE_DOWNLOAD_VIEW:
                downloadAdapter.notifyDataSetChanged();
                if(downloadAdapter.getVideos().size()==0){
                    downlaod_control.callOnClick();
                }
                break;
                default:
                    break;
        }
      }
  };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_centre);
        initview();
        addData();
        initListener();
        isExists=true;
        DownLoadService.setDownloadCenterActivityContext(this);
    }

    public static boolean isIsExists() {
        return isExists;
    }

    public static List<SaveVideoDownloadStatus> getDownloadVideos() {
        return downloadVideos;
    }

    public static void setDownloadVideos(List<SaveVideoDownloadStatus> downloadVideos) {
        DownloadCenterActivity.downloadVideos = downloadVideos;
    }



    private void addData() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                List<SaveVideoDownloadStatus> saveVideoDownloadStatuses=SaveVideoDownloadStatus.getVideoDownloadStatusList();//获取暂停的下载视频
                List<DownLoadingState> downloadingVideoPath= HttpUtil.downloadingVideoPath;//获取所有下载视频的信息（包括暂停的）
                downloadVideos.clear();
                for(DownLoadingState downLoadingState:downloadingVideoPath){
                    if(!downLoadingState.isDownComplete()&&downLoadingState.getCurrentState()==0){//如果视频没下载完,且正在下载
                        SaveVideoDownloadStatus saveVideoDownloadStatus=new SaveVideoDownloadStatus();
                        saveVideoDownloadStatus.setDownLoadVideoAddress(downLoadingState.getDownLoadVideoAddress());
                        saveVideoDownloadStatus.setVideoName(downLoadingState.getDownLoadVideoName());
                        saveVideoDownloadStatus.setCoverImg(downLoadingState.getCoverImg());
                        saveVideoDownloadStatus.setDownByteNumber(downLoadingState.getDownByteNumber());
                        saveVideoDownloadStatus.setVideoSize(downLoadingState.getVideoSize());
                        saveVideoDownloadStatus.setDownLoading(true);
                        downloadVideos.add(saveVideoDownloadStatus);
                    }
                }
                for(int i=0;i<saveVideoDownloadStatuses.size();i++){
                    SaveVideoDownloadStatus videoDownloadStatusList=saveVideoDownloadStatuses.get(i);
                    if(!videoDownloadStatusList.isDownLoading()){
                        downloadVideos.add(videoDownloadStatusList);
                    }
                }

                downloadAdapter = new DownloadAdapter();
                downloadAdapter.setVideos(downloadVideos);
                download_videolist.setAdapter(downloadAdapter);
                updateDate();
            }
        }).start();
    }




    private final static int DELAY_TIME=50;//延迟时间
    private Runnable runnable=null;
    //更新下载视图
    private void updateDate(){
         runnable= new Runnable() {
            @Override
            public void run() {
                   synchronized (this){
                       List<SaveVideoDownloadStatus> saveVideoDownloadStatuses=SaveVideoDownloadStatus.getVideoDownloadStatusList();//获取暂停的下载视频
                       List<DownLoadingState> downloadingVideoPath= HttpUtil.downloadingVideoPath;//获取所有下载视频的信息（包括暂停的）
                       downloadVideos.clear();
                       for(DownLoadingState downLoadingState:downloadingVideoPath){
                           if(!downLoadingState.isDownComplete()&&downLoadingState.getCurrentState()==0){//如果视频没下载完,且正在下载
                               SaveVideoDownloadStatus saveVideoDownloadStatus=new SaveVideoDownloadStatus();
                               saveVideoDownloadStatus.setDownLoadVideoAddress(downLoadingState.getDownLoadVideoAddress());
                               saveVideoDownloadStatus.setVideoName(downLoadingState.getDownLoadVideoName());
                               saveVideoDownloadStatus.setCoverImg(downLoadingState.getCoverImg());
                               saveVideoDownloadStatus.setDownByteNumber(downLoadingState.getDownByteNumber());
                               saveVideoDownloadStatus.setVideoSize(downLoadingState.getVideoSize());
                               saveVideoDownloadStatus.setDownLoading(true);
                               downloadVideos.add(saveVideoDownloadStatus);
                           }
                       }
                       for(int i=0;i<saveVideoDownloadStatuses.size();i++){
                           SaveVideoDownloadStatus videoDownloadStatusList=saveVideoDownloadStatuses.get(i);
                           if(!videoDownloadStatusList.isDownLoading()){
                               downloadVideos.add(videoDownloadStatusList);
                           }
                       }
                       downloadAdapter.setVideos(downloadVideos);
                       mhander.sendEmptyMessage(UPTATE_DOWNLOAD_VIEW);
                       mhander.postDelayed(this,DELAY_TIME);
                   }

            }
        };
     mhander.post(runnable);
    }












    @Override
    protected void onDestroy() {
        super.onDestroy();
        mhander.removeCallbacks(runnable);
        isExists=false;
    }

    private void initListener() {
        download_centre_back.setOnClickListener(this);
        downlaod_control.setOnClickListener(this);
        download_videolist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                synchronized (this){
                    Log.i("movie111", "onItemClick111: "+position);
                    final  String stateMessage="暂停下载";
                    DownloadAdapter.ViewHolder viewHolder= (DownloadAdapter.ViewHolder) view.getTag();
                    TextView download_state=viewHolder.getDownload_state();
                    RadioButton select_radiobuton=viewHolder.getSelect_radiobuton();

                    if(select_radiobuton.getVisibility()==View.VISIBLE){
                        Log.i("movie111", "onItemClick222: 111");
                        if(select_radiobuton.isChecked()){//取消


                            downloadAdapter.removeOneSelectVideo(downloadAdapter.getVideos().get(position));
                            select_radiobuton.setChecked(false);
                            if(downloadAdapter.getSelectListVideoAddress().size()>0)
                            {  deleteSelect.setTextColor(getResources().getColor(R.color.deleteEnable));
                                deleteSelect.setEnabled(true);
                                deleteSelect.setText("删除("+downloadAdapter.getSelectListVideoAddress().size()+")");
                            }
                            else{
                                deleteSelect.setText("删除");
                                deleteSelect.setTextColor(getResources().getColor(R.color.deleteUnEnable));
                                deleteSelect.setEnabled(false);
                            }
                            selectAll.setText("全选");
                        }else{//选中
                            List<SaveVideoDownloadStatus> selectListVideoAddress=downloadAdapter.getSelectListVideoAddress();
                            selectListVideoAddress.add(downloadAdapter.getVideos().get(position));
                            downloadAdapter.setSelectListVideoAddress(selectListVideoAddress);

                            select_radiobuton.setChecked(true);
                            deleteSelect.setTextColor(getResources().getColor(R.color.deleteEnable));
                            deleteSelect.setEnabled(true);
                            deleteSelect.setText("删除("+selectListVideoAddress.size()+")");
                            if(selectListVideoAddress.size()==downloadVideos.size()){
                                selectAll.setText("取消全选");
                            }
                        }
                        return;
                    }

                    Log.i("movie111", "onItemClick333: 222");

                    String   downloadVideoAddress= downloadVideos.get(position).getDownLoadVideoAddress();//当前下载的视频地址

                    if(downloadAdapter.getVideos().get(position).isDownLoading()){//当前正在下载
                        int index=HttpUtil.getDownloadingIndex(downloadVideoAddress);
                        Log.i("movie1111", "onItemClick: "+index);
                        if(index!=-1){
                            List<DownLoadingState> downloadingVideoPath=HttpUtil.downloadingVideoPath;
                            DownLoadingState downLoadingState=downloadingVideoPath.get(index);
                            downLoadingState.setTargetState(1);
                            downloadingVideoPath.set(index,downLoadingState);
                            HttpUtil.downloadingVideoPath=downloadingVideoPath;
                        }
                        download_state.setText("暂停下载");

                    }else{

                        int pauseVideoListIndex=SaveVideoDownloadStatus.getDownloadIndexByVideoAddress(downloadVideoAddress);
                        Log.i("movie111", "onItemClick4444: "+pauseVideoListIndex);
                        if(pauseVideoListIndex!=-1){
                            List<SaveVideoDownloadStatus> videoDownloadStatusList=SaveVideoDownloadStatus.getVideoDownloadStatusList();
                            HttpUtil httpUtil=new HttpUtil(videoDownloadStatusList.get(pauseVideoListIndex));
                            httpUtil.continueDownLoad();
                            SaveVideoDownloadStatus.setDownloadStatus(pauseVideoListIndex);
                            SaveVideoDownloadStatus.deleteOneDownLoadStatusByCurrentID(videoDownloadStatusList.get(pauseVideoListIndex).getCurrentID());
                        }
                        download_state.setText("正在下载");
                    }

                }

            }
        });
        //选择所有
        selectAll.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"SetTextI18n", "ResourceAsColor"})
            @Override
            public void onClick(View v) {
                if(selectAll.getText().toString().trim().contentEquals("全选")){

                    downloadAdapter.setSelectListVideoAddress(downloadAdapter.getVideos());
                    downloadAdapter.notifyDataSetChanged();
                    selectAll.setText("取消全选");
                    deleteSelect.setEnabled(true);
                    deleteSelect.setText("删除("+downloadAdapter.getSelectListVideoAddress().size()+")");
                    deleteSelect.setTextColor(getResources().getColor(R.color.deleteEnable));
                }else{
                    downloadAdapter.setSelectListVideoAddress(new ArrayList<SaveVideoDownloadStatus>());
                    downloadAdapter.notifyDataSetChanged();
                    selectAll.setText("全选");
                    deleteSelect.setText("删除");
                    deleteSelect.setEnabled(false);
                    deleteSelect.setTextColor(getResources().getColor(R.color.deleteUnEnable));
                }

            }
        });

        deleteSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog().show();

            }
        });
    }
    public Dialog alertDialog(){
        final   View view = LayoutInflater.from(this).inflate(R.layout.alert_delete_dialog, null);
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
                downlaod_control.callOnClick();
            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                int count=downloadAdapter.getSelectListVideoAddress().size();
                downloadAdapter.deleteDownloadVieo();
                downloadAdapter.notifyDataSetChanged();
                downloadAdapter.setShowSelectIcon(false);
                selectDelete.setVisibility(View.GONE);
                deleteSelect.setEnabled(false);
                deleteSelect.setTextColor(getResources().getColor(R.color.deleteUnEnable));
                downlaod_control.setText(messages[0]);
                oneToast.showMessage(getApplication(),"已为您删除"+count+"个视频");
            }
        });
        WindowManager.LayoutParams lp = window.getAttributes(); // 获取对话框当前的参数值
        lp.width =WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height =WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        return dialog;
    }

    private void initview() {
        download_centre_back = (ImageView)this.findViewById(R.id.download_centre_back);
        downlaod_control = (TextView)this.findViewById(R.id.downlaod_control);
        download_videolist = (ListView)this.findViewById(R.id.download_videolist);
        selectDelete = this.findViewById(R.id.selectDelete);
        selectAll = this.findViewById(R.id.selectAll);
        deleteSelect = this.findViewById(R.id.deleteSelect);
        deleteSelect.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.download_centre_back:
                this.finish();
                DownloadCenterActivity.this.overridePendingTransition(R.anim.in_from_right,R.anim.out_from_left);
                break;
            case R.id.downlaod_control:
                dowmload_control_click(downlaod_control.getText().toString().trim());
                break;
                default:
                    break;
        }
    }

    @SuppressLint("ResourceAsColor")
    private void dowmload_control_click(String value){
        if(value.contentEquals(messages[0])){//编辑
            if(downloadVideos.size()>0){
                downloadAdapter.setShowSelectIcon(true);
                downloadAdapter.notifyDataSetChanged();
                selectAll.setText("全选");
                deleteSelect.setText("删除");
                selectDelete.setVisibility(View.VISIBLE);
                downlaod_control.setText(messages[1]);
            }

        }else if(value.contentEquals(messages[1])){//取消
            downloadAdapter.setSelectListVideoAddress(new ArrayList<SaveVideoDownloadStatus>());
            downloadAdapter.setShowSelectIcon(false);
            downloadAdapter.notifyDataSetChanged();
            selectDelete.setVisibility(View.GONE);
             deleteSelect.setEnabled(false);
            deleteSelect.setTextColor(getResources().getColor(R.color.deleteUnEnable));
            downlaod_control.setText(messages[0]);
        }
    }



}
