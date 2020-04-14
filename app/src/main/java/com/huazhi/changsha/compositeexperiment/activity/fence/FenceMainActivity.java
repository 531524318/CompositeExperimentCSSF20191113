package com.huazhi.changsha.compositeexperiment.activity.fence;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/***
 *
 * 家庭安防报警系统
 *
 * **/
public class FenceMainActivity extends Activity {
    @BindView(R.id.btn_head_back)
    Button btnBack;
    @BindView(R.id.tv_head_back_name)
    TextView tvBackName;
    @BindView(R.id.tv_head_title)
    TextView tvTitle;
    @BindView(R.id.fence_text)
    TextView fence;
    @BindView(R.id.fence_list)
    ListView fence_list;
    @BindView(R.id.human_text)
    TextView human;
    @BindView(R.id.human_list)
    ListView human_list;
    @BindView(R.id.start_btn)
    Button start;

    private Context context;
    // 用于将从服务器获取的消息显示出来
    private Handler mMainHandler;

    private String f = "";
    private String s = "";
    private String d = "";
    private List<String> fence_date_List = new ArrayList();//红外对射入侵记录
    private List<String> human_date_List = new ArrayList();//人体红外入侵记录

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
        setContentView(R.layout.activity_fence_main);
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
                                //声光报警器
                                case "sl":
                                    f = "H" + mode + "c" + type + number + "02onT";//获取连接方式与节点编号，写入控制命令
                                    s = "H" + mode + "c" + type + number + "03offT";
                                    d = nDate.substring(8, 9);
                                    break;
                                case "if"://红外对射传感器
                                    if (nDate.substring(9, 10).equals("0")) {
                                        fence.setTextColor(getResources().getColor(R.color.colorBlackDeep));
                                        fence.setText("无人闯入");
                                    } else {
                                        fence.setTextColor(getResources().getColor(R.color.colorRed));
                                        fence.setText("有人闯入");
                                        //获取当前时间
                                        Calendar ft = Calendar.getInstance();
                                        int year = ft.get(Calendar.YEAR);   //获取年份
                                        int month = ft.get(Calendar.MONTH) + 1;  //获取月份
                                        int day = ft.get(Calendar.DAY_OF_MONTH);  //获取日期
                                        int hour = ft.get(Calendar.HOUR_OF_DAY);  //获取小时
                                        int minute = ft.get(Calendar.MINUTE); //获取分钟
                                        int sec = ft.get(Calendar.SECOND);//获取秒
                                        String fence_date = year + "年" + month + "月" + day + "日" + hour + "时" + minute + "分" + sec + "秒" + "有人闯入";
                                        fence_date_List.add(fence_date);
                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                                FenceMainActivity.this, android.R.layout.simple_list_item_1, fence_date_List);
                                        fence_list.setAdapter(adapter);
                                    }
                                    break;
                                case "hi"://人体红外传感器
                                    if (nDate.substring(9, 10).equals("0")) {
                                        human.setTextColor(getResources().getColor(R.color.colorBlackDeep));
                                        human.setText("检测区域内无人活动");
                                    } else {
                                        human.setTextColor(getResources().getColor(R.color.colorRed));
                                        human.setText("检测区域内有人活动");
                                        //获取当前时间
                                        Calendar ht = Calendar.getInstance();
                                        int year = ht.get(Calendar.YEAR);   //获取年份
                                        int month = ht.get(Calendar.MONTH) + 1;  //获取月份
                                        int day = ht.get(Calendar.DAY_OF_MONTH);  //获取日期
                                        int hour = ht.get(Calendar.HOUR_OF_DAY);  //获取小时
                                        int minute = ht.get(Calendar.MINUTE); //获取分钟
                                        int sec = ht.get(Calendar.SECOND);//获取秒
                                        String human_date = year + "年" + month + "月" + day + "日" + hour + "时" + minute + "分" + sec + "秒" + "有人闯入";
                                        human_date_List.add(human_date);
                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                                FenceMainActivity.this, android.R.layout.simple_list_item_1, human_date_List);
                                        human_list.setAdapter(adapter);
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
                            ToastUtils.showToast(context, "监测已开启");
                        } else {
                            start.setText("开始监测");
                            ToastUtils.showToast(context, "监测已关闭");
                        }
                        break;
                }
            }
        };

        Boolean isConn = (Boolean) Util.getSP(FenceMainActivity.this, GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IS_CONN_SOCKET, Boolean.class);
        if (isConn) {
            //获取当前报警功能状态
            LogUtils.i("获取当前报警功能状态");
            String order = "Hs_get_alarm_status_01T";
            //发送数据到a53
            EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
        }
    }

    private void initView() {
        tvTitle.setText(context.getString(R.string.composite_experiment_fence));
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
                String order = "Hs_start_alarm_fun_01T";
                //发送数据到a53
                EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));

            } else {
                String order = "Hs_close_alarm_fun_01T";
                //发送数据到a53
                EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
            }
        } else {
            ToastUtils.showToast(view.getContext(), "请先设置连接");
        }
    }

    /**
     * 停止报警
     **/
    @OnClick(R.id.stop_btn)
    public void clickStopBtn(View view) {
        if (d.equals("2")) {
            String order = s;
            //发送数据到a53
            EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
            ToastUtils.showToast(view.getContext(), "停止报警命令已发出");
        } else if (d.equals("3")) {
            ToastUtils.showToast(view.getContext(), "报警器未打开");
        } else {
            ToastUtils.showToast(view.getContext(), "请先设置连接并开始监测");
        }
    }

    /**
     * evenbus事件处理
     **/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMsgModel event) {
        if (event.getMsg().endsWith(GlobalDefs.KEY_EVEN_BUS_MODULE_RECIVE_DATA)) {
            String reciveData = event.getParam();
            LogUtils.i("家庭安防报警系统_接收到的数据=" + reciveData);
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
