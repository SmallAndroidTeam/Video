package io.vov.vitamio.demo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import io.vov.vitamio.demo.R;
import io.vov.vitamio.demo.bean.ShareApplication;

/**
 * Created by MR.XIE on 2018/9/27.
 */
public class ShareAdapter extends RecyclerView.Adapter<ShareAdapter.ViewHolder> implements View.OnClickListener {
    private Context context;
    private List<ShareApplication> shareApplications;
    private onItemclickListener onItemclickListener;
    public ShareAdapter(Context context, List<ShareApplication> shareApplications) {
        this.context = context;
        this.shareApplications = shareApplications;
    }

    public void setOnItemclickListener(ShareAdapter.onItemclickListener onItemclickListener) {
        this.onItemclickListener = onItemclickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.shareview_item,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      holder.share_Icon.setImageResource(shareApplications.get(position).getShare_image_id());
      holder.share_name.setText(shareApplications.get(position).getShare_name());
      holder.share_linearlayout.setTag(position);
    }

    @Override
    public int getItemCount() {
        return shareApplications.size();
    }

    @Override
    public void onClick(View v) {
        if(onItemclickListener!=null){
            onItemclickListener.onClick(v, (Integer) v.getTag());
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout share_linearlayout;
        ImageView share_Icon;
        TextView share_name;

        public ViewHolder(View itemView) {
            super(itemView);
            share_linearlayout=(LinearLayout)itemView.findViewById(R.id.share_linearlayout);
            share_Icon=(ImageView)itemView.findViewById(R.id.share_Icon);
            share_name=(TextView)itemView.findViewById(R.id.share_name);
            share_linearlayout.setOnClickListener(ShareAdapter.this);
        }
    }
  public   interface onItemclickListener{
        void onClick(View view,int position);
   }
}
