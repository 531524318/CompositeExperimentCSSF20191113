package com.huazhi.changsha.compositeexperiment.activity.lighting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.huazhi.changsha.compositeexperiment.R;
import com.huazhi.changsha.compositeexperiment.app.ExitApplication;
import com.huazhi.changsha.compositeexperiment.device.SensorType;
import com.huazhi.changsha.compositeexperiment.model.EventMsgModel;
import com.huazhi.changsha.compositeexperiment.model.SensorDevice;
import com.huazhi.changsha.compositeexperiment.utils.GlobalDefs;
import com.huazhi.changsha.compositeexperiment.utils.LogUtils;
import com.huazhi.changsha.compositeexperiment.utils.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.VISIBLE;

/**
 * 家庭照明系统_主页
 */
public class LightingMainActivity extends Activity {
    @BindView(R.id.btn_head_back)
    Button btnBack;
    @BindView(R.id.tv_head_back_name)
    TextView tvBackName;
    @BindView(R.id.tv_head_title)
    TextView tvTitle;
    @BindView(R.id.btn_head_cmd)
    Button btnCmd;
    @BindView(R.id.home_tv_hi_status)
    TextView tvHiStatus;
    @BindView(R.id.home_tv_ie)
    TextView tvIe;
    @BindView(R.id.home_tv_al)
    TextView tvAl;
    @BindView(R.id.home_btn_hand_ctrl)
    Button btnHandCtrl;

    private Context context;
    private String deviceNo = "";//设备编号
    private SensorDevice sensorDevice = null;

    // 用于将从服务器获取的消息显示出来
    private Handler mMainHandler;

    //弹窗相关
    private PopupWindow popupWindow = null;
    private String now_angle = "000";
    private Timer timerlight;
    private String perial = "";//定时器时间间隔

    //倒计时
    private CountDownTimer timer = null;
    private String personStatus = "";//有人或无人状态  011时为有人   010时为无人

    private final int HI_STATUS_MSG_CODE = 0;//人体红外传感器状态
    private final int IE_MSG_CODE = 1;//光照度传感器
    private final int AL_MSG_CODE = 2;//可调节灯传感器
    private final int HI_IE_CTRL_AL_MSG_CODE = 3;//根据人体红外检测和光照度，控制可调节灯的亮度

    private int postStopDelayedTime = 200;//发给可调灯停止命令的延时时间 单位秒
    private int postCloseDelayedTime = 30;//发给可调灯熄灯命令的延时时间 单位秒

