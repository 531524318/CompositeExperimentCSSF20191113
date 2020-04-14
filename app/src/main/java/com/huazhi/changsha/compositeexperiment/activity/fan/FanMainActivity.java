package com.huazhi.changsha.compositeexperiment.activity.fan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
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
import com.huazhi.changsha.compositeexperiment.utils.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.MessageFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FanMainActivity extends Activity {
    @BindView(R.id.home_tv_temp)
    TextView fanTemp;
    @BindView(R.id.home_tv_humiture)
    TextView fanHumiture;
    @BindView(R.id.home_tv_fan)
    TextView fanDate;

    //弹窗相关
    private PopupWindow popupWindow = null;

    private Handler mMainHandler;// 用于将从服务器获取的消息显示出来
    private boolean controlAutFan = true;//是否为自动控制
    private boolean controlFan = false;//是否达到开启风扇的条件
    private String personStatusTemp;//存储当前温度值
    private SensorDevice sensorDevice = null;//收到的数据
    private double setTemp = 28;//设置的温度值
    private String fanDeviceNo = "";//风扇编号

    private Context context;
    @BindView(R.id.tv_head_back_name)
    TextView tvBackName;
    @BindView(R.id.tv_head_title)
    TextView tvTitle;
    @BindView(R.id.btn_head_cmd)
    Button btnCmd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fan_main);
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
                    case 0:
                        updateTemp((String) msg.obj);
                    case 1:
                        updateFan((String)msg.obj);
                        upFan();
                }
            }
        };
    }

    /**
     * evenbus事件处理
     **/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMsgModel event) {
        if (event.getMsg().endsWith(GlobalDefs.KEY_EVEN_BUS_MODULE_RECIVE_DATA)) {
            String reciveData = event.getParam();
            LogUtils.i("家庭风扇控制系统_接收到的数据=" + reciveData);
            handleSocketReciveData(reciveData);
        }
    }
    /***
     *
     * 处理从socket接收到的数据
     *
     * **/
    private void handleSocketReciveData(String tempData) {
        sensorDevice = new SensorDevice();//传感设备
        sensorDevice.setType(tempData.substring(3, 5));//设备类型
        sensorDevice.setNumber(tempData.substring(5, 7));//解析上报数据读取设备编号
        sensorDevice.setSrcData(tempData);
        //温湿度传感器
        if (sensorDevice.getType().endsWith(SensorType.SENSOR_HE)) {
            //温湿度消息
            Message message = new Message();
            message.what = 0;
            message.obj = tempData;
            mMainHandler.sendMessage(message);
        }
        //风扇
        else if (sensorDevice.getType().endsWith(SensorType.SENSOR_FAN)) {
            //风扇消息
            fanDeviceNo = sensorDevice.getNumber();
            Message message = new Message();
            message.what = 1;
            message.obj = tempData;
            mMainHandler.sendMessage(message);
            }
        }

    private void initView() {
        tvTitle.setText(this.getString(R.string.composite_experiment_fan));
        tvBackName.setVisibility(View.INVISIBLE);
        btnCmd.setText(this.getString(R.string.home_cmd_title));
    }
    /**
     * 更新温湿度传感器状态
     ***/
    private void updateTemp(String data) {
        personStatusTemp = data.substring(11, 15);//温度（℃）
        String personStatusHumiture = data.substring(16, 20);//湿度（%）
        fanTemp.setText(personStatusTemp + "℃");
        fanHumiture.setText(personStatusHumiture + "%");
        Double temp = Double.parseDouble(personStatusTemp);
        LogUtils.i("转化得到的当前温度值："+temp);
        String delayedTemp = (String) Util.getSP(FanMainActivity.this, GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_HE_MAX_VALUE, String.class);
        if(!TextUtils.isEmpty(delayedTemp))
        {
            setTemp = Double.parseDouble(delayedTemp);
            LogUtils.i("转化得到的设置温度值："+setTemp);
        }
        if (temp > setTemp) {
            controlFan = true;
        } else {
            controlFan = false;
        }
    }
    /**
     * 更新风扇状态
     **/
    private void updateFan(String data) {
        if (data.contains("on")) {
            fanDate.setText("开");
        } else if(data.contains("off")){
            fanDate.setText("关");
        }
    }

    /**
     * 调节风扇状态
     **/
    private void upFan() {
        if (controlAutFan) {
            if (controlFan) {
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
    }
    /***
     *
     * 配置
     *
     * **/
    @OnClick(R.id.btn_head_cmd)
    public void clickCmdBtn(View view) {
        Intent intent = new Intent(this, FanCfgActivity.class);
        startActivity(intent);
    }
    /***
     * 返回
     * **/
    @OnClick(R.id.btn_head_back)
    public void clickBackBtn(View view) {
        this.finish();
    }

    /*
     *手动控制
     */
    @OnClick(R.id.fan_btn_hand_ctrl)
    public void clickCtrl(View view) {
        controlAutFan = false;
        ctrlFan();
    }

    private void ctrlFan() {
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_fan, null);
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 20, 50);
        ImageView imageView = (ImageView) view.findViewById(R.id.iv_close_light);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                popupWindow = null;
                controlAutFan = true;
            }
        });
        final Switch btnCtrlFan = view.findViewById(R.id.switchSwitch);
        btnCtrlFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(fanDeviceNo)) {
                    ToastUtils.showToast(view.getContext(), view.getContext().getString(R.string.try_again));
                    return;
                }

                if (btnCtrlFan.isChecked()) {
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
    }
}
