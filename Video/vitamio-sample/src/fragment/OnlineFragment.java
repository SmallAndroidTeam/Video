package fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import activity.PlayActivity;
import adapter.OnlineVideoAdapter;
import bean.Video;
import domain.NetMediaItem;
import io.vov.vitamio.demo.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import toast.oneToast;
import utils.Constants;

/**
 * Created by MR.XIE on 2018/9/7.
 */
public class OnlineFragment extends Fragment {
    //请求的数据
    private List<NetMediaItem.TrailersBean> mTrailersBean=new ArrayList<>();
    private List<io.vov.vitamio.bean.Video> videoList;
    private OnlineVideoAdapter onlineVideoAdapter;
    private ListView onloneVideoListView;
    private final String TAG="movie";
    public static final int LOADDATA=0;
    public static final int LOADMORE=1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case LOADDATA:
                    //如果有数据
                    if (mTrailersBean != null && mTrailersBean.size() > 0) {
                        onlineVideoAdapter=new OnlineVideoAdapter(mTrailersBean,getContext());
                        onloneVideoListView.setAdapter(onlineVideoAdapter);
                    }
                    break;
                case LOADMORE:
                    //mTrailersBean.addAll(mTrailersBean);
                    onlineVideoAdapter.notifyDataSetChanged();
                    break;
            }

            //最终加载完成，进度条肯定要消失
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.online_fragment,container,false);
    initView(view);
     initOnlineVideo();
        return  view;
    }

    private void addListener() {
        if(mTrailersBean!=null&&mTrailersBean.size()>0){
            mHandler.sendEmptyMessage(LOADDATA);
            videoList=new ArrayList<>();
            for(NetMediaItem.TrailersBean netMediaItem:mTrailersBean){
                io.vov.vitamio.bean.Video video=new io.vov.vitamio.bean.Video();
                video.setVideoPath(netMediaItem.getHightUrl());
                video.setVideoName(netMediaItem.getMovieName());
                videoList.add(video);
            }
        }
        onloneVideoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlayActivity.setVideoList(videoList);
                Intent intent=new Intent(getActivity(), PlayActivity.class);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });

    }

    private void initOnlineVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getDataFromNet();
                addListener();
            }
        }).start();
    }

    private void initView(View view) {
        onloneVideoListView = (ListView)view.findViewById(R.id.onloneVideoListView);

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
