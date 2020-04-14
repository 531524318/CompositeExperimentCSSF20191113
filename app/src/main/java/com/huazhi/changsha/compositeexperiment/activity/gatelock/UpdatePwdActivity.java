package com.huazhi.changsha.compositeexperiment.activity.gatelock;

import android.content.Context;
import android.content.Intent;
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
import com.huazhi.changsha.compositeexperiment.model.ModPwdModel;
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
 * 修改密码
 *
 * **/
public class UpdatePwdActivity extends AppCompatActivity {
    @BindView(R.id.tv_head_back_name)
    TextView tvBackName;
    @BindView(R.id.tv_head_title)
    TextView tvTitle;
    @BindView(R.id.setpwd_et_pwd)
    EditText etPwd;
    @BindView(R.id.setpwd_et_repwd)
    EditText etRepwd;

    private String userName ="";
    private String oldPwd ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setpwd);
        ButterKnife.bind(this);
        initView();//初始化视图
        getExtraData();
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
        tvTitle.setText(this.getResources().getString(R.string.update_pwd));
    }

    private void getExtraData() {
        Intent intent = getIntent();
        if (intent != null) {
            userName = intent.getStringExtra(GlobalDefs.KEY_USER_NAME);
            oldPwd = intent.getStringExtra(GlobalDefs.KEY_PWD);
        }
    }

    /***
     *
     * 确定
     *
     * **/
    @OnClick(R.id.btn_submit)
    public void clickSubmitBtn(View view) {
        String pwd = etPwd.getText().toString();
        String rePwd = etRepwd.getText().toString();
        if (TextUtils.isEmpty(pwd) || pwd.length() != 6) {
            ToastUtils.showToast(this, this.getResources().getString(R.string.pwd_new_msg));
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

        //隐藏键盘
        hideKeyboard(etPwd);

        //发消息 app->a53 设置密码
        ModPwdModel model = new ModPwdModel();
        model.setUserName(userName);
        model.setOldPwd(oldPwd);
        model.setNewPwd(rePwd);

        String resultJson = JSON.toJSONString(model);
        String order = MessageFormat.format(SensorCmd.GL_MOD_REQ_CMD, resultJson);
        LogUtils.i("修改密码 order=" + order);
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
        //密码修改成功
        if (event.getMsg().endsWith(GlobalDefs.KEY_SET_PWD_SUCCESS)) {
            ToastUtils.showToast(this, this.getResources().getString(R.string.update_pwd_success));
            this.finish();
        }
        //密码修改失败
        else if (event.getMsg().endsWith(GlobalDefs.KEY_SET_PWD_FAIL)) {
            ToastUtils.showToast(this, this.getResources().getString(R.string.update_pwd_fail));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregister
        EventBus.getDefault().unregister(this);
    }
}
