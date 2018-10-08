package io.vov.vitamio.demo.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


import io.vov.vitamio.demo.R;
import io.vov.vitamio.demo.activity.PlayActivity;
import io.vov.vitamio.demo.adapter.FilmVideoAdapter;
import io.vov.vitamio.demo.broadcast.NetWorkChangeReceiver;
import io.vov.vitamio.demo.domain.NetMediaItem;
import io.vov.vitamio.demo.utils.Constants;
import io.vov.vitamio.toast.oneToast;
import io.vov.vitamio.utils.CommonUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by MR.XIE on 2018/9/24.
 */
public class FilmFragment extends Fragment implements NetWorkChangeReceiver.NetWorkChange {
    //请求的数据
    private List<NetMediaItem.TrailersBean> mTrailersBean=new ArrayList<>();
    private List<io.vov.vitamio.bean.Video> videoList;
    private FilmVideoAdapter filmVideoAdapter;
    private ListView onlineVideoListView;
    private final String TAG="movie2";
    public static final int LOADDATA=0;
    public static final int LOADMORE=1;
    public static final int REFRESH=2;
    public static final int NETAVAILABLE=3;
    public static final int NETUNAVAILABLE=4;
    public static final int NETEXCEPTION=5;
    private static final String footViewMessageOne="上拉加载更多...";
    private static final String footViewMessageTwo="正在加载更多数据...";
    private static final String footViewMessageThree="人家也是有底线的...";
    private boolean isPerson=true;//是否为人为滑动
    public final static String NET_CONNNECT_CHANGE="android.net.conn.CONNECTIVITY_CHANGE";
    private NetWorkChangeReceiver netWorkChangeReceiver;
    private boolean net_avaiable=false;//判断网络是否可用
    private SwipeRefreshLayout onlineSwipeRefreshLayout;
    private View footView;
    private TextView load_text;
    private LinearLayout net_unavailable_layout;
    private boolean isLoad=true;//是否加载
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case LOADDATA:
                    //如果有数据
                    if (mTrailersBean != null && mTrailersBean.size() > 0) {
                        filmVideoAdapter =new FilmVideoAdapter(mTrailersBean,getContext());

                        if(onlineVideoListView.getFooterViewsCount()==0){
                            onlineVideoListView.addFooterView(footView);
                        }

                        onlineVideoListView.setAdapter(filmVideoAdapter);
                        addOnlineVideoListViewListener();
                    }
                    break;
                case LOADMORE:
                    //无数据加载了
                     if(!isLoad){
                         load_progressbar.setVisibility(View.GONE);
                         load_text.setText(footViewMessageThree);
                         break;
                     }
                    load_text.setText(footViewMessageTwo);
                    mTrailersBean.addAll(mTrailersBean);
                    filmVideoAdapter.setmTrailersBean(mTrailersBean);
                    filmVideoAdapter.notifyDataSetChanged();
                    load_text.setText(footViewMessageOne);

