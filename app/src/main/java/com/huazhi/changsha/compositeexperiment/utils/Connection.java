package com.huazhi.changsha.compositeexperiment.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;

/**
 * 连接服务类,此处只是作为连接Web服务器
 */

public class Connection {

    public static String serverIP = "";

    public static String city = "桂林";

    public static boolean linkOrNot = false;        //是否连接上了web服务器

    public static String localIP = "";
    public static String wifissid = "";


    public static void link(final String address,final Handler mHandler){      //判断是否连接成功

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {//http://423eb680.ngrok.io/
                    String path;
                    if(!address.endsWith("io"))
                    {
                        path = "http://"+address+":8080/EnvironmentMonitorServer-war/getconnection";
                    }else{
                        path = "http://"+address+"/EnvironmentMonitorServer-war/getconnection";
                    }
                    URL url = new URL(path);        //声明访问的路径， url 网络资源 http ftp rtsp
                    conn = (HttpURLConnection) url.openConnection();      //通过路径得到一个连接 http的连接
                    conn.setConnectTimeout(1000);
                    conn.setReadTimeout(1000);
                    conn.setRequestMethod("GET");       // 设置为GET方法
                    int code = conn.getResponseCode();
                    InputStream in =  conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line = reader.readLine().trim();
                 /*这里先注释掉先，因为还不知道什么情况，直接默认服务器IP是可以使用的，将来有一日再来改过*/
//                    String line = "success";
                    if("success".equals(line)){
                        Connection.linkOrNot = true;
                        mHandler.sendEmptyMessageDelayed(1,2000);//成功连接的，发送延迟几秒，等所有失败的线程结束
                    }else{
                        Connection.linkOrNot = false;
                        mHandler.sendEmptyMessage(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(0);
                }finally{
                    if(conn != null){
                        conn.disconnect();
                    }
                }
            }
        }).start();
    }

    public static void requestSend(final Handler handler, final String query){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    String realizeQuery;
                    if(query.startsWith("http"))    //为了兼容以前的版本
                    {
                        realizeQuery = query;
                    }else{
                        realizeQuery = "http://"+Connection.serverIP+":8080/EnvironmentMonitorServer-war/"+query;
                    }
                    URL url = new URL(realizeQuery);        //声明访问的路径， url 网络资源 http ftp rtsp
                    conn = (HttpURLConnection) url.openConnection();      //通过路径得到一个连接 http的连接
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(8000);
                    conn.setRequestMethod("GET");       // 设置为GET方法
                    InputStream in =  conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = reader.readLine())!=null){
                         response.append(line);
                    }
                    Message msg = new Message();
                    msg.obj = response.toString();
                    handler.sendMessage(msg);

                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();        //未连接时的处理
                    msg.obj = "NOACCESS";
                    handler.sendMessage(msg);
                }finally{
                    if(conn != null){
                        conn.disconnect();
                    }
                }
            }
        }).start();
    }

    //本地WiFi网络状态
    public void wifiStatus(View view){

        boolean isWifi = false;
        ConnectivityManager mConnectivity = (ConnectivityManager) view.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        if(info!=null)//有网络
        {
            if(info.getType()==ConnectivityManager.TYPE_WIFI){
                Log.d("hello", "wifi在线");
                isWifi = true;
            }else if(info.getType()==ConnectivityManager.TYPE_MOBILE){
                Log.d("hello", "移动网络");
                isWifi = true;
                return;
            }
        }else{
            localIP = "...";
            wifissid = "...";
            Log.d("hello", "没有网络");
        }

        if(!isWifi)return;                  //如果不是wifi，直接返回

        WifiManager wifiMan = (WifiManager) view.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo infowifi = wifiMan.getConnectionInfo();
        int ipAddress = infowifi.getIpAddress();
        String ssid = infowifi.getSSID();   // 获得本机所链接的WIFI名称
        String ipString = "";               // 本机在WIFI状态下路由分配给的IP地址.

        if (ipAddress != 0){
            ipString = ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                    + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
            localIP = ipString;

            if (!TextUtils.isEmpty(ssid))
            {
                String newSsid = ssid.replace("\"", "");
                wifissid = newSsid;
            }
            else {
                wifissid = ssid;
            }

        }
    }

    //发送socket数据包给远程主机
    public void sendStringSocket(String message,String goalIP){
        Socket socket = null;
        try{
            InetAddress remoteAddr = InetAddress.getByName(goalIP);//远程主机IP
            SocketAddress remoteAddress = new InetSocketAddress(remoteAddr,6002);//绑定好服务器端口
            socket = new Socket();
            socket.connect(remoteAddress,6000);//6秒超时连接

            OutputStream socketOut = socket.getOutputStream();//构建输出字节流
            PrintWriter pw = new PrintWriter(socketOut,true);      //构建输出字符流
            if(message!=null){
                Log.d("hello", "开始发送: "+message);
                pw.println(message);

            }
        }catch (IOException e ){
            e.printStackTrace();
        }finally {
            try{
                if (socket!=null)
                    socket.close();//只是切断与远程服务器的连接，而不是释放占用的端口
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
