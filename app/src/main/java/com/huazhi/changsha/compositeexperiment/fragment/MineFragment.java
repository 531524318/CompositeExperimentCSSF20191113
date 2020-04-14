package com.huazhi.changsha.compositeexperiment.fragment;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.huazhi.changsha.compositeexperiment.R;
import com.huazhi.changsha.compositeexperiment.model.EventMsgModel;
import com.huazhi.changsha.compositeexperiment.utils.Connection;
import com.huazhi.changsha.compositeexperiment.utils.GlobalDefs;
import com.huazhi.changsha.compositeexperiment.utils.LogUtils;
import com.huazhi.changsha.compositeexperiment.utils.ToastUtils;
import com.huazhi.changsha.compositeexperiment.utils.Util;
import com.huazhi.changsha.compositeexperiment.view.CustomEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.content.Context.INPUT_METHOD_SERVICE;

/***
 *
 * 设置模块
 *
 * **/
public class MineFragment extends BaseFragment {

    @BindView(R.id.btn_head_back)
    Button btnBack;
    @BindView(R.id.tv_head_back_name)
    TextView tvBackName;
    @BindView(R.id.tv_head_title)
    TextView tvTitle;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipe_refresh;
    @BindView(R.id.wifi_status)
    TextView wifi_status;
    @BindView(R.id.et_serverIP)
    CustomEditText et_serverIP;
    @BindView(R.id.et_serverPort)
    CustomEditText et_serverPort;
    @BindView(R.id.complete_right)
    ImageView ivCompleteRight;

    @BindView(R.id.spinner_serverPort)
    Spinner spinnerServerPort;

    @BindView(R.id.btn_link)
    Button btn_link;
    @BindView(R.id.warn_set_button)
    Button set_button;
    @BindView(R.id.tv_version)
    TextView tvVersion;


    Unbinder unbinder;
    private View view;
    private Connection conn = null;

    //存储电话
    private String phoneNumber;
    private CustomEditText phone_et;
    private TextView prompt_tv;

    private String PATTERN_PHONENUMBER = "^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$";//手机号校验规则 | 数字
    private String port = "";

