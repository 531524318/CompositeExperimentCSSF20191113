package com.huazhi.changsha.compositeexperiment.activity.flame;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.huazhi.changsha.compositeexperiment.R;
import com.huazhi.changsha.compositeexperiment.app.ExitApplication;
import com.huazhi.changsha.compositeexperiment.model.EventMsgModel;
import com.huazhi.changsha.compositeexperiment.utils.GlobalDefs;
import com.huazhi.changsha.compositeexperiment.utils.LogUtils;
import com.huazhi.changsha.compositeexperiment.utils.ToastUtils;
import com.huazhi.changsha.compositeexperiment.utils.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/***
 *
 * 家庭火灾报警系统
 *
 * **/
public class FlameMainActivity extends Activity {
    @BindView(R.id.btn_head_back)
    Button btnBack;
    @BindView(R.id.tv_head_back_name)
    TextView tvBackName;
    @BindView(R.id.tv_head_title)
    TextView tvTitle;
    @BindView(R.id.fire)
    ImageView fire;
    @BindView(R.id.flame_text)
    TextView flame;
    @BindView(R.id.start_btn)
    Button start;
    @BindView(R.id.comb_text)
    TextView comb;

    private Context context;
    // 用于将从服务器获取的消息显示出来
    private Handler mMainHandler;

    //设备类型对象
    String type;
    //设备编号对象
    String number;
    //设备连接方式
    String mode;
    //存储报警设置命令
    private String command;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flame_main);
        ButterKnife.bind(this);
        context = this;
        //初始化视图
        initView();
        //注册
        EventBus.getDefault().register(this);
        ExitApplication.getInstance().addActivity(this);

        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    //传感器传来的数据
                    case 0:
                        String nDate = (String) msg.obj;
                        if (nDate.startsWith("H") && nDate.endsWith("T")) {
                            type = nDate.substring(3, 5);//解析上报数据读取是哪一个设备  Hwdhi01011T（有人）
                            number = nDate.substring(5, 7);//解析上报数据读取设备编号
                            mode = nDate.substring(1, 2);//解析数据读取连接方式
                            switch (type) {
                                case "fl"://火焰传感器
                                    if (nDate.substring(9, 10).equals("0")) {
                                        fire.setBackgroundResource(0);
                                        flame.setTextColor(getResources().getColor(R.color.colorBlackDeep));
                                        flame.setText("没有明火");
                                    } else {
                                        flame.setTextColor(getResources().getColor(R.color.colorRed));
                                        flame.setText("有明火出现");
                                        fire.setBackgroundResource(R.drawable.fire);
                                    }
                                    break;
                                case "ga"://可燃气体传感器
                                    if (nDate.substring(9, 10).equals("0")) {
                                        comb.setTextColor(getResources().getColor(R.color.colorBlackDeep));
                                        comb.setText("没有检测到可燃气体");
                                    } else {
                                        comb.setTextColor(getResources().getColor(R.color.colorRed));
                                        comb.setText("有可燃气体泄漏");
                                    }
                                    break;
                            }
                        }
                        break;
                    //当前报警功能的状态 开启或关闭
                    case 1:
                        command = (String) msg.obj;
                        if (command.substring(9, 13).equals("open")) {
                            start.setText("关闭监测");
                            ToastUtils.showToast(FlameMainActivity.this, "监测已开启");
                        } else {
                            start.setText("开始监测");
                            ToastUtils.showToast(FlameMainActivity.this, "监测已关闭");
                        }
                }
            }

        };

        Boolean isConn = (Boolean) Util.getSP(FlameMainActivity.this, GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IS_CONN_SOCKET, Boolean.class);
        if (isConn) {
            //获取当前报警功能状态
            LogUtils.i("获取当前报警功能状态");
            String order = "Hs_get_alarm_status_02T";
            //发送数据到a53
            EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
        }
    }

    private void initView() {
        tvTitle.setText(context.getString(R.string.composite_experiment_flame));
        tvBackName.setVisibility(View.INVISIBLE);
    }

    /**
     * 返回
     **/
    @OnClick(R.id.btn_head_back)
    public void clickBackBtn(View view) {
        this.finish();
    }

    /**
     * 开始监测
     **/
    @OnClick(R.id.start_btn)
    public void clickStartBtn(View view) {
        Boolean isConn = (Boolean) Util.getSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IS_CONN_SOCKET, Boolean.class);
        if (isConn) {
            if (start.getText().equals("开始监测")) {
                String order = "Hs_start_alarm_fun_02T";
                //发送数据到a53
                EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));

            } else {
                String order = "Hs_close_alarm_fun_02T";
                //发送数据到a53
                EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
            }
        } else {
            ToastUtils.showToast(view.getContext(), "请先设置连接");
        }
    }

    /**
     * evenbus事件处理
     **/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMsgModel event) {
        if (event.getMsg().endsWith(GlobalDefs.KEY_EVEN_BUS_MODULE_RECIVE_DATA)) {
            String reciveData = event.getParam();
            LogUtils.i("家庭火灾报警系统_接收到的数据=" + reciveData);
            handleSocketReciveData(reciveData);
        }
    }

    private void handleSocketReciveData(String reciveData) {
        //传感器传来的数据
        if ((reciveData.startsWith("Hw") || reciveData.startsWith("Hc")) && reciveData.endsWith("T")) {
            String Msg = reciveData;
            Message msg = new Message();
            msg.what = 0;
            msg.obj = Msg;
            mMainHandler.sendMessage(msg);
        }
        //当前报警功能的状态 开启或关闭
        else if (reciveData.startsWith("Hd")) {
            String Msg = reciveData;
            Message msg = new Message();
            msg.what = 1;
            msg.obj = Msg;
            mMainHandler.sendMessage(msg);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregister
        EventBus.getDefault().unregister(this);
    }
}
