package io.vov.vitamio.demo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import io.vov.vitamio.toast.oneToast;

public class NetWorkChangeReceiver extends BroadcastReceiver {
     private NetWorkChange netWorkChange;
     private final  static String TAG="movie2";
    public void setNetWorkChange(NetWorkChange netWorkChange) {
        this.netWorkChange = netWorkChange;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        ConnectivityManager connectivityManager= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null&&networkInfo.isAvailable()){//当前网络可用
            boolean isWifiConnected= connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
            boolean isGPRSConnected=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState()==NetworkInfo.State.CONNECTED;
            Log.i("movie3", "WIFI是否连接："+isWifiConnected+"\nGRPS是否连接："+isGPRSConnected);
            if(netWorkChange!=null){
                if(isWifiConnected){//如果连接了WIFI
                    netWorkChange.netWIFIAvaiable();
                }else if(isGPRSConnected){//如果连接了GPRS
                    netWorkChange.netGPRSAvaiable();
                }
            }
        }else{
            if(netWorkChange!=null){
                netWorkChange.netUnAvailable();
            }
        }

    }
    public interface NetWorkChange{

        void netUnAvailable();//网络不可用
        void netWIFIAvaiable();//WiFi可用
        void netGPRSAvaiable();//GPRS可用
    }

}
