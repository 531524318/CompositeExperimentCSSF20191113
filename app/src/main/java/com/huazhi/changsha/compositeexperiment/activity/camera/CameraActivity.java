package com.huazhi.changsha.compositeexperiment.activity.camera;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
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

/***
 *
 *  摄像头
 *
 * **/
public class CameraActivity extends Activity {
    @BindView(R.id.btn_head_back)
    Button btnBack;
    @BindView(R.id.tv_head_back_name)
    TextView tvBackName;
    @BindView(R.id.tv_head_title)
    TextView tvTitle;
    @BindView(R.id.camera_tv_human)
    TextView tvHuman;
    @BindView(R.id.camera_wv_showvideo)
    WebView webViewShowVideo;
    @BindView(R.id.camera_btn_open)
    Button btnOpen;
    @BindView(R.id.camera_btn_close)
    Button btnClose;
    @BindView(R.id.camera_btn_opencamera)
    Button btnOpencamera;

    private Context context;
    private SensorDevice sensorDevice = null;//传感器设备
    private String lockDeviceNo = "";//"门锁设备ID
    private boolean isOpenWebView = false;//是否打开webview

    private CountDownTimer timer = null;//倒计时
    private Boolean isHavePerson = false;//收到数据当前是否有人
    private Boolean havePerson = false;//判定是否有人
    private int timerTime = 5;//延时时间（S）
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);
        context = this;
        //初始化视图
        initView();
        //注册
        EventBus.getDefault().register(this);
        ExitApplication.getInstance().addActivity(this);
    }

    //初始化view
    private void initView() {
        tvTitle.setText(context.getString(R.string.composite_experiment_camera));
        tvBackName.setVisibility(View.INVISIBLE);
        webViewShowVideo.setVisibility(View.GONE);
//        btnOpen.setVisibility(View.GONE);
//        btnClose.setVisibility(View.GONE);
    }

    private void loadCamera() {
        webViewShowVideo.loadUrl("file:///android_asset/web/video.html");
        webViewShowVideo.getSettings().setJavaScriptEnabled(true);
    }

    /**
     * 返回
     **/
    @OnClick(R.id.btn_head_back)
    public void clickBackBtn(View view) {
        this.finish();
    }

    /**
     * 开门
     **/
    @OnClick(R.id.camera_btn_open)
    public void clickOpenBtn(View view) {
        if (TextUtils.isEmpty(lockDeviceNo)) {
            ToastUtils.showToast(view.getContext(), view.getContext().getString(R.string.try_again));
            return;
        }
        LogUtils.i("电插锁_开");
        String order = MessageFormat.format(SensorCmd.SENSOR_LOCK_OPEN_CMD, lockDeviceNo);
        //发送数据到a53
        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
    }

    /**
     * 关门
     **/
    @OnClick(R.id.camera_btn_close)
    public void clickCloseBtn(View view) {
        if (TextUtils.isEmpty(lockDeviceNo)) {
            ToastUtils.showToast(view.getContext(), view.getContext().getString(R.string.try_again));
            return;
        }
        LogUtils.i("电插锁_关");
        String order = MessageFormat.format(SensorCmd.SENSOR_LOCK_CLOSE_CMD, lockDeviceNo);
        //发送数据到a53
        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
    }

    /**
     * 打开/关闭 摄像头
     **/
    @OnClick(R.id.camera_btn_opencamera)
    public void clickOpenCamera(View view) {
        if (isOpenWebView) {
            ToastUtils.showToast(view.getContext(), "摄像头已打开！");
        } else {
            btnOpencamera.setText("关闭摄像头");
            webViewShowVideo.setVisibility(View.VISIBLE);
            //打开摄像头
            loadCamera();
            isOpenWebView = true;

        }
    }
    /**
     * evenbus事件处理
     **/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMsgModel event) {
        if (event.getMsg().endsWith(GlobalDefs.KEY_EVEN_BUS_MODULE_RECIVE_DATA)) {
            String reciveData = event.getParam();
            LogUtils.i("摄像头_接收到的数据=" + reciveData);
            handleSocketReciveData(reciveData);
        }
    }

    /**
     * 处理数据
     **/
    private void handleSocketReciveData(String reciveData) {
        if (reciveData.startsWith("H") && reciveData.endsWith("T")) {
            sensorDevice = new SensorDevice();//传感设备
            sensorDevice.setType(reciveData.substring(3, 5));//设备类型
            sensorDevice.setNumber(reciveData.substring(5, 7));//解析上报数据读取设备编号
            sensorDevice.setSrcData(reciveData);

            //电插锁
            if (sensorDevice.getType().endsWith(SensorType.SENSOR_LOCK)) {
                if (TextUtils.isEmpty(lockDeviceNo)) {
                    lockDeviceNo = sensorDevice.getNumber();
                }
                LogUtils.i("传感器设备的编号 sensorDevice.getNumber()=" + sensorDevice.getNumber());
            }
            //人体红外
            else if(sensorDevice.getType().endsWith(SensorType.SENSOR_HI)){
                if (reciveData.substring(9, 10).equals("0")) {
                    tvHuman.setTextColor(getResources().getColor(R.color.colorBlackDeep));
                    tvHuman.setText("检测区域内无人活动");
                    isHavePerson = false;
                } else {
                    tvHuman.setTextColor(getResources().getColor(R.color.colorRed));
                    tvHuman.setText("检测区域内有人活动");
                    isHavePerson = true;
                }
                openCountDownTimer();
                openWebView();
            }
        }
    }
    /***
     *
     * 开启定时器
     *
     ***/
    private void openCountDownTimer() {
        if (timer == null) {
            //倒计时30秒，一次1秒
            timer = new CountDownTimer(timerTime * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // TODO Auto-generated method stub

                    //int remainTime = (int) (millisUntilFinished / 1000L);
                    //LogUtils.i("---------openCountDownTimer remainTime" + remainTime);

                    //有人
                    if (isHavePerson) {
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
                        havePerson = true;//空间上为有人
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
                        havePerson = false;
                    } catch (Exception e) {
                    }
                }
            }.start();
        }
        }

    /***
     *
     * 开启定时器
     *
     * **/
    private void openWebView() {
        if (havePerson) {
            if (!isOpenWebView) {
                //显示
                webViewShowVideo.setVisibility(View.VISIBLE);
                btnOpen.setVisibility(View.VISIBLE);
                btnClose.setVisibility(View.VISIBLE);
                //打开摄像头
                loadCamera();
                isOpenWebView = true;
                btnOpencamera.setText("关闭摄像头");
            }
        } else {
            webViewShowVideo.setVisibility(View.GONE);
//            btnOpen.setVisibility(View.GONE);
//            btnClose.setVisibility(View.GONE);
            isOpenWebView = false;
            btnOpencamera.setText("打开摄像头");
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregister
        EventBus.getDefault().unregister(this);
    }
}