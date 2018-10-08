package io.vov.vitamio.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import java.util.List;

import io.vov.vitamio.demo.R;
import io.vov.vitamio.demo.domain.NetMediaItem;

/**
 * Created by MR.XIE on 2018/9/19.
 */
public class FilmVideoAdapter extends BaseAdapter {
    private List<NetMediaItem.TrailersBean> mTrailersBean;
    private Context mContext;
    private int current_type=0;//当前的类型
    private static final int  TYPE_ITEN=0;//普通的item view
    private static final int TYPE_FOOTER=1;//底部FootView;

    public FilmVideoAdapter(List<NetMediaItem.TrailersBean> mTrailersBean, Context mContext) {
        this.mTrailersBean = mTrailersBean;
        this.mContext = mContext;
    }

    public List<NetMediaItem.TrailersBean> getmTrailersBean() {
        return mTrailersBean;
    }

    public void setmTrailersBean(List<NetMediaItem.TrailersBean> mTrailersBean) {
        this.mTrailersBean = mTrailersBean;
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mTrailersBean.size();
    }

    @Override
    public Object getItem(int position) {
        return mTrailersBean.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //获取当前的子项的类型
//    @Override
//    public int getItemViewType(int position) {
//
//        if(position+1==getCount()){
//            current_type=TYPE_FOOTER;
//        }else
//        {
//            current_type=TYPE_ITEN;
//        }
//        return  current_type;
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView==null){
            convertView= LayoutInflater.from(parent.getContext()).inflate(R.layout.film_fragment_item,null);
            ImageView item_net_video_img=(ImageView)convertView.findViewById(R.id.item_net_video_img);
            TextView item_net_video_tv_title=(TextView)convertView.findViewById(R.id.item_net_video_tv_title);
            TextView item_net_video_tv_desc=(TextView)convertView.findViewById(R.id.item_net_video_tv_desc);
            viewHolder=new ViewHolder(item_net_video_img,item_net_video_tv_title,item_net_video_tv_desc);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder= (ViewHolder) convertView.getTag();
        }
         Glide.with(mContext).load(mTrailersBean.get(position).getCoverImg()).into(viewHolder.item_net_video_img);
         viewHolder.item_net_video_tv_title.setText(mTrailersBean.get(position).getVideoTitle());
         viewHolder.item_net_video_tv_desc.setText(mTrailersBean.get(position).getSummary());
       return convertView;
    }
    class ViewHolder{
        ImageView item_net_video_img;
        TextView item_net_video_tv_title;
        TextView item_net_video_tv_desc;

        public ViewHolder(ImageView item_net_video_img, TextView item_net_video_tv_title, TextView item_net_video_tv_desc) {
            this.item_net_video_img = item_net_video_img;
            this.item_net_video_tv_title = item_net_video_tv_title;
            this.item_net_video_tv_desc = item_net_video_tv_desc;
        }
    }
}
