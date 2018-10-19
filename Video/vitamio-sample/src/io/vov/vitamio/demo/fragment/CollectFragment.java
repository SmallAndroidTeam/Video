package io.vov.vitamio.demo.fragment;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.vov.vitamio.bean.Video;
import io.vov.vitamio.demo.R;
import io.vov.vitamio.demo.activity.PlayActivity;
import io.vov.vitamio.demo.adapter.VideoCollectAdapter;
import io.vov.vitamio.demo.localData.FileManger;
import io.vov.vitamio.provider.VideoCollectOperation;
import io.vov.vitamio.toast.oneToast;

/**
 * Created by MR.XIE on 2018/9/7.
 */
public class CollectFragment extends Fragment {

    private ListView videoCollectListView;
    private VideoCollectOperation videoCollectOperation;
    private final  String TAG="movie";
    private VideoCollectAdapter videoCollectAdapter;
    private TextView collect_tip;//没有收藏视频提示收藏
    private final static int DELETE_UPDATE=0;//删除视频更新
    private final static int HIDE_TIP_SHOW_LOADING=1;//隐藏提示，显示加载按钮
    private final static int HIDE_LOADING=2;//显示提示，隐藏加载按钮
    private final static int CLICK_REGRESH=3;//点击刷新
    private final  static int REFRESH=4;
    private final  static  int COLLECT_REGRESH=5;//收藏更新
    private boolean isCollect_REFRESH=false;//是否要进行收藏更新
    private List<Video> videos=new ArrayList<>();
    @SuppressLint("HandlerLeak")
    private Handler mHander=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case DELETE_UPDATE:
                    Bundle bundle=msg.getData();
                    int position= (int) bundle.getSerializable("position");
                    String path= (String) bundle.getSerializable("path");
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
                        if(videoList.size()>0){
                            collect_tip.setVisibility(View.GONE);
                        }else{
                            collect_tip.setVisibility(View.VISIBLE);
                        }
                    }else{
                        for(Video video:videoList){
                            if(video.getVideoPath().equals(path)){
                                videoCollectOperation.cancelCollectVide(path);
                                updateCollectFragmentView();
                                break;
                            }
                        }
                    }
                    break;
                case HIDE_TIP_SHOW_LOADING:
                    collect_tip.setVisibility(View.GONE);
                    loading_layout.setVisibility(View.VISIBLE);
                    break;
                case HIDE_LOADING:
                    Bundle bundle1=msg.getData();
                    int result= (int) bundle1.getSerializable("result");
                    if(result==0){
                        collect_tip.setVisibility(View.VISIBLE);
                    }else if(result==1){
                        collect_tip.setVisibility(View.GONE);
                    }
                    loading_layout.setVisibility(View.GONE);

                    collectVideoRefresh.setRefreshing(false);
                    break;
                case CLICK_REGRESH:

                    if(videoCollectAdapter==null) {
                        videoCollectAdapter = new VideoCollectAdapter(videos, getContext());
                        videoCollectListView.setAdapter(videoCollectAdapter);
                    }else{
                        videoCollectAdapter.setVideoList(videos);
                        videoCollectAdapter.notifyDataSetChanged();
                    }

                    Bundle bundle2=new Bundle();
                    if(videos.size()>0){
                        bundle2.putSerializable("result",1);
                    }else{
                        bundle2.putSerializable("result",0);
                    }
                    Message message=new Message();
                    message.setData(bundle2);
                    message.what=HIDE_LOADING;
                    mHander.sendMessageDelayed(message,500);
                    break;
                case REFRESH:
                    if(videoCollectAdapter==null) {
                        videoCollectAdapter = new VideoCollectAdapter(videos, getContext());
                        videoCollectListView.setAdapter(videoCollectAdapter);
                    }else{
                        videoCollectAdapter.setVideoList(videos);
                        videoCollectAdapter.notifyDataSetChanged();
                    }
                    if(videos.size()>0){
                       collect_tip.setVisibility(View.GONE);
                    }else{
                        collect_tip.setVisibility(View.VISIBLE);
                    }
                    oneToast.showMessage(getContext(),"刷新成功");
                    collectVideoRefresh.setRefreshing(false);
                    break;
                case COLLECT_REGRESH:
                    videoCollectAdapter.notifyDataSetChanged();
                    if(videos.size()>0){
                        collect_tip.setVisibility(View.GONE);
                    }else{
                        collect_tip.setVisibility(View.VISIBLE);
                    }
                    collect_tip.setVisibility(View.GONE);
                    loading_layout.setVisibility(View.GONE);
                    isCollect_REFRESH=false;
                    break;
                    default:
                        break;
            }
        }
    };
    private LinearLayout loading_layout;
    private SwipeRefreshLayout collectVideoRefresh;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.collect_fragment,container,false);
        initView(view);
//        videoCollectAdapter = new VideoCollectAdapter(videoList,container.getContext());
//        videoCollectListView.setAdapter(videoCollectAdapter);
//        if(videoList.size()>0){
//            collect_tip.setVisibility(View.GONE);
//        }else{
//            collect_tip.setVisibility(View.VISIBLE);
//        }
//
       initOnclickListener();//设置点击监听事件
        collect_tip.callOnClick();
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
        collectVideoRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
              new Thread(new Runnable() {
                  @Override
                  public void run() {
                  synchronized (this){

                      List<Video> videoList= videoCollectOperation.getVideoCollect();
                      videos=videoList;
                      mHander.sendEmptyMessage(REFRESH);

                  }
                  }
              }).start();
            }
        });
        collect_tip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (this){
                            mHander.sendEmptyMessage(HIDE_TIP_SHOW_LOADING);
                            List<Video> videoList= videoCollectOperation.getVideoCollect();
                            videos=videoList;
                            mHander.sendEmptyMessage(CLICK_REGRESH);
                        }
                    }
                }).start();

            }
        });
    }
    private void initView(View view) {
        videoCollectListView = (ListView) view.findViewById(R.id.videoCollectListView);
        videoCollectOperation = new VideoCollectOperation(view.getContext());
        collect_tip = view.findViewById(R.id.collect_tip);
        loading_layout = view.findViewById(R.id.loading_layout);
        collectVideoRefresh = view.findViewById(R.id.collectVideoRefresh);
        collectVideoRefresh.setProgressBackgroundColorSchemeResource(R.color.RefreshProgressBackground);
        collectVideoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,android.R.color.holo_green_light,
                android.R.color.holo_orange_light,android.R.color.holo_red_light);

    }

    //当点击收藏和取消时更新视图
    public  void updateCollectFragmentView(){
        isCollect_REFRESH=true;

    }
    //收藏后更新视图
    public void updateView(){
        if(isCollect_REFRESH){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mHander.sendEmptyMessage(HIDE_TIP_SHOW_LOADING);
                    videos= videoCollectOperation.getVideoCollect();
                    videoCollectAdapter.setVideoList(videos);
                    mHander.sendEmptyMessage(COLLECT_REGRESH);
                }
            }).start();
        }

    }

    //删除一个不存在的视频更新视图(播放的时候如果不存在某个视频就删掉）
    public void deleteOneVideoUpateView(int position, String path) {
        Message message=new Message();
        Bundle bundle=new Bundle();
        bundle.putSerializable("position",position);
        bundle.putSerializable("path",path);
        message.what=DELETE_UPDATE;
        mHander.sendMessage(message);

    }

}
