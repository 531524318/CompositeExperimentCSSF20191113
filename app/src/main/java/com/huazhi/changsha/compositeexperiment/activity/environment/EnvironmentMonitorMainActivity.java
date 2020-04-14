package com.huazhi.changsha.compositeexperiment.activity.environment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.huazhi.changsha.compositeexperiment.R;
import com.huazhi.changsha.compositeexperiment.app.ExitApplication;
import com.huazhi.changsha.compositeexperiment.model.EventMsgModel;
import com.huazhi.changsha.compositeexperiment.utils.GlobalDefs;
import com.huazhi.changsha.compositeexperiment.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/***
 *
 * 家庭环境监测
 *
 * **/
public class EnvironmentMonitorMainActivity extends Activity {
    @BindView(R.id.btn_head_back)
    Button btnBack;
    @BindView(R.id.tv_head_back_name)
    TextView tvBackName;
    @BindView(R.id.tv_head_title)
    TextView tvTitle;
    @BindView(R.id.temp_text)
    TextView temp;
    @BindView(R.id.humiture_text)
    TextView humiture;
    @BindView(R.id.ill_text)
    TextView ill;
    @BindView(R.id.human_text)
    TextView human;
    @BindView(R.id.fence_text)
    TextView fence;
    @BindView(R.id.comb_text)
    TextView comb;
    @BindView(R.id.flame_text)
    TextView flame;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_environment_monitor_main);
        ButterKnife.bind(this);
        context = this;
        //初始化视图
        initView();
        //注册
        EventBus.getDefault().register(this);
        ExitApplication.getInstance().addActivity(this);
    }

    private void initView() {
        tvTitle.setText(context.getString(R.string.composite_experiment_environmental_monitor));
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
     * evenbus事件处理
     **/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMsgModel event) {
        if (event.getMsg().endsWith(GlobalDefs.KEY_EVEN_BUS_MODULE_RECIVE_DATA)) {
            String reciveData = event.getParam();
            LogUtils.i("家庭环境监测_接收到的数据=" + reciveData);
            handleSocketReciveData(reciveData);
        }
    }

    private void handleSocketReciveData(String reciveData) {
        if (reciveData.startsWith("H") && reciveData.endsWith("T")) {
            String type = reciveData.substring(3, 5);//解析上报数据读取是哪一个设备  Hwdhi01011T（有人）
            switch (type) {
                case "he"://温湿度
                    String h, t;
                    t = reciveData.substring(11, 15);
                    h = reciveData.substring(16, 20);
                    //字体加粗
                    temp.getPaint().setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
                    temp.getPaint().setAntiAlias(true);//抗锯齿
                    temp.setTextColor(getResources().getColor(R.color.colorBlackDeep));
                    temp.setText(t + "  " + "℃");
                    //字体加粗
                    humiture.getPaint().setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
                    humiture.getPaint().setAntiAlias(true);//抗锯齿
                    humiture.setTextColor(getResources().getColor(R.color.colorBlackDeep));
                    humiture.setText(h + "  " + "%");
                    break;
                case "ie"://光照度
                    int i = Integer.parseInt(reciveData.substring(9, 15));
                    //字体加粗
                    ill.getPaint().setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
                    ill.getPaint().setAntiAlias(true);//抗锯齿
                    ill.setTextColor(getResources().getColor(R.color.colorBlackDeep));
                    ill.setText(i + "  " + "LUX");
                    break;
                case "hi"://人体红外
                    if (reciveData.substring(9, 10).equals("0")) {
                        human.setTextColor(getResources().getColor(R.color.colorBlackDeep));
                        human.setText("检测区域内无人活动");
                    } else {
                        human.setTextColor(getResources().getColor(R.color.colorRed));
                        human.setText("检测区域内有人活动");
                    }
                    break;
                case "if"://红外对射
                    if (reciveData.substring(9, 10).equals("0")) {
                        fence.setTextColor(getResources().getColor(R.color.colorBlackDeep));
                        fence.setText("无人闯入");
                    } else {
                        fence.setTextColor(getResources().getColor(R.color.colorRed));
                        fence.setText("有人闯入");
                    }
                    break;
                case "ga"://可燃气体
                    if (reciveData.substring(9, 10).equals("0")) {
                        comb.setTextColor(getResources().getColor(R.color.colorBlackDeep));
                        comb.setText("没有检测到可燃气体");
                    } else {
                        comb.setTextColor(getResources().getColor(R.color.colorRed));
                        comb.setText("有可燃气体泄漏");
                    }
                    break;
                case "fl"://火焰传感器
                    if (reciveData.substring(9, 10).equals("0")) {
                        flame.setTextColor(getResources().getColor(R.color.colorBlackDeep));
                        flame.setText("没有明火");
                    } else {
                        flame.setTextColor(getResources().getColor(R.color.colorRed));
                        flame.setText("有明火出现");
                    }
                    break;
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
