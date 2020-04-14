package com.huazhi.changsha.compositeexperiment.activity.pulsectrllighting;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.huazhi.changsha.compositeexperiment.R;
import com.huazhi.changsha.compositeexperiment.app.ExitApplication;
import com.huazhi.changsha.compositeexperiment.device.SensorCmd;
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
import java.text.MessageFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/***
 *
 * 脉冲信号控制可调灯
 *
 * **/
public class PulseCtrlLightingMainActivity extends AppCompatActivity {
    @BindView(R.id.btn_head_back)
    Button btnBack;
    @BindView(R.id.tv_head_back_name)
    TextView tvBackName;
    @BindView(R.id.tv_head_title)
    TextView tvTitle;
    @BindView(R.id.btn_head_cmd)
    Button btnCmd;

    @BindView(R.id.light_angle)
    TextView tvLightAngle;

    @BindView(R.id.light_control)
    SeekBar sbLightCtrl;


    Unbinder unbinder;
    private View view;
    //操作相关
    // 主线程Handler
    // 用于将从服务器获取的消息显示出来
    private Handler mMainHandler;
    // Socket变量
    private Socket socket = null;
    // 线程池
    // 为了方便展示,此处直接采用线程池进行线程管理,而没有一个个开线程
    private ExecutorService mThreadPool;
    private String now_angle = "000";
    private Timer timerlight;
    private Context context;

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
    OutputStream outputStream;

    private final int AL_MSG_CODE = 2;//可调节灯传感器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulsectrllighting_main);
        ButterKnife.bind(this);
        context = this;
        //初始化视图
        initView();
        //注册
        EventBus.getDefault().register(this);
        ExitApplication.getInstance().addActivity(this);
        // 实例化主线程,用于更新接收过来的消息
        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case AL_MSG_CODE:
                        //更新可调节灯
                        updateAlValue((String) msg.obj);
                        break;
                    case 0x117: //延迟一秒，可调灯再次生效
                        SeekBar bar = (SeekBar) msg.obj;
                        bar.setEnabled(true);
                        break;
                }
            }
        };
    }

    /**
     * 初始化视图
     **/
    private void initView() {
        tvTitle.setText(context.getString(R.string.composite_experiment_pulsectrllighting));
        btnCmd.setVisibility(View.GONE);

        //避免SeekBar滑块被遮盖
        sbLightCtrl.setPadding(0, 0, 0, 0);
        sbLightCtrl.setThumbOffset(0);
        sbLightCtrl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvLightAngle.setText("亮度:" + progress + "%");
                String angle = "100";
                if ((progress + "").length() == 1) {
                    angle = "00" + progress;
                } else if ((progress + "").length() == 2) {
                    angle = "0" + progress;
                }
                now_angle = angle;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //发送控制命令
                LogUtils.i("发送控制命令 now_angle=" + now_angle);
                TimerTask tasklight = new TimerTask() {
                    @Override
                    public void run() {
                        String orderlight = MessageFormat.format(SensorCmd.SENSOR_PLC_SEND_CMD, now_angle);
                        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, orderlight));
                    }
                };
                timerlight = new Timer();
                timerlight.schedule(tasklight, 0, 500);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //结束控制命令
                LogUtils.i("结束控制命令 now_angle=" + now_angle);
                String orderlight = MessageFormat.format(SensorCmd.SENSOR_PLC_SEND_END_CMD, now_angle);
                EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, orderlight));
                if (timerlight != null) {
                    timerlight.cancel();
                    timerlight = null;
                }
            }
        });
    }

    /**
     * 可调节灯关闭
     **/
    private void closeLight() {
        String orderlight = SensorCmd.SENSOR_PLC_CLOSE_CMD;
        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, orderlight));
    }

    /**
     * 更新可调节灯数据  Hpdal0103100T
     **/
    private void updateAlValue(String data) {
        try {
            String alData = data.substring(9, 12);
            Float alValue = Float.parseFloat(alData);
            int al = (int) Math.floor(alValue);//取整数
            now_angle = al + "";
            tvLightAngle.setText(al + "%");
            sbLightCtrl.setProgress(Integer.valueOf(now_angle));
        } catch (Exception e) {
        }
    }
    /**
     * 返回
     **/
    @OnClick(R.id.btn_head_back)
    public void clickBackBtn(View view) {
        //发送停止
        sendStop();
        this.finish();
    }
    /***
     *
     * 发送停止
     *
     * **/
    private void sendStop() {
        //设备编号
        String deviceID = (String) Util.getSP(this, GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_DEVICE_ID, String.class);
        //ToastUtils.showToast(view.getContext(),"sendStop 设备编号="+deviceNo);
        LogUtils.i("sendStop 设备编号=" + deviceID);

        if (!TextUtils.isEmpty(deviceID)) {
            //停止命令
            String orderlightStop = "Hpcal" + deviceID + "08stopctrlT";
            //发送数据到a53
            EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, orderlightStop));
        }
    }

    /**
     * 连接socket
     ***/
    private void connectSocket() {
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
    private void recvSocketData() {
        // 利用线程池直接开启一个线程 & 执行该线程
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    // 步骤1：创建输入流对象InputStream
                    while (true) {
                        if (socket != null) {
                            // 步骤1：创建输入流对象InputStream
                            isStream = socket.getInputStream();

                            // 步骤2：创建输入流读取器对象 并传入输入流对象
                            InputStream inputStream = socket.getInputStream();
                            DataInputStream input = new DataInputStream(inputStream);

                            // 步骤3：接收服务器发送过来的数据
                            byte[] b = new byte[150];
                            int length = input.read(b);
                            String recvData = "";
                            try {
                                recvData = new String(b, 0, length, "utf-8");
                            } catch (Exception e) {

                            }
                            //LogUtils.i("recvData="+recvData);

                            if (recvData.startsWith("H") && recvData.endsWith("T")) {
                                String[] tempStrArray = recvData.split("T");
                                // LogUtils.i("=========tempStrArray.length="+tempStrArray.length);

                                if (recvData.startsWith("Hp") && recvData.substring(3, 5).equals("al") && recvData.endsWith("T")) {
                                    int tempLength = tempStrArray.length;

                                    for (int i = 0; i < tempLength; i++) {

                                        String tempData = tempStrArray[i];
                                        if (!tempData.endsWith("T")) {
                                            tempData = tempData + "T";
                                        }
                                        //调节灯消息
                                        Message message = new Message();
                                        message.what = AL_MSG_CODE;
                                        message.obj = tempData;
                                        mMainHandler.sendMessage(message);
                                    }
                                }

                            }
                        }
                    }
                } catch (IOException e) {
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
    private void closeSocket() {
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

            // 判断客户端和服务器是否已经断开连接
            //LogUtils.i("socket.isConnected()=" + socket.isConnected());
        } catch (IOException e) {
            Util.setSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IS_CONN_SOCKET, false);
            e.printStackTrace();
        }
    }

    /**
     * evenbus事件处理
     **/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMsgModel event) {
        if (event.getMsg().endsWith(GlobalDefs.KEY_UPDATE_SOCKET)) {
            closeSocket(); //先关闭
            connectSocket();//连接socket
            recvSocketData();//接收socket数据
        }
        //发可调节灯停止命令_按退出按钮
        else if (event.getMsg().endsWith(GlobalDefs.KEY_AL_EXIT_STOP_CMD)) {
            //发送停止
            LogUtils.i("按手机上的多任务按键 Home按键 返回按键 执行关闭命令 start...");
            //可调节灯关闭
            closeLight();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.i("每次进入都执行 onResume start...");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.i("每次进入都执行 onResume start...");
    }
}
