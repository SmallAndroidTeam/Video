package io.vov.vitamio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import io.vov.vitamio.R;

/**
 * Created by MR.XIE on 2018/9/13.
 */
public class PlayModeAdapter extends BaseAdapter {
    private String[] data;

    public PlayModeAdapter(String[] data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int i) {
        return data[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
       final viewHolder viewHolder;
        if(view==null){
            view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.play_mode_list_item,null);
            TextView modeTextView=(TextView)view.findViewById(R.id.modeTextView);
            viewHolder=new viewHolder(modeTextView);
            view.setTag(viewHolder);
        }
        else{
            viewHolder= (PlayModeAdapter.viewHolder) view.getTag();
        }
        viewHolder.modeTextView.setText(data[i]);
        return view;
    }
    class viewHolder{
        TextView modeTextView;
        public viewHolder(TextView textView) {
            this.modeTextView = textView;
        }
    }
}