    Handler mHandler = new Handler();
    Runnable r = new Runnable() {
        @Override
        public void run() {
            //执行
            LogUtils.i("检查socket连接 start...");
            Boolean isConn = (Boolean) Util.getSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IS_CONN_SOCKET, Boolean.class);
            if (ivCompleteRight != null) {
                if (isConn) {
                    ivCompleteRight.setImageResource(R.drawable.wifi_true);

                } else {
                    ivCompleteRight.setImageResource(R.drawable.wifi_false);
//                        ToastUtils.showToast(view.getContext(), "正在连接...");
                    //检查wifi连接
                    checkWifiConn();
                    //发送更新socket重新连接消息
                    //EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_UPDATE_SOCKET));
                }
            }
            //发送心跳
            //EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_HEART_SOCKET));
            //检查socket连接
            mHandler.postDelayed(r, 5000);
        }
    };

    @Override
    public int getResource() {
        return R.layout.fragment_mine;
    }

    @Override
    public void init(final View view) {
        this.view = view;
        unbinder = ButterKnife.bind(this, view);
        //初始化视图
        initView();
        conn = new Connection();
        //注册
        EventBus.getDefault().register(this);
        //IP地址
        et_serverIP.addTextChangedListener(new TextWatcher() {              //给输入框设置输入监听
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        final String[] port1D = getResources().getStringArray(R.array.portArray);
        spinnerServerPort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                port = port1D[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btn_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Drawable.ConstantState drawableCs = view.getResources().getDrawable(R.drawable.wifi_true).getConstantState();
//                Boolean connSuccess = (Boolean) Util.getSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_CONN_SOCKET_SUCCESS, Boolean.class);
//                LogUtils.i("============connSuccess:"+connSuccess);
//                if (connSuccess) {
//                    ToastUtils.showToast(view.getContext(), "连接成功");
//                }
                //隐藏键盘
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
                if (getActivity().getCurrentFocus() == null) {
                } else {
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                }
                //IP
                String ip = et_serverIP.getText().toString().trim();
                if (!checkIP(ip)) {
                    ToastUtils.showToast(view.getContext(), "请输入正确的服务器IP");
                    return;
                }

                //String port = et_serverPort.getText().toString().trim();

                if (TextUtils.isEmpty(port)) {
                    //ToastUtils.showToast(view.getContext(), "请输入正确的端口号");
                    //return;
                    port = GlobalDefs.DEFAULT_PORT;
                }

                LogUtils.i("port="+port);

                //保存在本地
                saveOrNot(ip, port, true);
                ToastUtils.showToast(view.getContext(), "设置成功");
                ivCompleteRight.setImageResource(R.drawable.wifi_false);
                //发送更新socket重新连接消息
                EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_UPDATE_SOCKET));
            }
        });
        //下拉刷新
        swipe_refresh.setColorSchemeResources(R.color.colorGreen);
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //检查wifi连接
                checkWifiConn();

                initData();
                swipe_refresh.setRefreshing(false);      //刷新结束，隐藏进度条
            }
        });

        set_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean isConn = (Boolean) Util.getSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IS_CONN_SOCKET, Boolean.class);
                if (isConn) {
                    View viewSet = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_set, null);
                    final PopupWindow popupWindow = new PopupWindow(viewSet, WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT, true);
                    //显示PopupWindow
                    View rootview = LayoutInflater.from(getActivity()).inflate(R.layout.activity_main, null);
                    popupWindow.showAtLocation(rootview, Gravity.CENTER, 20, 20);
                    phone_et = viewSet.findViewById(R.id.et_phone);
                    final Button setNumber_btn = viewSet.findViewById(R.id.btn_setNumber);
                    prompt_tv = viewSet.findViewById(R.id.tv_prompt);
                    ImageView imageView = (ImageView) viewSet.findViewById(R.id.iv_close);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                        }
                    });

                    //进入设置报警电话界面
                    String order = "Henter_sNoT";
                    //发送数据到a53
                    EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));

                    setNumber_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            phoneNumber = phone_et.getText().toString().trim();//获取edittext的内容
                            if (!wordCheck(phoneNumber, PATTERN_PHONENUMBER)) {
                                ToastUtils.showToast(view.getContext(), "请输入正确的手机号码");
                            } else {
                                //隐藏键盘
                                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                                //设置报警电话
                                String order = "HsNo_" + phoneNumber + "T";
                                //发送数据到a53
                                EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53, order));
                            }
                        }
                    });
                } else {
                    ToastUtils.showToast(view.getContext(), "请先设置连接");
                }
            }
        });

        //检查socket连接
        mHandler.postDelayed(r, 3000);
    }


    /**
     * 验证手机号码
     **/
    private boolean wordCheck(String checkWord, String regex) {
        boolean ret = false;
        if (checkWord != null && regex != null) {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(checkWord);
            ret = m.matches();
        }
        return ret;
    }

    /**
     * 初始化视图
     **/
    private void initView() {
        tvTitle.setText("设置中心");
        btnBack.setVisibility(View.INVISIBLE);
        tvBackName.setVisibility(View.INVISIBLE);
        //版本号
        String version = Util. getVersionName(view.getContext());
        tvVersion.setText("版本号：V"+version);
    }

    /**
     * 校验服务器IP
     **/
    private boolean checkIP(String ip) {
        boolean isIP = false;

        if (!TextUtils.isEmpty(ip)) {
            //IP地址验证规则
            String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(ip);
            boolean rs = matcher.matches();// 字符串是否与正则表达式相匹配
            isIP = rs;
        }
        return isIP;
    }

    @Override
    public void loadingDatas() {
        //检查wifi连接
        checkWifiConn();
        //初始化数据
        initData();
    }

    /**
     * 检查wifi连接
     **/
    private void checkWifiConn() {
        conn.wifiStatus(view);
        if (!Connection.localIP.equals("") && !Connection.wifissid.equals("")) {
            wifi_status.setText("WIFI名称: " + Connection.wifissid + "\n手机IP: " + Connection.localIP);
        }
    }

    @Override
    public void startdestroy() {
        unbinder.unbind();
        //unregister
        EventBus.getDefault().unregister(this);
    }

    //保存记忆 输入服务器IP地址
    private void saveOrNot(String serverIP, String serverPort, boolean or) {
        //保存
        if (or) {
            Util.setSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IP_ADDRESS, serverIP);
            Util.setSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_PORT, serverPort);
        } else {
            Util.clearSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IP_ADDRESS);
            Util.clearSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_PORT);
        }
    }

    //初始化数据
    private void initData() {
        //从SharedPreferences中获取地址
        String address = (String) Util.getSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IP_ADDRESS, String.class);
        String port = (String) Util.getSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_PORT, String.class);

        //地址
        if (TextUtils.isEmpty(address)) {
            address = GlobalDefs.DEFAULT_IP;
            Util.setSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IP_ADDRESS, GlobalDefs.DEFAULT_IP);
        }
        et_serverIP.setText(address);
        et_serverIP.setSelection(address.length());//光标设置在内容末尾

        //端口号码
        if (TextUtils.isEmpty(port)) {
            port = GlobalDefs.DEFAULT_PORT;
            Util.setSP(view.getContext(), GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_PORT, GlobalDefs.DEFAULT_PORT);
        }
        et_serverPort.setText(port);
        et_serverPort.setSelection(port.length());//光标设置在内容末尾
    }

    /**
     * evenbus事件处理
     **/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMsgModel event) {
        if (event.getMsg().endsWith(GlobalDefs.KEY_EVEN_BUS_MODULE_RECIVE_DATA)) {
            String reciveData = event.getParam();

            if (reciveData.startsWith("Hhave_No") || reciveData.startsWith("Hno_No") || reciveData.startsWith("HsNo_ok")) {
                LogUtils.i("设置_接收到的数据=" + reciveData);
            }

            if (reciveData.startsWith("Hhave_No")) {
                phone_et.setText(reciveData.substring(9, 20));
                prompt_tv.setText("系统内已存有接收报警电话，如无需变更请点击“确定”按钮");
            } else if (reciveData.startsWith("Hno_No")) {
                prompt_tv.setText("系统内未存有接收报警电话，请填写后点击“确定”按钮");
            } else if (reciveData.startsWith("HsNo_ok")) {
                prompt_tv.setText("报警电话设置成功");
            }
        }
    }
}
