package com.huazhi.changsha.compositeexperiment.activity.gatelock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huazhi.changsha.compositeexperiment.R;
import com.huazhi.changsha.compositeexperiment.app.ExitApplication;
import com.huazhi.changsha.compositeexperiment.device.SensorCmd;
import com.huazhi.changsha.compositeexperiment.device.SensorType;
import com.huazhi.changsha.compositeexperiment.model.EventMsgModel;
import com.huazhi.changsha.compositeexperiment.model.LoginModel;
import com.huazhi.changsha.compositeexperiment.model.ResultModel;
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
 * 家庭门锁控制系统_主页
 *
 * ***/
public class GateLockMainActivity extends Activity {

    @BindView(R.id.btn_head_back)
    Button btnBack;
    @BindView(R.id.tv_head_back_name)
    TextView tvBackName;
    @BindView(R.id.tv_head_title)
    TextView tvTitle;
    @BindView(R.id.btn_head_cmd)
    Button btnCmd;
    @BindView(R.id.rl_login_username)
    RelativeLayout rlUserName;
    @BindView(R.id.et_login_username)
    EditText etUserName;
    @BindView(R.id.rl_login_pwd)
    RelativeLayout rlPwd;
    @BindView(R.id.et_login_pwd)
    EditText etLoginPwd;
    @BindView(R.id.tv_lock)
    TextView tvLock;
    @BindView(R.id.sw_lock)
    Switch swLock;
    @BindView(R.id.btn_confirm)
    Button btnConfirm;
    @BindView(R.id.btn_register)
    Button btnRegister;
    @BindView(R.id.btn_update_pwd)
    Button btnUpdatePwd;
    @BindView(R.id.rl_ctrl)
    RelativeLayout rlCtrl;

    private Context context;
    // 主线程Handler
    // 用于将从服务器获取的消息显示出来
    private Handler mMainHandler;

    private SensorDevice sensorDevice = null;//传感器设备
    private String lockDeviceNo = "";//"门锁设备ID

    private final String LOGIN_RES = "LgRes";//登录响应
    private final String REGISTER_RES = "RegRes";//注册响应
    private final String MODIFY_PWD_RES = "ModRes";//更新密码响应

    private final int LOCK_MSG_LOGIN_SUCCESS_CODE = 1;//登录成功
    private final int LOCK_MSG_LOGIN_FAIL_CODE = 2;//登录失败
    private final int LOCK_MSG_MOD_SUCCESS_CODE = 3;//修改密码成功
    private final int LOCK_MSG_MOD_FAIL_CODE = 4;//修改密码失败
    private final int LOCK_MSG_REG_SUCCESS_CODE = 5;//注册成功
    private final int LOCK_MSG_REG_FAIL_CODE = 6;//注册失败

    private final int LOCK_MSG_CODE = 7;//电插锁

    private String pwd = "";//密码

