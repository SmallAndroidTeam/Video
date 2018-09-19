package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import io.vov.vitamio.bean.Video;
import io.vov.vitamio.demo.R;
import io.vov.vitamio.utils.StringUtils;
import localData.FileManger;

/**
 * Created by MR.XIE on 2018/9/16.
 */
public class VideoCollectAdapter extends BaseAdapter {
    private List<Video> videoList;
    private Context context;
    private final String TAg="movie";
    public VideoCollectAdapter(List<Video> videoList, Context context) {
        this.videoList = videoList;
        this.context=context;
    }

    public void setVideoList(List<Video> videoList) {
        this.videoList = videoList;
    }

    public  List<Video> getVideoList() {
        return videoList;
    }

    @Override
    public int getCount() {
        return videoList.size();
    }

    @Override
    public Object getItem(int i) {
        return videoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final VideoCollectAdapter.ViewHolder viewHolder;
        if(view==null){
            view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.video_collect_list_item,null);
            ImageView videoImageView=(ImageView)view.findViewById(R.id.videoImageView);
            TextView videoName=(TextView)view.findViewById(R.id.videoName);
            TextView videoSize=(TextView)view.findViewById(R.id.videoSize);
            TextView videoDuration=(TextView)view.findViewById(R.id.videoDuration);
            TextView videoModifiedDate=(TextView)view.findViewById(R.id.videoModifiedDate);
            viewHolder=new VideoCollectAdapter.ViewHolder(videoImageView,videoName,videoSize,videoDuration,videoModifiedDate);
            view.setTag(viewHolder);
        }
        else{
            viewHolder= (VideoCollectAdapter.ViewHolder) view.getTag();
        }
        if(videoList.get(i).getVideoPath()!=null&&videoList.get(i).getThumbnail()==null){//网络图片
            Glide.with(context).load(videoList.get(i).getVideoPath()).into(viewHolder.videoImageView);
            viewHolder.videoSize.setVisibility(View.GONE);
        }else {
            Bitmap videoBitmap=null;
            if(videoList.get(i).getThumbnail()!=null){
                videoBitmap=videoList.get(i).getThumbnail();
            }else{
                videoBitmap= FileManger.getInstance(view.getContext()).getVideoThumbnailById(videoList.get(i).getVdieoId());
                videoList.get(i).setThumbnail(videoBitmap);
            }

            if(videoBitmap==null){
                viewHolder.videoImageView.setImageResource(R.drawable.ic_launcher);
            }else{
                viewHolder.videoImageView.setImageBitmap(videoBitmap);
            }
            viewHolder.videoSize.setVisibility(View.VISIBLE);
            viewHolder.videoSize.setText(String.format("%.2fM",1.0*videoList.get(i).getSize()/1024/1024));
        }
        viewHolder.videoName.setText(videoList.get(i).getVideoName());

        viewHolder.videoDuration.setText(StringUtils.generateTime(videoList.get(i).getDuration()));
        viewHolder.videoModifiedDate.setText(bean.Video.ConvertDate(videoList.get(i).getDate()));

        return view;
    }
    class ViewHolder{
        ImageView videoImageView;
        TextView videoName;
        TextView videoSize;
        TextView videoDuration;
        TextView videoModifiedDate;
        public ViewHolder(ImageView videoImageView, TextView videoName, TextView videoSize, TextView videoDuration, TextView videoModifiedDate) {
            this.videoImageView = videoImageView;
            this.videoName = videoName;
            this.videoSize = videoSize;
            this.videoDuration = videoDuration;
            this.videoModifiedDate = videoModifiedDate;
        }
    }
}
