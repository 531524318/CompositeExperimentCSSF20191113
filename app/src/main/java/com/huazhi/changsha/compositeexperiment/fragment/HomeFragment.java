package com.huazhi.changsha.compositeexperiment.fragment;

import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.huazhi.changsha.compositeexperiment.R;
import com.huazhi.changsha.compositeexperiment.activity.camera.CameraActivity;
import com.huazhi.changsha.compositeexperiment.activity.environment.EnvironmentMonitorMainActivity;
import com.huazhi.changsha.compositeexperiment.activity.fan.FanMainActivity;
import com.huazhi.changsha.compositeexperiment.activity.fence.FenceMainActivity;
import com.huazhi.changsha.compositeexperiment.activity.flame.FlameMainActivity;
import com.huazhi.changsha.compositeexperiment.activity.gas.GasMainActivity;
import com.huazhi.changsha.compositeexperiment.activity.gatelock.GateLockMainActivity;
import com.huazhi.changsha.compositeexperiment.activity.infrared.InfraredRemoteMainActivity;
import com.huazhi.changsha.compositeexperiment.activity.lighting.LightingMainActivity;
import com.huazhi.changsha.compositeexperiment.activity.pulsectrllighting.PulseCtrlLightingMainActivity;
import com.huazhi.changsha.compositeexperiment.activity.switchctrl.SwitchCtrlMainActivity;
import com.huazhi.changsha.compositeexperiment.model.EventMsgModel;
import com.huazhi.changsha.compositeexperiment.utils.GlobalDefs;
import com.huazhi.changsha.compositeexperiment.utils.LogUtils;
import com.huazhi.changsha.compositeexperiment.utils.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 8个综合实验系统
 **/
public class HomeFragment extends BaseFragment {
    @BindView(R.id.btn_head_back)
    Button btnBack;
    @BindView(R.id.tv_head_back_name)
    TextView tvBackName;
    @BindView(R.id.tv_head_title)
    TextView tvTitle;
    @BindView(R.id.btn_head_cmd)
    Button btnCmd;
    Unbinder unbinder;
    private View view;

    private SimpleAdapter simpleAdapter;
    // Socket变量
    public Socket socket = null;
    // 线程池
    // 为了方便展示,此处直接采用线程池进行线程管理,而没有一个个开线程
    public ExecutorService mThreadPool = null;

    /**
     * 接收服务器消息 变量
     */
    // 输入流对象
    InputStream isStream;
    // 输入流读取器对象
    BufferedReader br1;
    /**
     * 发送消息到服务器 变量
     */
    // 输出流对象
    public OutputStream outputStream;

    Handler mHandler = new Handler();
    Runnable r = new Runnable() {
        @Override
        public void run() {
            //执行
            LogUtils.i("检查socket 连接状态 start...");
            try {
                LogUtils.i("检查socket 连接状态 socket.isConnected()="+socket.isConnected());

                sendMsg("HAppBreakT");

                if(socket.isConnected()){
                    //连接
                    Util.setSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IS_CONN_SOCKET, true);

                }
                else{
                    Util.setSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IS_CONN_SOCKET, false);
                }
            }catch (Exception e){

            }