                    if(mTrailersBean!=null&&mTrailersBean.size()>0){
                        if(videoList!=null)
                        {
                            videoList.clear();
                        }
                        else{
                            videoList=new ArrayList<>();
                        }
                        for(NetMediaItem.TrailersBean netMediaItem:mTrailersBean){
                            io.vov.vitamio.bean.Video video=new io.vov.vitamio.bean.Video();
                            video.setVideoPath(netMediaItem.getHightUrl());
                            video.setVideoName(netMediaItem.getMovieName());
                            video.setThumbnailPath(netMediaItem.getCoverImg());
                            videoList.add(video);
                        }
                    }
                    isPerson=true;
                    isLoad=false;
                    break;
                case REFRESH:
                     isLoad=true;
                    isPerson=true;
                    load_progressbar.setVisibility(View.VISIBLE);
                    load_text.setText(footViewMessageOne);
                    filmVideoAdapter.setmTrailersBean(mTrailersBean);
                    filmVideoAdapter.notifyDataSetChanged();
                    onlineVideoListView.setAdapter(filmVideoAdapter);
                    onlineSwipeRefreshLayout.setRefreshing(false);//设置刷新隐藏
                    oneToast.showMessage(getContext(),"刷新成功");
                    break;
                case NETAVAILABLE:
                    netAvailableShowView();
                    break;
                case NETUNAVAILABLE:
                    netUnAvailableShowView();
                    onlineSwipeRefreshLayout.setRefreshing(false);
                    break;
                case NETEXCEPTION:
                    oneToast.showMessage(getContext(),"网络错误");
                    break;

            }

        }
    };
    private ImageView net_unavailable_image;
    private TextView net_unavailable_tipText;
    private ProgressBar load_progressbar;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.film_fragment,container,false);
        //注册网络变化广播
        netWorkChangeReceiver = new NetWorkChangeReceiver();
        netWorkChangeReceiver.setNetWorkChange(this);
        net_avaiable= CommonUtils.net_avaiable(Objects.requireNonNull(this.getContext()));//获取当前的网络是否可用
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(NET_CONNNECT_CHANGE);
        Objects.requireNonNull(this.getContext()).registerReceiver(netWorkChangeReceiver,intentFilter);
        initView(view);
        initOnlineVideo();
      addListener();
        return  view;
    }
    @Override
    public void netAvailable() {
        net_avaiable=true;
       //oneToast.showMessage(this.getContext(),"当前网络可用");
    }

    @Override
    public void netUnAvailable() {
        net_avaiable=false;
        oneToast.showMessage(this.getContext(),"当前网络不可用");
       // netUnAvailableShowView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //注销网络变化广播
        Objects.requireNonNull(this.getContext()).unregisterReceiver(netWorkChangeReceiver);
    }

    //网络可用时需要显示的视图
    private  void netAvailableShowView(){
        net_unavailable_layout.setVisibility(View.GONE);
        onlineVideoListView.setVisibility(View.VISIBLE);
        onlineSwipeRefreshLayout.setRefreshing(false);
     //   initOnlineVideo();//连接网络之后自动刷新当前的在线视频
    }

    //网络不可用时需要显示的视图
    private  void netUnAvailableShowView(){
        onlineVideoListView.setVisibility(View.GONE);
        net_unavailable_layout.setVisibility(View.VISIBLE);
       onlineSwipeRefreshLayout.setRefreshing(false);
    }

    private void mTrailersBeanChangeVideoList(){

        if(mTrailersBean!=null&&mTrailersBean.size()>0){
            mHandler.sendEmptyMessage(LOADDATA);
            if(videoList!=null)
            {
                videoList.clear();
            }
            else{
                videoList=new ArrayList<>();
            }
            for(NetMediaItem.TrailersBean netMediaItem:mTrailersBean){
                io.vov.vitamio.bean.Video video=new io.vov.vitamio.bean.Video();
                video.setVideoPath(netMediaItem.getHightUrl());
                video.setVideoName(netMediaItem.getMovieName());
                video.setThumbnailPath(netMediaItem.getCoverImg());
                videoList.add(video);
            }
        }
    }

    private void addOnlineVideoListViewListener(){
        onlineVideoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!net_avaiable){//如果网络不可用
                    netUnAvailableShowView();
                    return;
                }else{
                    netAvailableShowView();
                }
                if(position==onlineVideoListView.getCount()-1){
                    return;
                }
                PlayActivity.setVideoList(videoList);
                Intent intent=new Intent(getActivity(), PlayActivity.class);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
        onlineVideoListView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                boolean result=isListViewReachBottomEdge(onlineVideoListView);
                if(result&&isPerson){
                    Log.i("movie2", "onScrollChange: 22");
                    load_text.setText(footViewMessageOne);
                    mHandler.sendEmptyMessage(LOADMORE);
                    isPerson=false;
                }

            }
        });
    }
    private void addListener() {

        onlineSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!net_avaiable){//如果网络不可用
                      netUnAvailableShowView();
                    onlineSwipeRefreshLayout.setRefreshing(false);
                    return;
                }else{
                    netAvailableShowView();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getDataFromNet();
                        mTrailersBeanChangeVideoList();
                        mHandler.sendEmptyMessage(REFRESH);
                    }
                }).start();
            }
        });


        net_unavailable_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initOnlineVideo();
            }
        });
        net_unavailable_tipText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initOnlineVideo();
            }
        });
    }
    //判断是否滑动顶部
    private boolean isListViewReachBottomEdge(final  ListView listView){
        boolean result=false;
        if(listView.getLastVisiblePosition()==listView.getCount()-1){
            final View videoBottom=listView.getChildAt(listView.getLastVisiblePosition()-listView.getFirstVisiblePosition());
            result=(listView.getHeight()>=videoBottom.getBottom());
        }
        return result;
    }

    private void initOnlineVideo() {

        if(!net_avaiable){//如果网络不可用
            netUnAvailableShowView();
            return;
        }else{
            netAvailableShowView();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                getDataFromNet();
                mTrailersBeanChangeVideoList();
            }
        }).start();
    }

    private void initView(View view) {
        onlineVideoListView = (ListView)view.findViewById(R.id.onloneVideoListView);
        onlineSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.onloneSwipeRefreshLayout);
        onlineSwipeRefreshLayout.setRefreshing(true);
        onlineSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.RefreshProgressBackground);
        //设置颜色
        onlineSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,android.R.color.holo_green_light,
                android.R.color.holo_orange_light,android.R.color.holo_red_light);
        footView = LayoutInflater.from(getContext()).inflate(R.layout.listview_foot_load_view,null);
        footView.setEnabled(false);
        footView.setClickable(false);
        load_text = footView.findViewById(R.id.load_text);
        load_text.setText(footViewMessageOne);
        load_progressbar = footView.findViewById(R.id.probar);

        net_unavailable_layout = (LinearLayout)view.findViewById(R.id.net_unavailable);
        net_unavailable_image = (ImageView)view.findViewById(R.id.net_unavailable_image);
        net_unavailable_tipText = (TextView)view.findViewById(R.id.net_unavailable_tipText);
    }

    //从网络获取视频
    private void getDataFromNet(){
        OkHttpClient okHttpClient=new OkHttpClient();
        Request request=new Request.Builder().url(Constants.NET_URL).build();
        try {
            if(!net_avaiable){//如果网络不可用
                mHandler.sendEmptyMessage(NETUNAVAILABLE);
                return;
            }else{
                mHandler.sendEmptyMessage(NETAVAILABLE);
                Response response=okHttpClient.newCall(request).execute();
                parseJson(response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(NETUNAVAILABLE);
mHandler.sendEmptyMessage(NETEXCEPTION);
        }

    }


    private void parseJson(String result){

        Gson gson=new Gson();
        NetMediaItem netMediaItem=gson.fromJson(result,NetMediaItem.class);
        mTrailersBean=netMediaItem.getTrailers();
    }


}
