package com.huazhi.changsha.compositeexperiment.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * 提示工具类
 * @date 2019-7-8 上午11:09:38
 */
public class ToastUtils {

    private ToastUtils() {
		/* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    private static Toast toast;

    /**
     * 常用底部提示
     *
     * @param msg
     */
    public static void showToast(Context mContext, String msg) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * 头部提示
     *
     * @param mContext
     * @param msg
     */
    public static void showTopToast(Context mContext, String msg) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

    /**
     * 中部提示
     *
     * @param mContext
     * @param msg
     */
    public static void showCenterToast(Context mContext, String msg) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * 取消提示
     */
    public static void cancel() {
        if (toast != null) {
            toast.cancel();
        }
    }
}
