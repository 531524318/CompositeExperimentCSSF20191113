package com.huazhi.changsha.compositeexperiment.activity.infrared;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huazhi.changsha.compositeexperiment.R;
import com.huazhi.changsha.compositeexperiment.app.ExitApplication;
import com.huazhi.changsha.compositeexperiment.device.SensorType;
import com.huazhi.changsha.compositeexperiment.model.EventMsgModel;
import com.huazhi.changsha.compositeexperiment.model.SensorDevice;
import com.huazhi.changsha.compositeexperiment.utils.GlobalDefs;
import com.huazhi.changsha.compositeexperiment.utils.LogUtils;
import com.huazhi.changsha.compositeexperiment.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/***
 *
 * 家庭红外遥控电器控制_主页
 *
 * **/

public class InfraredRemoteMainActivity extends Activity {
    @BindView(R.id.btn_head_back)
    Button btnBack;
    @BindView(R.id.tv_head_back_name)
    TextView tvBackName;
    @BindView(R.id.tv_head_title)
    TextView tvTitle;
    @BindView(R.id.btn_head_cmd)
    Button btnCmd;

    @BindView(R.id.container_quanxiang)
    GridLayout container;
    @BindView(R.id.refresh_quanxiang)
    Button refresh_quanxiang;
    @BindView(R.id.study_quanxiang)
    Button study_quanxiang;
    @BindView(R.id.send_quanxiang)
    Button send_quanxiang;
    @BindView(R.id.clear_one_quanxiang)
    Button clear_one_quanxiang;
    @BindView(R.id.clear_all_quanxiang)
    Button clear_all_quanxiang;

    private Context context;
    private SensorDevice sensorDevice = null;//传感器设备
    private String deviceNo = "";//设备编号

