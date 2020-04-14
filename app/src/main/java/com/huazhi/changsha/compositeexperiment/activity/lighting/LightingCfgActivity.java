package com.huazhi.changsha.compositeexperiment.activity.lighting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.huazhi.changsha.compositeexperiment.R;
import com.huazhi.changsha.compositeexperiment.app.ExitApplication;
import com.huazhi.changsha.compositeexperiment.model.EventMsgModel;
import com.huazhi.changsha.compositeexperiment.utils.GlobalDefs;
import com.huazhi.changsha.compositeexperiment.utils.LogUtils;
import com.huazhi.changsha.compositeexperiment.utils.ToastUtils;
import com.huazhi.changsha.compositeexperiment.utils.Util;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 家庭照明系统_配置
 **/
public class LightingCfgActivity extends AppCompatActivity {
    @BindView(R.id.tv_head_back_name)
    TextView tvBackName;
    @BindView(R.id.tv_head_title)
    TextView tvTitle;
    @BindView(R.id.et_ie_max_value)
    EditText etIeMaxValue;
    @BindView(R.id.et_time_max_value)
    EditText etIeTime;
    @BindView(R.id.et_device_no)
    EditText etDeviceNo;

    private int LIMIT_MIN_VALUE = 100;//限制最小值
    private int Time_MIN_VALUB = 0;//延时时间最小值为0
    private String srcDeviceNo = "";//设备编号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lighting_cfg);
        ButterKnife.bind(this);
        initView();//初始化视图
        getExtraData();
        initData();
        ExitApplication.getInstance().addActivity(this);
    }

    /***
     *
     * 初始化视图
     *
     * **/
    private void initView() {
        tvBackName.setVisibility(View.INVISIBLE);
        tvTitle.setText(this.getResources().getString(R.string.home_cmd_title));
    }

    private void getExtraData() {
        Intent intent = getIntent();
        if (intent != null) {
            srcDeviceNo = intent.getStringExtra(GlobalDefs.KEY_COMM);
        }
    }

    /***
     *
     * 初始化数据
     *
     * **/
    private void initData(){
        //光照度最大值
        String tempIeMaxValue = (String) Util.getSP(this, GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IE_MAX_VALUE, String.class);
        if (!TextUtils.isEmpty(tempIeMaxValue)) {
            etIeMaxValue.setText(tempIeMaxValue);
        }
        else{
            etIeMaxValue.setText(GlobalDefs.IE_MAX_VALUE+"");
        }
        etIeMaxValue.setSelection(etIeMaxValue.getText().toString().length());

        //设备编号
        String deviceID = (String) Util.getSP(this, GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_DEVICE_ID, String.class);
        if (!TextUtils.isEmpty(deviceID)) {
            etDeviceNo.setText(deviceID);
        }
        etDeviceNo.setSelection(etDeviceNo.getText().toString().length());

    }

    /***
     *
     * 保存
     *
     * **/
    @OnClick(R.id.btn_save)
    public void clickSaveBtn(View view) {
        //设置最小光照度值
        String ieMaxValue = etIeMaxValue.getText().toString();
        if (TextUtils.isEmpty(ieMaxValue)) {
            ToastUtils.showToast(this, this.getResources().getString(R.string.home_ie_input));
        } else {
            int ieValue = Integer.valueOf(ieMaxValue);
            if (ieValue <= LIMIT_MIN_VALUE) {
                ToastUtils.showToast(this, this.getResources().getString(R.string.home_ie_value_input));
            } else {
                if(isCheck100(ieValue))
                {
                    //保存在本地
                    Util.clearSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME,GlobalDefs.KEY_IE_MAX_VALUE);
                    Util.setSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IE_MAX_VALUE, ieMaxValue);
                    ToastUtils.showToast(this, this.getResources().getString(R.string.success));
                }
                else {
                    ToastUtils.showToast(this, this.getResources().getString(R.string.home_ie_value_input));
                }
            }
        }
        //设置人体红外信号延时时间值
        String ieTime = etIeTime.getText().toString();
        if (TextUtils.isEmpty(ieTime)) {
            ToastUtils.showToast(this, this.getResources().getString(R.string.home_ie_time_input));
        } else {
            int ieTimeValue = Integer.valueOf(ieTime);
            if (ieTimeValue <= Time_MIN_VALUB) {
                ToastUtils.showToast(this, this.getResources().getString(R.string.home_ie_value_time_input));
            } else {

                    //保存在本地
                    Util.clearSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IE_TIME);
                    Util.setSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IE_TIME, ieTime);
                    ToastUtils.showToast(this, this.getResources().getString(R.string.success));
                }

            }
        }


    /**
     * 返回
     **/
    @OnClick(R.id.btn_head_back)
    public void clickBackBtn(View view) {
        //handleAl();//可调节灯逻辑处理
        this.finish();
    }

    /**
     *
     * 可调节灯逻辑处理
     *
     * **/
    private void handleAl(){
        String deviceNo = etDeviceNo.getText().toString();
        LogUtils.i("可调节灯设备编号："+deviceNo);

        if(!TextUtils.isEmpty(deviceNo)){
            if(deviceNo.equals("01")||deviceNo.equals("02")|| deviceNo.equals("03")
                    ||deviceNo.equals("04") ||deviceNo.equals("05")
                    ||deviceNo.equals("06")||deviceNo.equals("07")
                    ||deviceNo.equals("08")||deviceNo.equals("09")
                    || deviceNo.equals("10")||deviceNo.equals("11")
                    || deviceNo.equals("12")){

                //保存到本地
                Util.clearSP(this, GlobalDefs.LS_SHARED_PREFS_NAME,GlobalDefs.KEY_DEVICE_ID);
                Util.setSP(this, GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_DEVICE_ID, deviceNo);

                //发可调节灯停止命令
                EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_AL_STOP_CMD,deviceNo));
                this.finish();
            }
            else{
                ToastUtils.showToast(this,this.getResources().getString(R.string.device_no_msg));
            }
        }
        else{
            this.finish();
        }
    }


    /**
     *
     * 判断是否为100的整数倍
     *
     * ***/
    private boolean isCheck100(int ieValue){
        Boolean is100 = false;

        try{
            if(ieValue%100==0){
                is100 = true;
            }
        }catch (Exception e){
        }
        return  is100;
    }
}