            //检查socket连接 状态
            mHandler.postDelayed(r, 5000);
        }
    };

    @Override
    public int getResource() {
        return R.layout.fragment_home;
    }

    @Override
    public void init(final View view) {
        this.view = view;
        unbinder = ButterKnife.bind(this, view);
        //初始化视图
        initView();
        // 初始化线程池

        LogUtils.i("Executors.newCachedThreadPool()");
        mThreadPool = Executors.newCachedThreadPool();
        //注册
        EventBus.getDefault().register(this);

        //检查socket连接 状态
        mHandler.postDelayed(r, 3000);
    }

    /**
     * 初始化视图
     **/
    private void initView() {
        tvTitle.setText(this.view.getContext().getString(R.string.app_name));
        btnBack.setVisibility(View.INVISIBLE);
        tvBackName.setVisibility(View.INVISIBLE);
        btnCmd.setVisibility(View.GONE);
    }


    @Override
    public void loadingDatas() {

    }

    /**
     * 家庭环境监测系统
     **/
    @OnClick(R.id.composite_experiment_rl_environmental_monitor)
    public void onViewEnvClicked(View view) {
        LogUtils.i("===========家庭环境监测系统============");
        Intent intent = new Intent(getContext(), EnvironmentMonitorMainActivity.class);
        startActivity(intent);
    }

    /**
     * 家庭照明系统
     **/
    @OnClick(R.id.composite_experiment_rl_lighting_system)
    public void onViewLightingClicked(View view) {
        LogUtils.i("===========家庭照明系统============");
        Intent intent = new Intent(getContext(), LightingMainActivity.class);
        startActivity(intent);
    }

    /**
     * 家庭电器设备控制
     **/
    @OnClick(R.id.composite_experiment_rl_switch_control)
    public void onViewSwitchClicked(View view) {
        LogUtils.i("===========家庭电器设备控制============");
        Intent intent = new Intent(getContext(), SwitchCtrlMainActivity.class);
        startActivity(intent);
    }

    /**
     * 家庭红外遥控电器控制
     **/
    @OnClick(R.id.composite_experiment_rl_infrared_remote_control)
    public void onViewInfraredClicked(View view) {
        LogUtils.i("===========家庭红外遥控电器控制============");
        Intent intent = new Intent(getContext(), InfraredRemoteMainActivity.class);
        startActivity(intent);
    }

    /**
     * 家庭安防报警系统
     **/
    @OnClick(R.id.composite_experiment_rl_fence)
    public void onViewFenceClicked(View view) {
        LogUtils.i("===========家庭安防报警系统============");
        Intent intent = new Intent(getContext(), FenceMainActivity.class);
        startActivity(intent);
    }

    /**
     * 家庭火灾报警系统
     **/
    @OnClick(R.id.composite_experiment_rl_flame)
    public void onViewFlameClicked(View view) {
        LogUtils.i("===========家庭火灾报警系统============");
        Intent intent = new Intent(getContext(), FlameMainActivity.class);
        startActivity(intent);
    }

    /**
     * 家庭可燃气体泄漏报警系统
     **/
    @OnClick(R.id.composite_experiment_rl_gas)
    public void onViewGasClicked(View view) {
        LogUtils.i("===========家庭可燃气体泄漏报警系统============");
        Intent intent = new Intent(getContext(), GasMainActivity.class);
        startActivity(intent);
    }

    /**
     * 家庭门锁控制系统
     **/
    @OnClick(R.id.composite_experiment_rl_gate_lock)
    public void onViewGateClicked(View view) {
        LogUtils.i("===========家庭门锁控制系统============");
        Intent intent = new Intent(getContext(), GateLockMainActivity.class);
        startActivity(intent);
    }

    /**
     * 家庭可视门禁系统
     **/
    @OnClick(R.id.composite_experiment_rl_camera)
    public void onViewCameraClicked(View view) {
        LogUtils.i("===========摄像头控制系统============");
        Intent intent = new Intent(getContext(), CameraActivity.class);
        startActivity(intent);
    }
    /**
     * 家庭风扇控制系统
     **/
    @OnClick(R.id.composite_experiment_rl_fan)
    public void onViewFanClicked(View view) {
        LogUtils.i("===========家庭风扇控制系统============");
        Intent intent = new Intent(getContext(), FanMainActivity.class);
        startActivity(intent);
    }
    /**
     * 家庭红外遥控电器控制
     **/
    @OnClick(R.id.composite_experiment_rl_pulsectrllighting)
    public void onViewPulsectrllightingClicked(View view) {
        LogUtils.i("===========脉冲信号控制可调灯============");
        Intent intent = new Intent(getContext(), PulseCtrlLightingMainActivity.class);
        startActivity(intent);
    }


    /**
     * evenbus事件处理
     **/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMsgModel event) {
        //连接socket等
        if (event.getMsg().endsWith(GlobalDefs.KEY_UPDATE_SOCKET)) {
            closeSocket(); //先关闭
            connectSocket();//连接socket

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    recvSocketData();//接收socket数据
                    LogUtils.i("============recvSocketData END");
                }
            };

            Timer timer = new Timer();
            timer.schedule(task, 8 * 1000);
        }
        //socket 各功能模块发送数据到a53
        else if (event.getMsg().endsWith(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53)) {
            String reciveData = event.getParam();
            sendMsg(reciveData);
        }
        //socket 接收到的数据
        else if (event.getMsg().endsWith(GlobalDefs.KEY_EVEN_BUS_SOCKET_RECIVE_DATA)) {
            String reciveData = event.getParam();
            //LogUtils.i("socket 接收到的数据"+reciveData);
            //转发到各功能模块中
            EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_RECIVE_DATA, reciveData));
        }
    }

    //============================socket start===================================================

    /**
     * 连接socket
     ***/
    public void connectSocket() {
        LogUtils.i("-------------连接socket connectSocket start...");

        // 利用线程池直接开启一个线程 & 执行该线程
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String address = (String) Util.getSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IP_ADDRESS, String.class);
                    String port = (String) Util.getSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_PORT, String.class);

                    if (TextUtils.isEmpty(address)) {
                        address = GlobalDefs.DEFAULT_IP;
                    }

                    if (TextUtils.isEmpty(port)) {
                        port = GlobalDefs.DEFAULT_PORT;
                    }

                    // 创建Socket对象 & 指定服务端的IP 及 端口号
                    socket = new Socket(address, Integer.parseInt(port));
                    // 判断客户端和服务器是否连接成功
                    LogUtils.i("socket.isConnected()=" + socket.isConnected());

                    //如果连接
                    if (socket.isConnected()) {
                        //已连接
                        Util.setSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IS_CONN_SOCKET, true);
                        Util.setSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_CONN_SOCKET_SUCCESS, true);
                    } else {
                        //连接
                        Util.setSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IS_CONN_SOCKET, false);
                    }
                } catch (IOException e) {
                    Util.setSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IS_CONN_SOCKET, false);
                    e.printStackTrace();
                }
            }
        });
    }

    /***
     *
     * 接受socket数据
     *
     * **/
    public void recvSocketData() {
        // 利用线程池直接开启一个线程 & 执行该线程
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 步骤1：创建输入流对象InputStream
                    while (true) {
                        if (socket != null) {
                            if (socket.isConnected()) {
                                // 步骤1：创建输入流对象InputStream
                                /*try {
                                    isStream = socket.getInputStream();
                                }catch (Exception e){
                                    LogUtils.i(e.toString());
                                }*/

                                // 步骤3：接收服务器发送过来的数据
                                byte[] b = new byte[150];
                                DataInputStream input = null;
                                int length = 0;

                                try{
                                    // 步骤2：创建输入流读取器对象 并传入输入流对象
                                    InputStream inputStream = socket.getInputStream();
                                    input = new DataInputStream(inputStream);
                                    length = input.read(b);
                                }catch (IOException e){
                                    LogUtils.i(e.toString());
                                    Util.setSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IS_CONN_SOCKET, false);
                                    return;//异常退出while循环！！！
                                }

                                String recvData = "";
                                try {
                                    recvData = new String(b, 0, length, "GBK");
                                } catch (Exception e) {

                                }
                                //LogUtils.i("recvData="+recvData);

                                if (recvData.startsWith("H") && recvData.endsWith("T")) {
                                    String[] tempStrArray = recvData.split("T");
                                    // LogUtils.i("=========tempStrArray.length="+tempStrArray.length);
                                    int tempLength = tempStrArray.length;
                                    for (int i = 0; i < tempLength; i++) {
                                        String tempData = tempStrArray[i];
                                        if (!tempData.endsWith("T")) {
                                            tempData = tempData + "T";
                                        }
                                        //LogUtils.i("------------tempData=" + tempData);

                                        //发送socket接收到的数据
                                        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_SOCKET_RECIVE_DATA, tempData));
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LogUtils.i("++++++IOException++++++" + e.toString());
                    //未连接
                    Util.setSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IS_CONN_SOCKET, false);
                    e.printStackTrace();
                }
            }
        });
    }


    /***
     *
     * 关闭socket
     *
     * **/
    public void closeSocket() {
        LogUtils.i("-------------关闭socket closeSocket start...");

        try {
            // 断开 客户端发送到服务器 的连接，即关闭输出流对象OutputStream
            if (outputStream != null) {
                outputStream.close();
            }

            if (isStream != null) {
                isStream.close();
            }

            // 断开 服务器发送到客户端 的连接，即关闭输入流读取器对象BufferedReader
            //br.close();

            // 最终关闭整个Socket连接
            if (socket != null) {
                socket.close();
            }

            LogUtils.i("-------------关闭socket closeSocket end");
            // 判断客户端和服务器是否已经断开连接
            //LogUtils.i("socket.isConnected()=" + socket.isConnected());
        } catch (IOException e) {
            Util.setSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IS_CONN_SOCKET, false);
            e.printStackTrace();
        }
    }

    /***
     *
     *  发送消息
     *
     * ***/
    public void sendMsg(final String msg) {
        LogUtils.i("sendMsg 发送消息 msg =" + msg);

        // 利用线程池直接开启一个线程 & 执行该线程
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                LogUtils.i("sendMsg 发送消息 socket =" + socket);
                if (socket != null) {
                    try {
                        // 步骤1：从Socket 获得输出流对象OutputStream
                        // 该对象作用：发送数据
                        outputStream = socket.getOutputStream();
                        // 步骤2：写入需要发送的数据到输出流对象中
                        outputStream.write(msg.getBytes("utf-8"));
                        // 步骤3：发送数据到服务端
                        outputStream.flush();
                    } catch (IOException e) {
                        Util.setSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IS_CONN_SOCKET, false);
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    //============================socket end===================================================


    @Override
    public void startdestroy() {
        unbinder.unbind();

        LogUtils.i("startdestroy start...");
        //unregister
        EventBus.getDefault().unregister(this);
        //关闭socket
        closeSocket();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
