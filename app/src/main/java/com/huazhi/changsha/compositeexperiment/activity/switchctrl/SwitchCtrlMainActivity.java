package com.huazhi.changsha.compositeexperiment.activity.switchctrl;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.huazhi.changsha.compositeexperiment.R;
import com.huazhi.changsha.compositeexperiment.app.ExitApplication;
import com.huazhi.changsha.compositeexperiment.device.SensorCmd;
import com.huazhi.changsha.compositeexperiment.device.SensorType;
import com.huazhi.changsha.compositeexperiment.model.EventMsgModel;
import com.huazhi.changsha.compositeexperiment.model.SensorDevice;
import com.huazhi.changsha.compositeexperiment.utils.GlobalDefs;
import com.huazhi.changsha.compositeexperiment.utils.LogUtils;
import com.huazhi.changsha.compositeexperiment.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.MessageFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 家庭电器设备控制_主页
 */
public class SwitchCtrlMainActivity extends Activity {
    @BindView(R.id.btn_head_back)
    Button btnBack;
    @BindView(R.id.tv_head_back_name)
    TextView tvBackName;
    @BindView(R.id.tv_head_title)
    TextView tvTitle;
    @BindView(R.id.btn_head_cmd)
    Button btnCmd;
    @BindView(R.id.tv_relay)
    TextView tvRelay;
    @BindView(R.id.sw_relay)
    Switch swRelay;
    @BindView(R.id.tv_fan)
    TextView tvFan;
    @BindView(R.id.sw_fan)
    Switch swFan;
    @BindView(R.id.tv_lock)
    TextView tvLock;
    @BindView(R.id.sw_lock)
    Switch swLock;
    @BindView(R.id.tv_alarm)
    TextView tvAlarm;
    @BindView(R.id.sw_alarm)
    Switch swAlarm;

    private Context context;

    // 主线程Handler
    // 用于将从服务器获取的消息显示出来
    private Handler mMainHandler;

    private SensorDevice sensorDevice = null;//传感器设备
    private String relayDeviceNo = "";//继电器编号
    private String fanDeviceNo = "";//风扇编号
    private String lockDeviceNo = "";//电插锁编号
    private String alarmDeviceNo = "";//报警器编号

    private final int RELAY_MSG_CODE = 0;//继电器
    private final int FAN_MSG_CODE = 1;//风扇
    private final int LOCK_MSG_CODE = 2;//电插锁
    private final int ALARM_MSG_CODE = 3;//声光报警器

