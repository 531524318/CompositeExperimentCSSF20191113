package com.huazhi.changsha.compositeexperiment.activity.fan;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.huazhi.changsha.compositeexperiment.R;
import com.huazhi.changsha.compositeexperiment.app.ExitApplication;
import com.huazhi.changsha.compositeexperiment.utils.GlobalDefs;
import com.huazhi.changsha.compositeexperiment.utils.ToastUtils;
import com.huazhi.changsha.compositeexperiment.utils.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 *家庭风扇控制系统_配置
* */
public class FanCfgActivity extends AppCompatActivity {
    @BindView(R.id.et_temp_max_value)
    EditText etMaxTemp;
    @BindView(R.id.tv_head_back_name)
    TextView tvBackName;
    @BindView(R.id.tv_head_title)
    TextView tvTitle;
    @BindView(R.id.btn_head_cmd)
    Button btnCmd;
    private double Temp_MIN_VALUB = 16;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fan_cfg);
        ButterKnife.bind(this);
        initView();//初始化视图

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

    /***
     *
     * 初始化数据
     *
     * **/
    private void initData() {
        //温度最大值
        String tempIeMaxValue = (String) Util.getSP(this, GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_HE_MAX_VALUE, String.class);
        if (!TextUtils.isEmpty(tempIeMaxValue)) {
            etMaxTemp.setText(tempIeMaxValue);
        } else {
            etMaxTemp.setText(GlobalDefs.TEMP_MAX_VALUE + "");
        }
        etMaxTemp.setSelection(etMaxTemp.getText().toString().length());
    }
    /***
     *
     * 保存
     *
     * **/
    @OnClick(R.id.btn_save)
    public void clickSaveBtn(View view) {
        //设置最大温度度值
        String tempMaxValue = etMaxTemp.getText().toString();
        if (TextUtils.isEmpty(tempMaxValue)) {
            ToastUtils.showToast(this, this.getResources().getString(R.string.home_temp_input));
        } else {
            double tempValue = Double.parseDouble(tempMaxValue);
            if (tempValue <= Temp_MIN_VALUB) {
                ToastUtils.showToast(this, this.getResources().getString(R.string.home_temp_value_input));
            } else {
                    //保存在本地
                    Util.clearSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_HE_MAX_VALUE);
                    Util.setSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_HE_MAX_VALUE, tempMaxValue);
                    ToastUtils.showToast(this, this.getResources().getString(R.string.success));
            }
        }
    }
    /**
     * 返回
     **/
    @OnClick(R.id.btn_head_back)
    public void clickBackBtn(View view) {
        this.finish();
    }
}
