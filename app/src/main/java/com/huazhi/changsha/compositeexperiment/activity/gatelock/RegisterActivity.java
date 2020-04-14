package com.huazhi.changsha.compositeexperiment.activity.gatelock;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huazhi.changsha.compositeexperiment.R;
import com.huazhi.changsha.compositeexperiment.app.ExitApplication;
import com.huazhi.changsha.compositeexperiment.device.SensorCmd;
import com.huazhi.changsha.compositeexperiment.model.EventMsgModel;
import com.huazhi.changsha.compositeexperiment.model.RegisterModel;
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
 * 设置密码
 *
 * **/
public class RegisterActivity extends AppCompatActivity {
    @BindView(R.id.tv_head_back_name)
    TextView tvBackName;
    @BindView(R.id.tv_head_title)
    TextView tvTitle;

    @BindView(R.id.register_et_username)
    EditText etUserName;
    @BindView(R.id.register_et_pwd)
    EditText etPwd;
    @BindView(R.id.register_et_repwd)
    EditText etRepwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        initView();//初始化视图
        //注册
        EventBus.getDefault().register(this);
        ExitApplication.getInstance().addActivity(this);
    }

    /***
     *
     * 初始化视图
     *
     * **/
    private void initView() {
        tvBackName.setVisibility(View.INVISIBLE);
        tvTitle.setText(this.getResources().getString(R.string.register));
    }

    /***
     *
     * 确定
     *
     * **/
    @OnClick(R.id.btn_submit)
    public void clickSubmitBtn(View view) {
        String userName = etUserName.getText().toString();
        String pwd = etPwd.getText().toString();
        String rePwd = etRepwd.getText().toString();

        if (TextUtils.isEmpty(userName) || userName.length() > 16) {
            ToastUtils.showToast(this, this.getResources().getString(R.string.username_msg));
            return;
        }

        if (TextUtils.isEmpty(pwd) || pwd.length() != 6) {
            ToastUtils.showToast(this, this.getResources().getString(R.string.pwd_msg));
            return;
        }
        if (TextUtils.isEmpty(rePwd) || rePwd.length() != 6) {
            ToastUtils.showToast(this, this.getResources().getString(R.string.pwd_re_msg));
            return;
        }
        if (!pwd.equals(rePwd)) {
            ToastUtils.showToast(this, this.getResources().getString(R.string.pwd_equals_msg));
            return;
        }

        //键盘不显示
        hideKeyboard(etUserName);

        //发消息 app->a53 设置密码
        RegisterModel model = new RegisterModel();
        model.setUserName(userName);
        model.setPwd(pwd);

        //格式如：HGlRegReq{"userName": "","pwd": ""}T
        String resultJson = JSON.toJSONString(model);
        //发消息 app->a53 注册
        String order = MessageFormat.format(SensorCmd.GL_REG_REQ_CMD, resultJson);
        LogUtils.i("注册 order=" + order);

        //发送数据到a53
        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
    }

    /**
     * 隐藏键盘
     **/
    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0); //强制隐藏键盘
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

        //注册成功
        if (event.getMsg().endsWith(GlobalDefs.KEY_REGISTER_SUCCESS)) {
            ToastUtils.showToast(this, this.getResources().getString(R.string.register_pwd_success));
            this.finish();
        } else if (event.getMsg().endsWith(GlobalDefs.KEY_REGISTER_FAIL)) {
            String message = event.getParam();
            if (TextUtils.isEmpty(message)) {
                ToastUtils.showToast(this, this.getResources().getString(R.string.register_pwd_fail));
            } else {
                ToastUtils.showToast(this, message);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregister
        EventBus.getDefault().unregister(this);
    }
}