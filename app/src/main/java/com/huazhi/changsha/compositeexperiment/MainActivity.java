package com.huazhi.changsha.compositeexperiment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huazhi.changsha.compositeexperiment.app.ExitApplication;
import com.huazhi.changsha.compositeexperiment.fragment.HomeFragment;
import com.huazhi.changsha.compositeexperiment.fragment.MineFragment;
import com.huazhi.changsha.compositeexperiment.model.EventMsgModel;
import com.huazhi.changsha.compositeexperiment.utils.GlobalDefs;
import com.huazhi.changsha.compositeexperiment.utils.LogUtils;
import com.huazhi.changsha.compositeexperiment.utils.Util;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @BindView(R.id.parent_home_img)
    ImageView ivHome;
    @BindView(R.id.parent_home_text)
    TextView tvHome;
    @BindView(R.id.parent_mine_img)
    ImageView ivMine;
    @BindView(R.id.parent_mine_text)
    TextView tvMine;

    //两个fragment
    private HomeFragment mHomeFragment;
    private MineFragment mMineFragment;

    //碎片管理器
    private FragmentManager fm;
    private static boolean isExit = false;

    MainReceiver mainReceiver = null;//广播
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        ButterKnife.bind(this);
        fm = getSupportFragmentManager();
        init();
        showFragment(0);
        //网络状态
        Util.setSP(this, GlobalDefs.LS_SHARED_PREFS_NAME, GlobalDefs.KEY_IS_CONN_SOCKET, false);
        ExitApplication.getInstance().addActivity(this);
    }

    private void init() {
        showFragment(0);
        ivHome.setBackgroundResource(R.drawable.home_pushed);
        tvHome.setTextColor(getResources().getColor(R.color.colorLittleBlack));
        checkPermission();

        //创建广播
        mainReceiver = new MainReceiver();
        //动态注册广播
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        //启动广播
        registerReceiver(mainReceiver, intentFilter);

    }

    /**
     * 权限配置
     **/
    private void checkPermission() {
        AndPermission.with(this).permission(
                Permission.LOCATION).start();
    }

    @OnClick({R.id.parent_home, R.id.parent_mine})
    public void onViewClicked(View view) {
        reSetBackground();
        switch (view.getId()) {
            case R.id.parent_home:
                showFragment(0);
                ivHome.setBackgroundResource(R.drawable.home_pushed);
                tvHome.setTextColor(getResources().getColor(R.color.colorLittleBlack));
                break;
            case R.id.parent_mine:
                showFragment(1);
                ivMine.setBackgroundResource(R.drawable.set_pushed);
                tvMine.setTextColor(getResources().getColor(R.color.colorLittleBlack));
                break;
            default:
                break;
        }
    }

    private void showFragment(int position) {
        FragmentTransaction ft = fm.beginTransaction();
        hideFragment(ft);
        switch (position) {
            case 0:
                if (mHomeFragment != null) {
                    ft.show(mHomeFragment);
                } else {
                    mHomeFragment = new HomeFragment();
                    ft.add(R.id.main_fragment, mHomeFragment);
                }
                break;
            case 1:
                if (mMineFragment != null) {
                    ft.show(mMineFragment);
                } else {
                    mMineFragment = new MineFragment();
                    ft.add(R.id.main_fragment, mMineFragment);
                }
                break;
            default:
                break;
        }
        ft.commit();//管理器提交

    }

    //隐藏所有Fragment
    private void hideFragment(FragmentTransaction ft) {

        if (mHomeFragment != null) {
            ft.hide(mHomeFragment);
        }
        if (mMineFragment != null) {
            ft.hide(mMineFragment);
        }
    }

    //重置底部Button的背景和字体颜色
    private void reSetBackground() {
        ivHome.setBackgroundResource(R.drawable.home_normal);
        tvHome.setTextColor(getResources().getColor(R.color.gray_02));

        ivMine.setBackgroundResource(R.drawable.set_normal);
        tvMine.setTextColor(getResources().getColor(R.color.gray_02));
    }

    /**
     * fragment重叠问题处理 注销掉
     **/
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // super.onSaveInstanceState(outState);
    }

    /**
     * 退出应用
     ***/
    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(this, "在按一次退出程序", Toast.LENGTH_SHORT).show();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        } else {
            //发停止命令
            EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_AL_EXIT_STOP_CMD));

            //发调节灯停止命令数据
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    /**
                     *要执行的操作
                     */
                    //退出系统
                    ExitApplication.getInstance().exit(mContext);
                }
            }, 1000);//x秒后执行Runnable中的run方法

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            exit();
            //返回事件监听
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mainReceiver != null) {
            unregisterReceiver(mainReceiver);
        }
    }

    /**
     * 广播
     **/
    class MainReceiver extends BroadcastReceiver {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                        //Toast.makeText(context, "Home键被监听", Toast.LENGTH_SHORT).show();
                        //发可调节灯停止命令_退出
                        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_AL_EXIT_STOP_CMD));

                    } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                        //Toast.makeText(context, "多任务键被监听", Toast.LENGTH_SHORT).show();
                        //发可调节灯停止命令_退出
                        EventBus.getDefault().post(new EventMsgModel(GlobalDefs.KEY_AL_EXIT_STOP_CMD));
                    }
                }
            }
        }
    }
}
