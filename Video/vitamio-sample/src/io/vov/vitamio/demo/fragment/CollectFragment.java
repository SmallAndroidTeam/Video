package io.vov.vitamio.demo.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import io.vov.vitamio.bean.Video;
import io.vov.vitamio.demo.R;
import io.vov.vitamio.demo.activity.PlayActivity;
import io.vov.vitamio.demo.adapter.VideoCollectAdapter;
import io.vov.vitamio.provider.VideoCollectOperation;

/**
 * Created by MR.XIE on 2018/9/7.
 */
public class CollectFragment extends Fragment {

    private ListView videoCollectListView;
    private VideoCollectOperation videoCollectOperation;
    private final  String TAG="movie";
    private VideoCollectAdapter videoCollectAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.collect_fragment,container,false);
        initView(view);
        videoCollectAdapter = new VideoCollectAdapter(videoCollectOperation.getVideoCollect(),container.getContext());
        videoCollectListView.setAdapter(videoCollectAdapter);
       initOnclickListener();//设置点击监听事件
        return  view;
    }
    private void initOnclickListener() {
        videoCollectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                List<Video> videoList= videoCollectAdapter.getVideoList();
                PlayActivity.setVideoList(videoList);
                Intent intent=new Intent(getActivity(), PlayActivity.class);
                intent.putExtra("position",i);
                startActivity(intent);
                // oneToast.showMessage(getActivity(),videoList.get(i).getVideoPath());
            }
        });
    }
    private void initView(View view) {
        videoCollectListView = (ListView) view.findViewById(R.id.videoCollectListView);
        videoCollectOperation = new VideoCollectOperation(view.getContext());
    }

    //当点击收藏和取消时更新视图
    public  void updateCollectFragmentView(){
        videoCollectAdapter.setVideoList(videoCollectOperation.getVideoCollect());
        videoCollectAdapter.notifyDataSetChanged();
    }


    //删除一个不存在的视频更新视图
    public void deleteOneVideoUpateView(int position, String path) {
        List<Video> videoList= videoCollectAdapter.getVideoList();
        if(position>=videoList.size()||position<0)
        {
            return;
        }
        if(videoList.get(position).getVideoPath().equals(path)){
            videoList.remove(position);
            videoCollectAdapter.setVideoList(videoList);
            videoCollectAdapter.notifyDataSetChanged();
            videoCollectOperation.cancelCollectVide(path);
        }else{
           for(Video video:videoList){
               if(video.getVideoPath().equals(path)){
                   videoCollectOperation.cancelCollectVide(path);
                   updateCollectFragmentView();
                   break;
               }
           }


        }
    }

}
