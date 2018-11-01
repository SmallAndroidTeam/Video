package com.of.vmvideo.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.of.vmvideo.R;
import com.of.vmvideo.activity.PlayActivity;
import com.of.vmvideo.adapter.VideoAdapter;
import com.of.vmvideo.localData.FileManger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.vov.vitamio.bean.Video;

import io.vov.vitamio.toast.oneToast;

/**
 * Created by MR.XIE on 2018/9/7.
 */
public class SdCardFragment extends Fragment {

    private ListView videoListView;
    private final  String TAG="movie";
    private VideoAdapter videoApaper;
    private SwipeRefreshLayout localVideoRefresh;
    private final  static int REFRESH=0;
    private final static int DOWN_REFRESH=1;//下载视频后更新
    private final static int DELETE_UPDATE=2;//删除视频更新
    private final static int HIDE_TIP=3;//隐藏提示信息
    private final static int SHOW_TIP=4;//显示提示信息
    private final static int HIDE_TIP_SHOW_LOADING=5;//隐藏提示，显示加载按钮
    private final static int HIDE_LOADING=6;//显示提示，隐藏加载按钮
    private final static int CLICK_REGRESH=7;//点击刷新
    private boolean isDOWNLOAD_REFRESH=false;//是否要进行下载更新
    private List<Video> videos=new ArrayList<>();
   @SuppressLint("HandlerLeak")
   private Handler mHander=new Handler(){
       @Override
       public void handleMessage(Message msg) {
          switch (msg.what){
              case REFRESH:
                  videoApaper.notifyDataSetChanged();
                  oneToast.showMessage(getContext(),"刷新成功");
                  localVideoRefresh.setRefreshing(false);
                  break;
              case DOWN_REFRESH:
                  Log.i("dsfsadf", "handleMessage: "+videoApaper.getCount());
                 // videoListView.setAdapter(videoApaper);
                  videoApaper.notifyDataSetChanged();
                  if(videos.size()>0){
                      sd_card_tip.setVisibility(View.GONE);
                  }else{
                      sd_card_tip.setVisibility(View.VISIBLE);
                  }
                  isDOWNLOAD_REFRESH=false;

                  loadingLayout.setVisibility(View.GONE);
              break;
              case DELETE_UPDATE:
                  Bundle bundle=msg.getData();
                 int position= (int) bundle.getSerializable("position");
                 String path= (String) bundle.getSerializable("path");
                  List<Video> videoList1= videoApaper.getVideoList();
                  if(position>=videoList1.size()||position<0)
                  {
                      return;
                  }
                  if(videoList1.get(position).getVideoPath().equals(path)){
                      videoList1.remove(position);
                      videoApaper.setVideoList(videoList1);
                      videoApaper.notifyDataSetChanged();
                      if(videoList1.size()>0){
                          sd_card_tip.setVisibility(View.GONE);
                      }else{
                          sd_card_tip.setVisibility(View.VISIBLE);
                      }
                  }
                  break;
              case HIDE_TIP:
                  sd_card_tip.setVisibility(View.GONE);
                  break;
              case SHOW_TIP:
                  sd_card_tip.setVisibility(View.VISIBLE);
                  break;
              case HIDE_TIP_SHOW_LOADING:
                  sd_card_tip.setVisibility(View.GONE);
                  loadingLayout.setVisibility(View.VISIBLE);
                  break;
              case HIDE_LOADING:
                  Bundle bundle1=msg.getData();
                  int result= (int) bundle1.getSerializable("result");
                  if(result==0){
                  sd_card_tip.setVisibility(View.VISIBLE);
                  }else if(result==1){
                      sd_card_tip.setVisibility(View.GONE);
                  }
                  loadingLayout.setVisibility(View.GONE);
                  localVideoRefresh.setRefreshing(false);
                  break;
              case CLICK_REGRESH:
                  if(videoApaper==null){
                      videoApaper = new VideoAdapter(videos,getContext());
                      videoListView.setAdapter(videoApaper);
                  }
                  else{
                      videoApaper.setVideoList(videos);
                      videoApaper.notifyDataSetChanged();
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

          }
       }
   };

    private TextView sd_card_tip;
    private LinearLayout loadingLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.sd_card_fragment,container,false);
        initView(view);
//         List<Video> videoList=FileManger.getInstance(container.getContext()).getVideos();
//        videoApaper = new VideoAdapter(videoList,container.getContext());
//        videoListView.setAdapter(videoApaper);
//        if(videoList.size()>0){
//            sd_card_tip.setVisibility(View.GONE);
//        }else{
//            sd_card_tip.setVisibility(View.VISIBLE);
//        }
//        localVideoRefresh.setRefreshing(false);
        initOnclickListener();//设置点击监听事件
        sd_card_tip.callOnClick();
        return  view;
    }


    private void initOnclickListener() {
        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                List<Video> videoList= videoApaper.getVideoList();
                PlayActivity.setVideoList(videoList);
                Intent intent=new Intent(getActivity(), PlayActivity.class);
                intent.putExtra("position",i);
                startActivity(intent);
               // oneToast.showMessage(getActivity(),videoList.get(i).getVideoPath());

            }
        });
        localVideoRefresh.setProgressBackgroundColorSchemeResource(R.color.RefreshProgressBackground);
        //设置颜色
        localVideoRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,android.R.color.holo_green_light,
                android.R.color.holo_orange_light,android.R.color.holo_red_light);
        localVideoRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<Video> videoList= FileManger.getInstance(getContext()).getVideos();
                    videoApaper.setVideoList(videoList);
                    mHander.sendEmptyMessage(REFRESH);

                    if(videoList.size()>0){
                    mHander.sendEmptyMessage(HIDE_TIP);
                    }else{
                        mHander.sendEmptyMessage(SHOW_TIP);
                    }
                }
            }).start();


            }
        });
//       点击提示信息刷新
        sd_card_tip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  new Thread(new Runnable() {
                      @Override
                      public void run() {
                          synchronized (this){
                              mHander.sendEmptyMessage(HIDE_TIP_SHOW_LOADING);
                              List<Video> videoList=FileManger.getInstance(getContext()).getVideos();
                              videos=videoList;
                              mHander.sendEmptyMessage(CLICK_REGRESH);
                          }
                      }
                  }).start();

            }
        });
    }

    private void initView(View view) {
        videoListView = (ListView)view.findViewById(R.id.videoListView);
        localVideoRefresh = (SwipeRefreshLayout)view.findViewById(R.id.localVideoRefresh);
        localVideoRefresh.setRefreshing(true);
        sd_card_tip = view.findViewById(R.id.sd_card_tip);
        loadingLayout = view.findViewById(R.id.loading_layout);
    }

    //删除一个不存在的视频更新视图
   public void deleteOneVideoUpateView(int position, String path){
      Message message=new Message();
      Bundle bundle=new Bundle();
      bundle.putSerializable("position",position);
       bundle.putSerializable("path",path);
       message.what=DELETE_UPDATE;
       mHander.sendMessage(message);
   }


 //下载视频后更新视图
   public void downLoadVideoUpdateView() {
       isDOWNLOAD_REFRESH = true;
   }

   //下载后更新视图
    public    void UpdateView(){
        if(isDOWNLOAD_REFRESH){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (this){
                        mHander.sendEmptyMessage(HIDE_TIP_SHOW_LOADING);
                        videos=FileManger.getInstance(getContext()).getVideos();
                        Log.i("dsfsadf", "run: "+videos.size());
                        videoApaper.setVideoList(videos);
                        mHander.sendEmptyMessage(DOWN_REFRESH);
                    }
                }
            }).start();

        }
    }
}
