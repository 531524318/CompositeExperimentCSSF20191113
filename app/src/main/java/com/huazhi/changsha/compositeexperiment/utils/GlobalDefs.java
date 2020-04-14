package com.huazhi.changsha.compositeexperiment.utils;

public class GlobalDefs {

    // 共享文件数据库名
    public static final String LS_SHARED_PREFS_NAME = "lightingSystem_setting";

    public static final String KEY_COMM = "comm";//公共

    // 登录相关
    public static final String KEY_ACCESS_TOKEN = "access_token";//access_token
    public static final String KEY_USER_ID = "userId";//用户ID
    public static final String KEY_USER_NAME = "userName";//账号

    public static final String KEY_SPLASH_FIRST = "splashFirst"; // 启动图
    public static final String KEY_IP_ADDRESS = "IPaddress"; // IP
    public static final String KEY_PORT = "port"; // 端口号码
    public static final String KEY_CITY = "city"; //城市

    public static final String KEY_UPDATE_SOCKET = "updateSocket"; //更新socket连接
    public static final String KEY_IS_CONN_SOCKET = "isConnSocket"; //是否连上socket连接
    public static final String KEY_HEART_SOCKET = "heartSocket"; //心跳socket
    public static final String KEY_CONN_SOCKET_SUCCESS = "ConnSocketSuccess"; //是否连上socket连接


    public static final String KEY_IE_MAX_VALUE = "ieMaxValue"; //光照最大值
    public static final String KEY_DEVICE_ID = "deviceID"; //设备编号
    public static final String KEY_IE_TIME = "ieTime";//人体红外信号延时默认值

    public static final String KEY_AL_STOP_CMD = "alStopCmd"; //发可调节灯停止命令
    public static final String KEY_AL_EXIT_STOP_CMD = "alExitStopCmd"; //发可调节灯停止命令

    public static final String DEFAULT_IP = "192.168.1.200"; //默认ip
    public static final String DEFAULT_PORT = "6004"; //默认端口

    //温湿度传感器
    public static final String KEY_HE_MAX_VALUE = "tempMaxValue"; //温度最大值
    public static final int TEMP_MAX_VALUE = 28;    //温度的初始最值
    public static final String KEY_FAN_TEMP = "fanTemp";//最大温度默认值

    //人体红外传感器
    public static final String HI_HAVE_PERSON = "011"; //有人
    public static final String HI_NO_HAVE_PERSON = "010"; //无人

    //光照度的最值
    public static final int IE_MAX_VALUE = 1000;

    public static final String KEY_SET_PWD = "setPwd"; //设置密码
    public static final String KEY_SET_PWD_SUCCESS = "setPwdSuccess"; //设置密码成功
    public static final String KEY_SET_PWD_OK = "setPwdOk"; //设置密码完成
    public static final String KEY_SET_PWD_FAIL = "setPwdFail"; //设置密码失败
    public static final String KEY_UPDATE_PWD = "setPwd"; //修改密码
    public static final String KEY_PWD = "pwd"; //密码
    public static final String KEY_REGISTER = "register"; //注册
    public static final String KEY_REGISTER_SUCCESS  = "registerSuccess"; //注册成功
    public static final String KEY_REGISTER_FAIL = "registerFail"; //注册失败

    //even bus key
    public static final String KEY_EVEN_BUS_SOCKET_RECIVE_DATA = "even_bus_socket_recive_data"; //socket 接收到的数据
    public static final String KEY_EVEN_BUS_MODULE_RECIVE_DATA = "even_bus_module_recive_data"; //socket 各功能模块接收到的传感器数据
    public static final String KEY_EVEN_BUS_MODULE_SEND_MSG_TO_A53 = "even_bus_module_send_msg_to_a53"; //socket 各功能模块发送数据到a53

}