    Handler mHandler = new Handler();
    Runnable r = new Runnable() {
        @Override
        public void run() {
            //执行
            if (!TextUtils.isEmpty(lockDeviceNo)) {
                String lock = "门锁(设备编号" + lockDeviceNo + ")";
                tvLock.setText(lock);
            }
            mHandler.postDelayed(r, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_lock_main);
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
                switch (msg.what) {
                    case LOCK_MSG_LOGIN_SUCCESS_CODE: //登录成功
                        ToastUtils.showToast(context, context.getResources().getString(R.string.login_success));
                        rlUserName.setVisibility(View.GONE);//用户名输入框不显示
                        rlPwd.setVisibility(View.GONE);//密码框输入框不显示
                        btnConfirm.setVisibility(View.GONE);//登录按钮不显示
                        btnRegister.setVisibility(View.GONE);//注册按钮不显示
                        rlCtrl.setVisibility(View.VISIBLE);//门锁显示
                        btnUpdatePwd.setVisibility(View.VISIBLE);//修改密码显示
                        break;
                    case LOCK_MSG_LOGIN_FAIL_CODE: //登录
                        ToastUtils.showToast(context, context.getResources().getString(R.string.username_and_pwd_error));
                        break;
                    case LOCK_MSG_MOD_SUCCESS_CODE: //修改密码成功
                        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_SET_PWD_SUCCESS));
                        break;
                    case LOCK_MSG_MOD_FAIL_CODE: //修改密码失败
                        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_SET_PWD_FAIL));
                        break;
                    case LOCK_MSG_REG_SUCCESS_CODE: //注册成功
                        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_REGISTER_SUCCESS));
                        break;
                    case LOCK_MSG_REG_FAIL_CODE: //注册失败
                        String param = (String) msg.obj;
                        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_REGISTER_FAIL, param));
                        break;
                    //电插锁
                    case LOCK_MSG_CODE:
                        String lockRecvData = (String) (msg.obj).toString();
                        //处理返回状态
                        lockHandleData(lockRecvData);
                        break;
                }
            }
        };

        mHandler.postDelayed(r, 5000);
    }

    /**
     * 初始化视图
     **/
    private void initView() {
        tvTitle.setText(context.getString(R.string.composite_experiment_gate_lock));
        tvBackName.setVisibility(View.INVISIBLE);

        //界面不显示
        setViewGone();
    }

    //刷新功能
    private void refresh() {
        //界面不显示
        setViewGone();
    }

    /**
     * 电插锁_接收数据处理
     **/
    private void lockHandleData(String recvData) {
        if (recvData.contains("on")) {
            LogUtils.i("电插锁__返回状态  开");
            swLock.setChecked(true);

        } else if (recvData.contains("off")) {
            LogUtils.i("电插锁__返回状态  关");
            swLock.setChecked(false);
        }
    }

    /***
     *
     * 注册
     *
     * **/
    @OnClick(R.id.btn_register)
    public void clickRegisterBtn(View view) {
        Intent intent = new Intent(GateLockMainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    /***
     *
     * 修改密码
     *
     * **/
    @OnClick(R.id.btn_update_pwd)
    public void clickUpdateBtn(View view) {
        Intent intent = new Intent(GateLockMainActivity.this, UpdatePwdActivity.class);
        intent.putExtra(GlobalDefs.KEY_USER_NAME, etUserName.getText().toString());
        intent.putExtra(GlobalDefs.KEY_PWD, pwd);
        startActivity(intent);
    }

    /**
     * 登录点击事件
     **/
    @OnClick(R.id.btn_confirm)
    public void clickConfirmBtn(View view) {
        String userName = etUserName.getText().toString();
        String pwd = etLoginPwd.getText().toString();

        if (TextUtils.isEmpty(userName) || userName.length() > 16) {
            ToastUtils.showToast(context, this.getResources().getString(R.string.username_msg));
            return;
        } else if (checkPwd(pwd) || pwd.length() != 6) {
            ToastUtils.showToast(context, this.getResources().getString(R.string.pwd_msg));
            return;
        } else {
            //交互操作
            hideKeyboard(etUserName);
            //发送登录操作
            //发消息 app->a53
            LoginModel model = new LoginModel();
            model.setUserName(userName);
            model.setPwd(pwd);

            String resultJson = JSON.toJSONString(model);
            String order = MessageFormat.format(SensorCmd.GL_LG_REQ_CMD, resultJson);

            //发送数据到a53
            EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
        }
    }

    /**
     * 校验密码
     **/
    private boolean checkPwd(String pwd) {
        boolean isCheck = false;
        if (TextUtils.isEmpty(pwd)) {
            isCheck = true;
        }
        return isCheck;
    }

    /**
     * 隐藏键盘
     **/
    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0); //强制隐藏键盘
    }

    /***
     *
     * 事件
     *
     * **/
    private void initListener() {
        //电插锁
        swLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(lockDeviceNo)) {
                    ToastUtils.showToast(view.getContext(), view.getContext().getString(R.string.try_again));
                    return;
                }

                if (swLock.isChecked()) {
                    LogUtils.i("电插锁_开");
                    String order = MessageFormat.format(SensorCmd.SENSOR_LOCK_OPEN_CMD, lockDeviceNo);
                    //发送数据到a53
                    EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
                } else {
                    LogUtils.i("电插锁_关");
                    String order = MessageFormat.format(SensorCmd.SENSOR_LOCK_CLOSE_CMD, lockDeviceNo);
                    //发送数据到a53
                    EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
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
     * 设置界面不显示
     **/
    private void setViewGone() {
        rlCtrl.setVisibility(View.GONE);//门锁控制不显示
        btnUpdatePwd.setVisibility(View.GONE);//修改密码按钮不显示
    }

    /**
     * evenbus事件处理
     **/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMsgModel event) {
        if (event.getMsg().endsWith(GlobalDefs.KEY_EVEN_BUS_MODULE_RECIVE_DATA)) {
            String reciveData = event.getParam();
            LogUtils.i("家庭门锁控制系统_接收到的数据=" + reciveData);
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

        //电插锁
        if (sensorDevice.getType().endsWith(SensorType.SENSOR_LOCK)) {
            if (TextUtils.isEmpty(lockDeviceNo)) {
                lockDeviceNo = sensorDevice.getNumber();
            }
            LogUtils.i("传感器设备的编号 sensorDevice.getNumber()=" + sensorDevice.getNumber());

            Message msg = new Message();
            msg.what = LOCK_MSG_CODE;
            msg.obj = tempData;
            mMainHandler.sendMessage(msg);

        }
        //注册
        else if (tempData.contains(REGISTER_RES)) {
            LogUtils.i("从A53收到_注册响应消息");

            String jsonStr = tempData.substring(9, tempData.length() - 1);//格式如：HGlRegRes{"code": "","msg": "","result": ""}T
            ResultModel model = JSON.parseObject(jsonStr, ResultModel.class);

            //注册成功
            if (model.getCode().equals("0")) {
                Message msg = new Message();
                msg.what = LOCK_MSG_REG_SUCCESS_CODE;
                mMainHandler.sendMessage(msg);
            }
            //注册失败
            else {
                Message msg = new Message();
                msg.what = LOCK_MSG_REG_FAIL_CODE;
                msg.obj = model.getMsg();
                mMainHandler.sendMessage(msg);
            }
        }
        //登录响应
        else if (tempData.contains(LOGIN_RES)) {
            LogUtils.i("从A53收到_登录响应消息");

            String jsonStr = tempData.substring(8, tempData.length() - 1);//格式如：HGlLgReq{"userName": "","pwd": ""}T
            ResultModel model = JSON.parseObject(jsonStr, ResultModel.class);

            //登录成功
            if (model.getCode().equals("0")) {
                pwd = (String) model.getResult();

                Message msg = new Message();
                msg.what = LOCK_MSG_LOGIN_SUCCESS_CODE;
                mMainHandler.sendMessage(msg);
            }
            //登录失败
            else {
                Message msg = new Message();
                msg.what = LOCK_MSG_LOGIN_FAIL_CODE;
                mMainHandler.sendMessage(msg);
            }
        }
        //修改密码
        else if (tempData.contains(MODIFY_PWD_RES)) {
            LogUtils.i("从A53收到_修改密码响应消息");

            String jsonStr = tempData.substring(9, tempData.length() - 1);//格式如：HGlModRes{"code": "","msg": "","result": ""}T
            ResultModel model = JSON.parseObject(jsonStr, ResultModel.class);

            //修改密码成功
            if (model.getCode().equals("0")) {
                pwd = (String) model.getResult();

                Message msg = new Message();
                msg.what = LOCK_MSG_MOD_SUCCESS_CODE;
                mMainHandler.sendMessage(msg);
            }
            //修改密码失败
            else {
                Message msg = new Message();
                msg.what = LOCK_MSG_MOD_FAIL_CODE;
                mMainHandler.sendMessage(msg);
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
