package com.of.vmvideo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.of.vmvideo.R;


/**
 * Created by MR.XIE on 2018/9/24.
 */
public class OnlineAdapter extends RecyclerView.Adapter<OnlineAdapter.ViewHolder> implements View.OnClickListener {
    private    String[] titleList={"院线独播","今日精彩","猜你喜欢","热门电影","热门电视","热门综艺","热门新闻"};
    private final  static String TAG="movie2";
    private String titleName="热门电影";//默认设置哪一个分类标题选中
    private Context mContext;
    private OnItemClickListener mItemClickListener;
    public OnlineAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setonItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public void onClick(View v) {
      if(mItemClickListener!=null){
          mItemClickListener.onItemClick(v, (Integer) v.getTag());
      }
    }

    public interface OnItemClickListener{
        void onItemClick(View view,int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleView=(TextView)itemView.findViewById(R.id.titleView);
            //绑定点击事件
            titleView.setOnClickListener(OnlineAdapter.this);
        }

    }

    public String[] getTitleList() {
        return titleList;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.online_video_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.titleView.setText(titleList[position]);
    holder.titleView.setTag(position);
    if(titleList[position].contentEquals(titleName)){
        holder.titleView.setTextColor(mContext.getResources().getColor(R.color.indexTextSelect));
    }

    }

    @Override
    public int getItemCount() {
        return titleList.length;
    }



}