    // 主线程Handler
    // 用于将从服务器获取的消息显示出来
    private Handler mMainHandler;
    //具体业务
    private String QUANXIANG_STATUS = ""; //STUDY,SEND,CLEAR_ONE,CLEAR_ALL
    private String orderhead = "Hw";
    private List<Button> quanxiangList = new ArrayList<>();   //存储已经学习过的按钮，显示蓝色样式
    private Button quanxiangBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infrared_remote_main);
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
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0x110:
                        String content = msg.obj.toString();
                        //bindsensor.setText(content);
                        break;
                    case 0x111:
                        // zigbeeswitch.setEnabled(true);
                        break;
                    case 0x112:     //延迟3秒不管成功与否都这样处理数据信息
                        for (Button btn : quanxiangList) {
                            btn.setBackgroundResource(R.drawable.button_style);
                        }
                        if (msg.obj instanceof Button) {
                            Button btn = (Button) msg.obj;
                            btn.setText(btn.getText().toString().substring(0, 4));
                            btn.setEnabled(true);
                            ((LinearLayout) (btn.getParent())).setEnabled(true);
                        }
                        QUANXIANG_STATUS = "";
                        break;
                    case 0x113:     //立刻执行--》清除所有节点界面更新
                        String clearall = msg.obj.toString();
                        Toast.makeText(context, clearall, Toast.LENGTH_SHORT).show();
                        if (clearall.contains("成功")) {
                            for (Button btn : quanxiangList) {
                                btn.setBackgroundResource(R.drawable.blue_btn);
                            }
                            quanxiangList.removeAll(quanxiangList);
                        }
                        QUANXIANG_STATUS = "";
                        break;
                    case 0x114:     //立刻执行-》清除单个节点返回信息处理,更改显示界面
                        String sclear = msg.obj.toString();
                        Toast.makeText(context, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        if (sclear.contains("成功"))
                            quanxiangBtn.setBackgroundResource(R.drawable.blue_btn);
                        QUANXIANG_STATUS = "";
                        break;
                    case 0x115:     //立刻执行-》学习命令返回信息处理
                        String sstudy = msg.obj.toString();
                        Toast.makeText(context, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        if (sstudy.contains("成功"))
                            quanxiangBtn.setBackgroundResource(R.drawable.btnclicktrue);
                        QUANXIANG_STATUS = "";
                        break;
                    case 0x116:     //立刻执行-->发射命令界面更新，刷新
                        Toast.makeText(context, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        QUANXIANG_STATUS = "";
                        break;
                    case 100:
                        String recvData = msg.obj.toString();
                        // ToastUtils.showToast(view.getContext(),"接收到的数据 recvData="+ recvData);
                        break;
                }
            }
        };
    }


    /**
     * 初始化视图
     **/
    private void initView() {
        tvTitle.setText(this.getString(R.string.composite_experiment_infrared_remote_control));
        tvBackName.setVisibility(View.INVISIBLE);

        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int orignalWidth = outMetrics.widthPixels;
        int orignalHeight = outMetrics.heightPixels;
        //LogUtils.i("orignalWidth="+orignalWidth);
        //LogUtils.i("orignalHeight="+orignalHeight);

        int width = (orignalWidth - 40) / 5 - 5;
        int height = (orignalHeight - 100) / 20 + 20;

        //LogUtils.i("width=" + width);
        //LogUtils.i("height=" + height);

        for (int i = 0; i < 100; i++) {                 //添加100个按钮
            Button bn = new Button(context);
            bn.setText(i < 10 ? "0" + i : i + "");
            bn.setTextSize(14);

            bn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (quanxiangBtn != null) {
                        if (quanxiangBtn.getBackground().equals(InfraredRemoteMainActivity.this.getResources().getDrawable(R.drawable.btnclicktrue, null))) {

                        } else {
                            quanxiangBtn.setBackgroundResource(R.drawable.blue_btn);
                        }
                        if (quanxiangList.contains(quanxiangBtn))
                            quanxiangBtn.setBackgroundResource(R.drawable.button_style);//记忆学习过的按钮
                    }
                    quanxiangBtn = (Button) v;   //quanxiangBtn指向当前的点击的按钮
                    quanxiangBtn.setBackgroundResource(R.drawable.button_link);
                }
            });
            bn.setBackgroundResource(R.drawable.blue_btn);
            GridLayout.Spec rowspec = GridLayout.spec(i / 5);
            GridLayout.Spec columnspec = GridLayout.spec(i % 5);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowspec, columnspec);
            params.width = width;
            params.leftMargin = 5;
            params.rightMargin = 5;
            params.bottomMargin = 30;
            params.height = height;
            params.setGravity(Gravity.FILL);
            container.addView(bn, params);
        }
    }

    /***
     *
     * 事件
     *
     * **/
    private void initListener() {
        refresh_quanxiang.setOnClickListener(new View.OnClickListener() {    //刷新
            @Override
            public void onClick(View v) {
                //发送刷新按钮指令，获取返回信息
                if (!QUANXIANG_STATUS.equals("")) return;//一定要等待其他按钮按下3秒之后，状态恢复了，这个按钮才能起作用

                if (!TextUtils.isEmpty(deviceNo)) {
                    String order = orderhead + "cor" + deviceNo + "07refreshT";
                    //ToastUtils.showToast(view.getContext(), order);
                    //发送数据到a53
                    EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
                    QUANXIANG_STATUS = "REFRESH";

                    ((Button) v).setText("刷新控制中...");
                    ((LinearLayout) (v.getParent())).setEnabled(false);
                    //延迟3秒不管成功与否都这样处理数据信息
                    Message msg = new Message();
                    msg.what = 0x112;
                    msg.obj = v;
                    mMainHandler.sendMessageDelayed(msg, orderhead.contains("Hw") ? 3000 : 5000);//zigbee间隔五秒可以使用
                } else {
                    ToastUtils.showToast(context, context.getString(R.string.try_again));
                }
            }
        });
        study_quanxiang.setOnClickListener(new View.OnClickListener() {      //学习
            @Override
            public void onClick(View v) {
                //发送学习指令，获取返回信息
                if (!QUANXIANG_STATUS.equals("")) return;
                if (quanxiangBtn != null) {

                    if (!TextUtils.isEmpty(deviceNo)) {
                        String btnNumber = quanxiangBtn.getText().toString();
                        String order = orderhead + "cor" + deviceNo + "08learn_" + btnNumber + "T";
                        //发送数据到a53
                        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));

                        QUANXIANG_STATUS = "STUDY";
                        ((Button) v).setText("学习控制中...");
                        ((LinearLayout) (v.getParent())).setEnabled(false);
                        v.setEnabled(false);
                        //延迟3秒不管成功与否都这样处理数据信息
                        Message msg = new Message();
                        msg.what = 0x112;
                        msg.obj = v;
                        mMainHandler.sendMessageDelayed(msg, orderhead.contains("Hw") ? 3000 : 5000);

                    } else {
                        ToastUtils.showToast(context, context.getString(R.string.try_again));
                    }
                }
            }
        });
        send_quanxiang.setOnClickListener(new View.OnClickListener() {       //发射
            @Override
            public void onClick(View v) {
                //发送发射指令，获取返回信息
                if (!QUANXIANG_STATUS.equals("")) return;
                if (quanxiangBtn != null) {

                    if (!TextUtils.isEmpty(deviceNo)) {
                        String btnNumber = quanxiangBtn.getText().toString();
                        String order = orderhead + "cor" + deviceNo + "07send_" + btnNumber + "T";
                        //发送数据到a53
                        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));

                        QUANXIANG_STATUS = "SEND";
                        ((Button) v).setText("发射控制中...");
                        ((LinearLayout) (v.getParent())).setEnabled(false);
                        v.setEnabled(false);
                        //延迟3秒不管成功与否都这样处理数据信息
                        Message msg = new Message();
                        msg.what = 0x112;
                        msg.obj = v;
                        mMainHandler.sendMessageDelayed(msg, orderhead.contains("Hw") ? 3000 : 5000);
                    } else {
                        ToastUtils.showToast(context, context.getString(R.string.try_again));
                    }
                }
            }
        });
        clear_one_quanxiang.setOnClickListener(new View.OnClickListener() {  //清除
            @Override
            public void onClick(View v) {
                //发送清除单个指令，获取返回信息
                if (!QUANXIANG_STATUS.equals("")) return;
                if (quanxiangBtn != null) {

                    if (!TextUtils.isEmpty(deviceNo)) {
                        String btnNumber = quanxiangBtn.getText().toString();
                        String order = orderhead + "cor" + deviceNo + "12clear_one_" + btnNumber + "T";
                        //发送数据到a53
                        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));

                        QUANXIANG_STATUS = "CLEAR_ONE";
                        ((Button) v).setText("清除单个中...");
                        ((LinearLayout) (v.getParent())).setEnabled(false);
                        v.setEnabled(false);
                        //延迟3秒不管成功与否都这样处理数据信息
                        Message msg = new Message();
                        msg.what = 0x112;
                        msg.obj = v;
                        mMainHandler.sendMessageDelayed(msg, orderhead.contains("Hw") ? 3000 : 5000);
                    } else {
                        ToastUtils.showToast(context, context.getString(R.string.try_again));
                    }
                }
            }
        });
        clear_all_quanxiang.setOnClickListener(new View.OnClickListener() {  //清除所有
            @Override
            public void onClick(View v) {
                //发送清除所有指令，获取返回信息
                if (!QUANXIANG_STATUS.equals("")) return;

                if (!TextUtils.isEmpty(deviceNo)) {
                    String order = orderhead + "cor" + deviceNo + "09clear_allT";
                    //发送数据到a53
                    EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
                    QUANXIANG_STATUS = "CLEAR_ALL";
                    ((Button) v).setText("清除全部中...");
                    ((LinearLayout) (v.getParent())).setEnabled(false);
                    v.setEnabled(false);
                    //延迟3秒不管成功与否都这样处理数据信息
                    Message msg = new Message();
                    msg.what = 0x112;
                    msg.obj = v;
                    mMainHandler.sendMessageDelayed(msg, orderhead.contains("Hw") ? 3000 : 5000);
                } else {
                    ToastUtils.showToast(context, context.getString(R.string.try_again));
                }
            }
        });
    }

    /**
     * 返回
     **/
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
            LogUtils.i("家庭红外遥控电器控制_接收到的数据=" + reciveData);
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

        //全向红外
        if (sensorDevice.getType().endsWith(SensorType.SENSOR_OR)) {
            if(TextUtils.isEmpty(deviceNo)){
                deviceNo = sensorDevice.getNumber();
                LogUtils.i("全红外设备号deviceNo=" + deviceNo);
            }

            //是数据条件
            if (tempData.charAt(2) == 'd') {
                switch (QUANXIANG_STATUS) {
                    case "REFRESH":
                        Message msgRefresh = new Message();
                        msgRefresh.what = 0x116;
                        msgRefresh.obj = "获取刷新节点状态失败";
                        if (tempData.length() == 113) {

                            //清除所有已记忆按钮的样式，还原未记忆按钮样式
                            for (Button btn : quanxiangList) {
                                btn.setBackgroundResource(R.drawable.blue_btn);
                            }
                            quanxiangList.removeAll(quanxiangList);

                            //清除按钮样式
                            int count = container.getChildCount();
                            if (count > 0) {
                                for (int i = 0; i < count; i++) {
                                    Button btn = (Button) container.getChildAt(i);
                                    btn.setBackgroundResource(R.drawable.blue_btn);
                                }
                            }

                            char[] hundred = tempData.substring(12, tempData.length()).toCharArray();
                            //记录下等于'1'的字符的序号，该序号表示按钮是已经学习过命令的
                            for (int i = 0; i < hundred.length; i++) {
                                if (hundred[i] == '1') {
                                    quanxiangList.add((Button) container.getChildAt(i));//加入列表，等待处理
                                }
                            }
                            msgRefresh.obj = "获取刷新节点状态成功";
                        }
                        mMainHandler.sendMessage(msgRefresh);
                        break;
                    case "STUDY":
                        //Hwdor0108learn_okT
                        Message msgStu = new Message();
                        msgStu.what = 0x115;
                        msgStu.obj = "学习命令发送失败";
                        if (tempData.length() == 18) {
                            quanxiangBtn.setBackgroundResource(R.drawable.btnclicktrue);
                            //if(quanxiangList.add(quanxiangBtn)){
                            msgStu.obj = "学习命令发送成功";
                        }
                        //}
                        mMainHandler.sendMessage(msgStu);
                        break;
                    case "SEND":
                        //Hwdor0107send_okT
                        Message msgSend = new Message();
                        msgSend.what = 0x116;
                        msgSend.obj = "发射命令失败";
                        if (tempData.length() == 17)
                            msgSend.obj = "发射命令成功";
                        mMainHandler.sendMessage(msgSend);
                        break;
                    case "CLEAR_ONE":
                        //Hwdor0112clear_one_okT
                        Message msgClear = new Message();
                        msgClear.what = 0x114;
                        msgClear.obj = "清除单个节点失败";
                        if (tempData.length() == 22)
                        //移除等于节点的号,版本太低Lambda无法使用
                        //if(quanxiangList.removeIf(obj->((Button)obj).equals(quanxiangBtn)))
                        {
                            Iterator<Button> iterator = quanxiangList.iterator();
                            while (iterator.hasNext()) {
                                if (((Button) iterator.next()).getText().toString().equals(quanxiangBtn
                                        .getText().toString())) {
                                    iterator.remove();
                                    msgClear.obj = "清除单个节点成功";
                                    break;
                                }
                            }
                        }
                        mMainHandler.sendMessage(msgClear);
                        break;
                    case "CLEAR_ALL":
                        //Hwdor0111clear_all_okT
                        Message msgClearAll = new Message();
                        msgClearAll.what = 0x113;
                        msgClearAll.obj = "清除所有节点失败";
                        if (tempData.length() == 22)
                            msgClearAll.obj = "清除所有节点成功";
                        mMainHandler.sendMessage(msgClearAll);
                        break;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //unregister
        EventBus.getDefault().unregister(this);
    }
}
