package io.vov.vitamio.demo.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vov.vitamio.demo.R;
import io.vov.vitamio.demo.utils.DownLoadingState;
import io.vov.vitamio.demo.utils.HttpUtil;
import io.vov.vitamio.demo.utils.SaveVideoDownloadStatus;

/**
 * Created by MR.XIE on 2018/10/16.
 */
public class DownloadAdapter   extends BaseAdapter {
    private List<SaveVideoDownloadStatus> videos=new ArrayList<>();
    private  boolean isShowSelectIcon=false;//表示是否显示选择图标
    private   List<SaveVideoDownloadStatus> selectListVideoAddress=new ArrayList<>();

    //删除选中的下载视频
    public void deleteDownloadVieo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this){
                    Log.i("movie444", "run: "+selectListVideoAddress.size());
                    for(SaveVideoDownloadStatus saveVideoDownloadStatus:selectListVideoAddress){
                        if(saveVideoDownloadStatus.isDownLoading()){//删除正在下载的视频
                            int index=HttpUtil.getDownloadingIndex(saveVideoDownloadStatus.getDownLoadVideoAddress());
                            if(index!=-1){

                                HttpUtil.setVideoDeleteTargetState(index);

                            }
                        }else{//删除暂停的视频

                         SaveVideoDownloadStatus.deleteDownloadVideoByAddress(saveVideoDownloadStatus.getDownLoadVideoAddress());
                        }
                    }


                    for(SaveVideoDownloadStatus saveVideoDownloadStatus:selectListVideoAddress){
                        int index=getVideoIndex(saveVideoDownloadStatus);
                        if(index!=-1){
                            videos.remove(index);
                        }
                    }
                    selectListVideoAddress=new ArrayList<>();
                }
            }
        }).start();

    }

    //获取选中的视频在列表视频对应下标
    public synchronized  int getVideoIndex(SaveVideoDownloadStatus saveVideoDownloadStatus){
        for(int i=0;i<videos.size();i++){
            if(videos.get(i).getDownLoadVideoAddress().trim().contentEquals(saveVideoDownloadStatus.getDownLoadVideoAddress())){
                return i;
            }
        }
        return -1;
    }

    //获取视频在选中视频列表中的下标
    public synchronized  int getSelectVideoIndex(SaveVideoDownloadStatus saveVideoDownloadStatus){
        for(int i=0;i<selectListVideoAddress.size();i++){
            if(selectListVideoAddress.get(i).getDownLoadVideoAddress().trim().contentEquals(saveVideoDownloadStatus.getDownLoadVideoAddress())){
                return i;
            }
        }
        return -1;
    }

    //删除一个 在选中视频列表中的视频
    public synchronized void removeOneSelectVideo(SaveVideoDownloadStatus saveVideoDownloadStatus){
        int index=getSelectVideoIndex(saveVideoDownloadStatus);
        if(index!=-1){
            selectListVideoAddress.remove(index);
        }
    }


    public void setVideos(List<SaveVideoDownloadStatus> videos) {
        this.videos = videos;
    }

    public List<SaveVideoDownloadStatus> getSelectListVideoAddress() {
        return selectListVideoAddress;
    }

    public void setSelectListVideoAddress(List<SaveVideoDownloadStatus> selectListVideoAddress) {
        this.selectListVideoAddress = selectListVideoAddress;
    }

    public boolean isShowSelectIcon() {
        return isShowSelectIcon;
    }

    public void setShowSelectIcon(boolean showSelectIcon) {
        isShowSelectIcon = showSelectIcon;
    }

    public List<SaveVideoDownloadStatus> getVideos() {
        return videos;
    }

    @Override
    public int getCount() {
        return videos.size();
    }

    @Override
    public Object getItem(int position) {
        return videos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
//判断此时的视频是否被选中
    public synchronized boolean isSelect(SaveVideoDownloadStatus saveVideoDownloadStatus){
        for(SaveVideoDownloadStatus saveVideoDownloadStatus1:selectListVideoAddress){
            if(saveVideoDownloadStatus1.getDownLoadVideoAddress().trim().contentEquals(saveVideoDownloadStatus.getDownLoadVideoAddress().trim())){
                return true;
            }
        }
        return false;
    }
    @SuppressLint("DefaultLocale")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
         ViewHolder viewHolder=null;
      if(convertView==null){
           convertView= LayoutInflater.from(parent.getContext()).inflate(R.layout.download_videolist_item,parent,false);
          RadioButton select_radiobuton=convertView.findViewById(R.id.select_radiobuton);
          ImageView download_video_image=convertView.findViewById(R.id.download_video_image);
          TextView download_title=convertView.findViewById(R.id.download_title);
          SeekBar download_progress= convertView.findViewById(R.id.download_progress);
          TextView download_state=convertView.findViewById(R.id.download_state);
          TextView download_amount=convertView.findViewById(R.id.download_amount);
          TextView download_total_amount=convertView.findViewById(R.id.download_total_amount);
          viewHolder=new ViewHolder(select_radiobuton,download_video_image,download_title,download_progress,download_state,download_amount,download_total_amount);
          convertView.setTag(viewHolder);
      }else{
       viewHolder= (ViewHolder) convertView.getTag();
      }

      if(!isShowSelectIcon){
          viewHolder.select_radiobuton.setVisibility(View.GONE);
          viewHolder.select_radiobuton.setChecked(false);
      }else{


          if(selectListVideoAddress.size()>0)

          if(isSelect(videos.get(position))){
              viewHolder.select_radiobuton.setChecked(true);
          }else{
              viewHolder.select_radiobuton.setChecked(false);
          }

          viewHolder.select_radiobuton.setVisibility(View.VISIBLE);
      }


        Log.i("movie11", position+"//"+viewHolder.select_radiobuton.isChecked());
        Glide.with(parent.getContext()).load(videos.get(position).getCoverImg()).into(viewHolder.download_video_image);
        viewHolder.download_title.setText(videos.get(position).getVideoName());

         if(videos.get(position).isDownLoading()){
             viewHolder.download_state.setText("正在下载");
         }
       else{
             viewHolder.download_state.setText("暂停下载");
         }

        long downloadAmount=videos.get(position).getDownByteNumber();
        long downloadTotalAmount=videos.get(position).getVideoSize();

        viewHolder.download_progress.setClickable(false);
        viewHolder.download_progress.setEnabled(false);
        viewHolder.download_progress.setSelected(false);
        viewHolder.download_progress.setFocusable(false);
        viewHolder.download_progress.setProgress((int) (100.0*downloadAmount/downloadTotalAmount));
        viewHolder.download_amount.setText(String.format("%.1fM",1.0*downloadAmount/1024/1024));
        viewHolder.download_total_amount.setText(String.format("%.1fM",1.0*downloadTotalAmount/1024/1024));
      return convertView;
    }

    public class ViewHolder{
     RadioButton select_radiobuton;
     ImageView download_video_image;
     TextView download_title;
     SeekBar download_progress;
     TextView download_state;
     TextView download_amount;
     TextView download_total_amount;

        public ViewHolder(RadioButton select_radiobuton, ImageView download_video_image, TextView download_title, SeekBar download_progress, TextView download_state, TextView download_amount, TextView download_total_amount) {
            this.select_radiobuton = select_radiobuton;
            this.download_video_image = download_video_image;
            this.download_title = download_title;
            this.download_progress = download_progress;
            this.download_state = download_state;
            this.download_amount = download_amount;
            this.download_total_amount = download_total_amount;
        }

        public RadioButton getSelect_radiobuton() {
            return select_radiobuton;
        }

        public ImageView getDownload_video_image() {
            return download_video_image;
        }

        public TextView getDownload_title() {
            return download_title;
        }

        public SeekBar getDownload_progress() {
            return download_progress;
        }

        public TextView getDownload_state() {
            return download_state;
        }

        public TextView getDownload_amount() {
            return download_amount;
        }

        public TextView getDownload_total_amount() {
            return download_total_amount;
        }
    }
}