    Handler mHandler = new Handler();
    Runnable r = new Runnable() {
        @Override
        public void run() {
            //执行
            if (!TextUtils.isEmpty(relayDeviceNo)) {
                String reLay = context.getString(R.string.home_relay_title) + "(设备编号" + relayDeviceNo + ")";
                tvRelay.setText(reLay);
            }
            if (!TextUtils.isEmpty(fanDeviceNo)) {
                String fan = context.getString(R.string.home_fan_title) + "(设备编号" + fanDeviceNo + ")";
                tvFan.setText(fan);
            }
            if (!TextUtils.isEmpty(lockDeviceNo)) {
                String lock = context.getString(R.string.home_electric_bolt_lock_title) + "(设备编号" + lockDeviceNo + ")";
                tvLock.setText(lock);
            }
            if (!TextUtils.isEmpty(alarmDeviceNo)) {
                String alarm = context.getString(R.string.home_alarm_title) + "(设备编号" + alarmDeviceNo + ")";
                tvAlarm.setText(alarm);
            }
            mHandler.postDelayed(r, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_ctrl_main);
        ButterKnife.bind(this);
        context = this;
        //初始化视图
        initView();
        initListener();
        //注册
        EventBus.getDefault().register(this);
        ExitApplication.getInstance().addActivity(this);

        // 实例化主线程,用于更新接收过来的消息
        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    //继电器
                    case RELAY_MSG_CODE:
                        String relayRecvData = (String)(msg.obj).toString();
                        //处理返回状态
                        relayHandleData(relayRecvData);
                        break;
                    //风扇
                    case FAN_MSG_CODE:
                        String fanRecvData = (String)(msg.obj).toString();
                        //处理返回状态
                        fanHandleData(fanRecvData);
                        break;
                    //电插锁
                    case LOCK_MSG_CODE:
                        String lockRecvData = (String)(msg.obj).toString();
                        //处理返回状态
                        lockHandleData(lockRecvData);
                        break;
                    //报警器
                    case ALARM_MSG_CODE:
                        String alarmRecvData = (String)(msg.obj).toString();
                        //处理返回状态
                        alarmHandleData(alarmRecvData);
                        break;
                    case 0x117: //延迟一秒，可调灯再次生效
                        SeekBar bar = (SeekBar) msg.obj;
                        bar.setEnabled(true);
                        break;
                }
            }
        };

        mHandler.postDelayed(r, 5000);
    }

    /**
     *
     * 初始化视图
     *
     **/
    private void initView() {
        tvTitle.setText(this.getString(R.string.composite_experiment_switch_control));
        tvBackName.setVisibility(View.INVISIBLE);
    }

    /***
     *
     * 事件
     *
     * **/
    private void initListener() {
        //继电器
        swRelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(relayDeviceNo)) {
                    ToastUtils.showToast(view.getContext(), view.getContext().getString(R.string.try_again));
                    return;
                }

                if (swRelay.isChecked()) {
                    LogUtils.i("继电器_开");
                    String order = MessageFormat.format(SensorCmd.SENSOR_RELAY_OPEN_CMD, relayDeviceNo);
                    //发送数据到a53
                    EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
                } else {
                    LogUtils.i("继电器_关");
                    String order = MessageFormat.format(SensorCmd.SENSOR_RELAY_CLOSE_CMD, relayDeviceNo);
                    //发送数据到a53
                    EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
                }
            }
        });

        //风扇
        swFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(fanDeviceNo)) {
                    ToastUtils.showToast(view.getContext(), view.getContext().getString(R.string.try_again));
                    return;
                }

                if (swFan.isChecked()) {
                    LogUtils.i("风扇_开");
                    String order = MessageFormat.format(SensorCmd.SENSOR_FAN_OPEN_CMD, fanDeviceNo);
                    //发送数据到a53
                    EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
                } else {
                    LogUtils.i("风扇_关");
                    String order = MessageFormat.format(SensorCmd.SENSOR_FAN_CLOSE_CMD, fanDeviceNo);
                    //发送数据到a53
                    EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
                }
            }
        });

        //电插锁
        swLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(lockDeviceNo)) {
                    ToastUtils.showToast(view.getContext(), view.getContext().getString(R.string.try_again));
                    return;
                }

                if (swLock.isChecked()) {
                    LogUtils.i("电插锁_开");
                    String order = MessageFormat.format(SensorCmd.SENSOR_LOCK_OPEN_CMD, lockDeviceNo);
                    //发送数据到a53
                    EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
                } else {
                    LogUtils.i("电插锁_关");
                    String order = MessageFormat.format(SensorCmd.SENSOR_LOCK_CLOSE_CMD, lockDeviceNo);
                    //发送数据到a53
                    EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
                }
            }
        });


        //报警器
        swAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(alarmDeviceNo)) {
                    ToastUtils.showToast(view.getContext(), view.getContext().getString(R.string.try_again));
                    return;
                }

                if (swAlarm.isChecked()) {
                    LogUtils.i("报警器_开");
                    String order = MessageFormat.format(SensorCmd.SENSOR_ALARM_OPEN_CMD, alarmDeviceNo);
                    //发送数据到a53
                    EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
                } else {
                    LogUtils.i("报警器_关");
                    String order = MessageFormat.format(SensorCmd.SENSOR_ALARM_CLOSE_CMD, alarmDeviceNo);
                    //发送数据到a53
                    EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
                }
            }
        });
    }

    /***
     *
     * 继电器_接收数据处理
     *
     * **/
    private void relayHandleData(String recvData){
        if(recvData.contains("on")){
            //LogUtils.i("继电器_返回状态  开");
            swRelay.setChecked(true);

        }else if(recvData.contains("off")){
            //LogUtils.i("继电器_返回状态  关");
            swRelay.setChecked(false);
        }
    }

    /***
     *
     * 风扇_接收数据处理
     *
     * **/
    private void fanHandleData(String recvData){
        if(recvData.contains("on")){
            //LogUtils.i("风扇__返回状态  开");
            swFan.setChecked(true);

        }else if(recvData.contains("off")){
            // LogUtils.i("风扇__返回状态  关");
            swFan.setChecked(false);
        }
    }

    /***
     *
     * 电插锁_接收数据处理
     *
     * **/
    private void lockHandleData(String recvData){
        if(recvData.contains("on")){
            //LogUtils.i("电插锁__返回状态  开");
            swLock.setChecked(true);

        }else if(recvData.contains("off")){
            //LogUtils.i("电插锁__返回状态  关");
            swLock.setChecked(false);
        }
    }

    /***
     *
     * 声光报警器_接收数据处理
     *
     * **/
    private void alarmHandleData(String recvData){
        if(recvData.contains("on")){
            //LogUtils.i("声光报警器__返回状态  开");
            swAlarm.setChecked(true);

        }else if(recvData.contains("off")){
            //LogUtils.i("声光报警器__返回状态  关");
            swAlarm.setChecked(false);
        }
    }

    /***
     *
     * 返回
     *
     * **/
    @OnClick(R.id.btn_head_back)
    public void clickBackBtn(View view) {
        this.finish();
    }


    /**
     * evenbus事件处理
     **/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMsgModel event) {
        if (event.getMsg().endsWith(GlobalDefs.KEY_EVEN_BUS_MODULE_RECIVE_DATA)) {
            String reciveData = event.getParam();
            LogUtils.i("家庭电器设备控制_接收到的数据=" + reciveData);
            handleSocketReciveData(reciveData);
        }
    }

    /***
     *
     * 处理从socket接收到的数据
     *
     * **/
    private void handleSocketReciveData(String tempData) {
        //LogUtils.i("------------tempData=" + tempData);
        sensorDevice = new SensorDevice();//传感设备
        sensorDevice.setType(tempData.substring(3, 5));//设备类型
        sensorDevice.setNumber(tempData.substring(5, 7));//解析上报数据读取设备编号
        sensorDevice.setSrcData(tempData);

        //继电器
        if (sensorDevice.getType().endsWith(SensorType.SENSOR_RELAY)) {
            LogUtils.i("继电器编号:" + sensorDevice.getNumber());
            if (TextUtils.isEmpty(relayDeviceNo)) {
                relayDeviceNo = sensorDevice.getNumber();
            }

            //发消息
            Message relayMessage = new Message();
            relayMessage.what = RELAY_MSG_CODE;
            relayMessage.obj = tempData;
            mMainHandler.sendMessage(relayMessage);
        }

        //风扇
        else if (sensorDevice.getType().endsWith(SensorType.SENSOR_FAN)) {
            LogUtils.i("风扇编号:" + sensorDevice.getNumber());
            if (TextUtils.isEmpty(fanDeviceNo)) {
                fanDeviceNo = sensorDevice.getNumber();
            }

            //发消息
            Message fanMessage = new Message();
            fanMessage.what = FAN_MSG_CODE;
            fanMessage.obj = tempData;
            mMainHandler.sendMessage(fanMessage);
        }

        //电插锁
        else if (sensorDevice.getType().endsWith(SensorType.SENSOR_LOCK)) {
            LogUtils.i("电插锁编号:" + sensorDevice.getNumber());

            if (TextUtils.isEmpty(lockDeviceNo)) {
                lockDeviceNo = sensorDevice.getNumber();
            }

            //发消息
            Message lockMessage = new Message();
            lockMessage.what = LOCK_MSG_CODE;
            lockMessage.obj = tempData;
            mMainHandler.sendMessage(lockMessage);
        }
        //报警器
        else if (sensorDevice.getType().endsWith(SensorType.SENSOR_ALARM)) {
            LogUtils.i("报警器编号:" + sensorDevice.getNumber());

            if (TextUtils.isEmpty(alarmDeviceNo)) {
                alarmDeviceNo = sensorDevice.getNumber();
            }

            //发消息
            Message alarmMessage = new Message();
            alarmMessage.what = ALARM_MSG_CODE;
            alarmMessage.obj = tempData;
            mMainHandler.sendMessage(alarmMessage);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregister
        EventBus.getDefault().unregister(this);
    }

}
