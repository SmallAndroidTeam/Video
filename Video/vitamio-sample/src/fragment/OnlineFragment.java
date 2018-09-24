package fragment;

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

import com.google.gson.Gson;


import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import activity.PlayActivity;
import adapter.OnlineVideoAdapter;
import domain.NetMediaItem;
import io.vov.vitamio.demo.R;
import io.vov.vitamio.toast.oneToast;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import utils.Constants;

/**
 * Created by MR.XIE on 2018/9/7.
 */
public class OnlineFragment extends Fragment {
    //请求的数据
    private List<NetMediaItem.TrailersBean> mTrailersBean=new ArrayList<>();
    private List<io.vov.vitamio.bean.Video> videoList;
    private OnlineVideoAdapter onlineVideoAdapter;
    private ListView onlineVideoListView;
    private final String TAG="movie";
    public static final int LOADDATA=0;
    public static final int LOADMORE=1;
    public static final int REFRESH=2;
    private static final String footViewMessageOne="上拉加载更多...";
    private static final String footViewMessageTwo="正在加载更多数据...";
    private boolean isPerson=true;//是否为人为滑动
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case LOADDATA:
                    //如果有数据
                    if (mTrailersBean != null && mTrailersBean.size() > 0) {
                        onlineVideoAdapter=new OnlineVideoAdapter(mTrailersBean,getContext());
                        if(onlineVideoListView.getFooterViewsCount()==0){
                            onlineVideoListView.addFooterView(footView);
                            onlineVideoListView.requestFocus();
                        }
                        onlineVideoListView.setAdapter(onlineVideoAdapter);
                    }
                    break;
                case LOADMORE:
                    load_text.setText(footViewMessageTwo);
                    mTrailersBean.addAll(mTrailersBean);
                    onlineVideoAdapter.setmTrailersBean(mTrailersBean);
                    onlineVideoAdapter.notifyDataSetChanged();
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
                            videoList.add(video);
                        }

                    }
                    isPerson=true;
                    break;
                case REFRESH:
                     onlineVideoAdapter.setmTrailersBean(mTrailersBean);
                    onlineVideoAdapter.notifyDataSetChanged();
                    onloneSwipeRefreshLayout.setRefreshing(false);//设置刷新隐藏
                    oneToast.showMessage(getContext(),"刷新成功");
                    break;
            }

        }
    };
    private SwipeRefreshLayout onloneSwipeRefreshLayout;
    private View footView;
    private TextView load_text;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.online_fragment,container,false);
    initView(view);
     initOnlineVideo();
        return  view;
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
                videoList.add(video);
            }
        }
    }
    private void addListener() {
        mTrailersBeanChangeVideoList();
        onlineVideoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlayActivity.setVideoList(videoList);
                Intent intent=new Intent(getActivity(), PlayActivity.class);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
        onloneSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
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
        onlineVideoListView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
             boolean result=isListViewReachBottomEdge(onlineVideoListView);
             if(result&&isPerson){
                 load_text.setText(footViewMessageOne);
                 mHandler.sendEmptyMessage(LOADMORE);
                 isPerson=false;
             }
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                getDataFromNet();
                addListener();
                onloneSwipeRefreshLayout.setRefreshing(false);
            }
        }).start();
    }

    private void initView(View view) {
        onlineVideoListView = (ListView)view.findViewById(R.id.onloneVideoListView);
        onloneSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.onloneSwipeRefreshLayout);

        onloneSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.RefreshProgressBackground);
        //设置颜色
        onloneSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,android.R.color.holo_green_light,
                android.R.color.holo_orange_light,android.R.color.holo_red_light);
        onloneSwipeRefreshLayout.setRefreshing(true);
        footView =LayoutInflater.from(getContext()).inflate(R.layout.listview_foot_load_view,null);
        load_text = footView.findViewById(R.id.load_text);
    }

    //从网络获取视频
    private void getDataFromNet(){
        OkHttpClient okHttpClient=new OkHttpClient();
        Request request=new Request.Builder().url(Constants.NET_URL).build();
        try {
            Response response=okHttpClient.newCall(request).execute();
        parseJson(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            oneToast.showMessage(getContext(),"网络错误");
        }

    }

    private void parseJson(String result){

        Gson gson=new Gson();
      NetMediaItem netMediaItem=gson.fromJson(result,NetMediaItem.class);
        mTrailersBean=netMediaItem.getTrailers();
    }
}