    //光照度和可调节灯的对应公式 100-x/(y/100)  x值为实时的光照度值;y为光照度的最值
    private int IE_MAX_VALUE = 1000;//光照度的最值
    private boolean isHavePerson = false;//空间上是否有人
    private boolean isOpenWindow = false;//是否打开手动控制界面，进行可调灯的调节

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lighting_main);
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
                    //人体红外状态消息
                    case HI_STATUS_MSG_CODE:
                        updateHiStatus((String) msg.obj);
                        break;
                    //光照度消息
                    case IE_MSG_CODE:
                        updateIeValue((String) msg.obj);
                        //每次收到光照传感器的数据，如果空间上有人则根据光照的实时数据控制调节灯的亮度；如果无人，则关闭调节灯光（处理控制人体红外传感器）
                        handleCtrlHi((String) msg.obj);
                        break;
                    //可调节灯消息
                    case AL_MSG_CODE:
                        updateAlValue((String) msg.obj);
                        break;
                    //根据人体红外检测，判断空间上是否有人
                    case HI_IE_CTRL_AL_MSG_CODE:
                        String recvData = (String) msg.obj;
                        judgeHavePerson(recvData);
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
        tvTitle.setText(this.getString(R.string.composite_experiment_lighting_system));
        tvBackName.setVisibility(View.INVISIBLE);
        btnCmd.setText(this.getString(R.string.home_cmd_title));
    }

    /***
     *
     * 配置
     *
     * **/
    @OnClick(R.id.btn_head_cmd)
    public void clickCmdBtn(View view) {
        Intent intent = new Intent(this, LightingCfgActivity.class);
        if (!TextUtils.isEmpty(deviceNo)) {
            intent.putExtra(GlobalDefs.KEY_COMM, deviceNo);
        } else {
            intent.putExtra(GlobalDefs.KEY_COMM, "");
        }
        startActivity(intent);
    }

    /***
     *
     * 返回
     *
     * **/
    @OnClick(R.id.btn_head_back)
    public void clickBackBtn(View view) {
       //发送停止
       sendStop();
       this.finish();
    }

    /**
     * 手动控制灯光事件
     **/
    @OnClick(R.id.home_btn_hand_ctrl)
    public void onViewHandCtrlClicked(View view) {
        isOpenWindow = true;
        lightUserInface("Hw");//wifi控制
        //lightUserInface("Hz");//zigbee控制
    }

    /**
     * 可调灯界面
     *
     * @param orderHeader 命令头
     */
    private void lightUserInface(final String orderHeader) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_light, null);
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 5, 5);
        ImageView imageView = (ImageView) view.findViewById(R.id.iv_close_light);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (orderHeader == "Hw") { //wifi控制才发送，zigbee控制不发送
                    if (!TextUtils.isEmpty(deviceNo)) {
                        String orderlight = orderHeader + "cal" + deviceNo + "08stopctrlT";
                        //发送数据到a53
                        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, orderlight));
                    }
                }
                popupWindow.dismiss();
                popupWindow = null;
                isOpenWindow = false;
            }
        });

        SeekBar light_control = (SeekBar) view.findViewById(R.id.light_control);
        int parseInt = Integer.parseInt(now_angle);

        LogUtils.i("lightUserInface  parseInt=" + parseInt);

        light_control.setProgress(parseInt);
        final TextView light_angle = (TextView) view.findViewById(R.id.light_angle);
        light_angle.setText("亮度:" + parseInt + "%");
        EditText control_perial = (EditText) view.findViewById(R.id.control_perial);
        control_perial.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                perial = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        light_control.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                light_angle.setText("亮度:" + progress + "%");
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
                if (orderHeader.contains("Hw")) {
                    TimerTask tasklight = new TimerTask() {
                        @Override
                        public void run() {
                            if (!TextUtils.isEmpty(deviceNo)) {
                                String orderlight = orderHeader + "cal" + deviceNo + "03" + now_angle + "T";
                                //发送数据到a53
                                EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, orderlight));
                            }
                        }
                    };
                    timerlight = new Timer();
                    try {
                        if (!perial.equals("")) {
                            timerlight.schedule(tasklight, 0, Integer.parseInt(perial));
                        } else {
                            timerlight.schedule(tasklight, 0, 500);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (orderHeader.contains("Hw")) {
                    if (!TextUtils.isEmpty(deviceNo)) {
                        String orderlight = orderHeader + "cal" + deviceNo + "06" + now_angle + "endT";
                        //发送数据到a53
                        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, orderlight));
                    }

                    if (timerlight != null) {
                        timerlight.cancel();
                        timerlight = null;
                    }
                    seekBar.setEnabled(false);
                    Message msgRelift = new Message();
                    msgRelift.what = 0x117;
                    msgRelift.obj = seekBar;
                    mMainHandler.sendMessageDelayed(msgRelift, 1000);
                }
            }
        });

        //打开界面时，发送start控制命令
        if (orderHeader == "Hw") { //wifi控制才发送，zigbee控制不发送
            if (!TextUtils.isEmpty(deviceNo)) {
                String orderlight = orderHeader + "cal" + deviceNo + "09startctrlT";
                //发送数据到a53
                EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, orderlight));
            }
        }

        if (!orderHeader.contains("Hw")) {   //不包含Hw 表示就是Zigbee 传输的，显示zigbee按钮
            Button sendOrderBtn = (Button) view.findViewById(R.id.send_light_order);
            sendOrderBtn.setVisibility(VISIBLE);
            sendOrderBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(deviceNo)) {
                        String orderlight = orderHeader + "cal" + deviceNo + "06" + now_angle + "endT";
                        //发送数据到a53
                        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, orderlight));
                    }
                }
            });
        }
    }

    /**
     * evenbus事件处理
     **/
    @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEventMainThread(EventMsgModel event) {
            if (event.getMsg().endsWith(GlobalDefs.KEY_EVEN_BUS_MODULE_RECIVE_DATA)) {
                String reciveData = event.getParam();
                LogUtils.i("家庭照明系统_接收到的数据=" + reciveData);
                handleSocketReciveData(reciveData);
            }
            //发可调节灯停止命令_配置界面的返回
            else if (event.getMsg().endsWith(GlobalDefs.KEY_AL_STOP_CMD)) {
                String numberData = event.getParam();
                sendStopCtrl(numberData);
            }
            //发可调节灯停止命令_按退出按钮
            else if (event.getMsg().endsWith(GlobalDefs.KEY_AL_EXIT_STOP_CMD)) {
                //发送停止
                LogUtils.i("按手机上的多任务按键 Home按键 返回按键 执行停止命令 start...");
                sendStop();
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
        //光照度传感器
        if (sensorDevice.getType().endsWith(SensorType.SENSOR_IE)) {
            //光照度消息
            Message message = new Message();
            message.what = IE_MSG_CODE;
            message.obj = tempData;
            mMainHandler.sendMessage(message);
            //LogUtils.i("光照度传感器 type=" + sensorDevice.getType());
            //LogUtils.i("光照度传感器 number=" + sensorDevice.getNumber());
        }
        //调节灯传感器
        else if (sensorDevice.getType().endsWith(SensorType.SENSOR_AL)) {

            if (TextUtils.isEmpty(deviceNo)) {
                deviceNo = sensorDevice.getNumber();
                LogUtils.i("-------------------调节灯传感器设备编号为 deviceNo =" + deviceNo);

                //保存到本地
                Util.clearSP(LightingMainActivity.this, GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_DEVICE_ID);
                Util.setSP(LightingMainActivity.this, GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_DEVICE_ID, deviceNo);
            }

            //调节灯消息
            Message message = new Message();
            message.what = AL_MSG_CODE;
            message.obj = tempData;
            mMainHandler.sendMessage(message);
            //LogUtils.i("调节灯传感器 type=" + sensorDevice.getType());
            //LogUtils.i("调节灯传感器 number=" + sensorDevice.getNumber());
            //LogUtils.i("调节灯传感器 sensorAlDataList size=" + sensorAlDataList.size());
        }
        //如果获取的数据为人体红外传感器时,则判断空间上是否有人
        else if (sensorDevice.getType().endsWith(SensorType.SENSOR_HI)) {

            //LogUtils.i("对调节灯传感器进行控制 start...");
            // LogUtils.i("人体红外传感器 number=" + sensorDevice.getNumber());
            //LogUtils.i("人体红外传感器 type=" + sensorDevice.getType());

            //人体红外状态消息
            Message message = new Message();
            message.what = HI_STATUS_MSG_CODE;
            message.obj = tempData;
            mMainHandler.sendMessage(message);

            //根据人体红外传感，判断空间上是否有人
            Message ctrlMessage = new Message();
            ctrlMessage.what = HI_IE_CTRL_AL_MSG_CODE;
            ctrlMessage.obj = tempData;
            mMainHandler.sendMessage(ctrlMessage);
        }
    }

    /***
     *
     * 发送停止命令
     *
     * **/
    private void sendStopCtrl(String numberData) {
        LogUtils.i("sendStopCtrl 设备编号=" + numberData);
        //停止命令
        String orderlightStop = "Hwcal" + numberData + "08stopctrlT";
        //发送数据到a53
        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, orderlightStop));
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
            String orderlightStop = "Hwcal" + deviceID + "08stopctrlT";
            //发送数据到a53
            EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, orderlightStop));
        }
    }


    /**
     * 更新人体红外传感器状态
     ***/
    private void updateHiStatus(String data) {
        String personStatusTemp = data.substring(7, 10);//有人或无人状态  011时为有人   010时为无人
        String status = this.getString(R.string.home_hi_status_no_person);
        //有人
        if (personStatusTemp.endsWith(GlobalDefs.HI_HAVE_PERSON)) {
            status = this.getString(R.string.home_hi_status_have_person);
        }
        //没人
        else if (personStatusTemp.endsWith(GlobalDefs.HI_NO_HAVE_PERSON)) {
            status = this.getString(R.string.home_hi_status_no_person);
        }
        tvHiStatus.setText(status);
    }

    /**
     * 更新光照度数据
     **/
    private void updateIeValue(String data) {
        String ieData = data.substring(9, 9 + Integer.parseInt(data.substring(7, 9)));
        Float ieRecv = Float.parseFloat(ieData);
        String ieValue = (int) Math.floor(ieRecv) + "LUX";//Hwdie0106002000T
        tvIe.setText(ieValue);
    }

    /**
     * 更新可调节灯数据
     **/
    private void updateAlValue(String data) {
        String alData = data.substring(9, 9 + Integer.parseInt(data.substring(7, 9)));
        Float alValue = Float.parseFloat(alData);
        int al = (int) Math.floor(alValue);//取整数

        //如果收到控制已经打开
        if (isOpenWindow) {
            now_angle = al + "";
        }
        tvAl.setText(al + "%");
    }

    /**
     * 根据光照度，依据推导公式100-x/(y/100) ，计算可调节灯对应的数值
     **/
    private int calculatorAlValue(String data) {

        int sendIeValue = 0;

        String ieData = data.substring(9, 9 + Integer.parseInt(data.substring(7, 9)));
        Float ieValue = Float.parseFloat(ieData);

        String tempIeMaxValue = (String) Util.getSP(LightingMainActivity.this, GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IE_MAX_VALUE, String.class);
        if (!TextUtils.isEmpty(tempIeMaxValue)) {
            IE_MAX_VALUE = Integer.valueOf(tempIeMaxValue);
        }

        if (ieValue > IE_MAX_VALUE) {
            sendIeValue = 0;
        } else if (ieValue > 0 && ieValue < IE_MAX_VALUE) {
            Float sendValue = 100 - (ieValue / (IE_MAX_VALUE / 100));
            sendIeValue = (int) Math.floor(sendValue);//取整数
            LogUtils.i("calculatorAlValue 计算结果的光照数据 sendIeValue=" + sendIeValue);
        } else if (ieValue == 0) {
            sendIeValue = 100;
        }

        return sendIeValue;
    }

    /**
     * 根据人体红外传感器判断在空间上是否有人
     ***/
    private void judgeHavePerson(String data) {
        personStatus = data.substring(7, 10);//有人或无人状态  011时为有人   010时为无人
        //有人
        if (personStatus.endsWith(GlobalDefs.HI_HAVE_PERSON)) {
            LogUtils.i("handleCtrlHi 有人");
            isHavePerson = true;
        }
        //没人时，倒计时x秒，如果还没人则熄灭可调节灯
        else if (personStatus.endsWith(GlobalDefs.HI_NO_HAVE_PERSON)) {
            LogUtils.i("handleCtrlHi 无人");
            //开启定时器
            openCountDownTimer();
        }
    }

    /***
     *
     * 开启定时器
     *
     * **/
    private void openCountDownTimer() {
        if (timer == null) {
            String delayedTime = (String)Util.getSP(LightingMainActivity.this, GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IE_TIME, String.class);

            LogUtils.i("delayedTime="+delayedTime);
            if(!TextUtils.isEmpty(delayedTime))
            {
                postCloseDelayedTime = Integer.parseInt(delayedTime);
            }

            //倒计时60秒，一次1秒
            timer = new CountDownTimer(postCloseDelayedTime * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // TODO Auto-generated method stub

                    //int remainTime = (int) (millisUntilFinished / 1000L);
                    //LogUtils.i("---------openCountDownTimer remainTime" + remainTime);

                    //有人
                    if (personStatus.endsWith(GlobalDefs.HI_HAVE_PERSON)) {
                        try {
                            //先停止
                            if (timer != null) {
                                timer.cancel();
                                timer = null;
                            }
                        } catch (Exception e) {
                        }

                        //LogUtils.i("============开启定时器 重置 ===============");
                        //重置
                        isHavePerson = true;//空间上为有人
                        openCountDownTimer();
                    }
                }

                @Override
                public void onFinish() {
                    try {
                        //先停止
                        if (timer != null) {
                            timer.cancel();
                            timer = null;
                        }

                        //空间上为无人
                        isHavePerson = false;
                    } catch (Exception e) {
                    }
                }
            }.start();
        }
    }

    /**
     * 处理控制人体红外传感器
     ***/
    private void handleCtrlHi(String data) {

        //空间上有人
        if (isHavePerson) {
            LogUtils.i("handleCtrlHi 空间上 有人");
            int sendAl = calculatorAlValue(data);
            String angle = "100";
            if ((sendAl + "").length() == 1) {
                angle = "00" + sendAl;
            } else if ((sendAl + "").length() == 2) {
                angle = "0" + sendAl;
            }

            LogUtils.i("=========handleCtrlHi 控制调节灯数值为 angle=" + angle);

            //调节灯智能控制
            sendAlMsg(angle);
        }

        //空间上无人时，可调节灯熄灭
        else {
            LogUtils.i("handleCtrlHi 空间上 无人 可调节灯熄灭");
            sendAlMsg("000");
        }
    }

    /***
     *
     * 调节灯智能控制:手动控制下，自动控制不再生效
     *
     * **/
    private void sendAlMsg(String now_angle) {
        //打开界面时，发送start控制命令
        if (!TextUtils.isEmpty(deviceNo) && !isOpenWindow) {
            LogUtils.i("打开界面时，发送start控制命令 ===============");

            final String number = deviceNo;
            //发调节灯结束数据
            String orderlightEnd = "Hwcal" + number + "06" + now_angle + "endT";
            //发送数据到a53
            EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, orderlightEnd));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            //先停止
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        } catch (Exception e) {
        }

        //unregister
        EventBus.getDefault().unregister(this);
    }
}
