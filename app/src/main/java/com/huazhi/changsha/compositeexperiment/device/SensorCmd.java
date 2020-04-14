package com.huazhi.changsha.compositeexperiment.device;

/**
 *
 * 传感器命令
 *
 * **/
public class SensorCmd {
    //继电器
    public static final String SENSOR_RELAY_OPEN_CMD = "Hwcre{0}02onT";
    public static final String SENSOR_RELAY_CLOSE_CMD = "Hwcre{0}03offT";

    //风扇
    public static final String SENSOR_FAN_OPEN_CMD = "Hwcaf{0}02onT";
    public static final String SENSOR_FAN_CLOSE_CMD = "Hwcaf{0}03offT";

    //电插锁
    public static final String SENSOR_LOCK_OPEN_CMD = "Hwcel{0}02onT";
    public static final String SENSOR_LOCK_CLOSE_CMD = "Hwcel{0}03offT";

    //报警器
    public static final String SENSOR_ALARM_OPEN_CMD = "Hwcsl{0}02onT";
    public static final String SENSOR_ALARM_CLOSE_CMD = "Hwcsl{0}03offT";

    //家庭门锁控制命令
    public static final String GL_LG_REQ_CMD = "HGlLgReq{0}T";//手机->A53 登录请求
    public static final String GL_REG_REQ_CMD = "HGlRegReq{0}T";//手机->A53 注册请求
    public static final String GL_MOD_REQ_CMD = "HGlModReq{0}T";//手机->A53 修改密码请求

    //plc
    public static final String SENSOR_PLC_CLOSE_CMD = "Hpcal0103000T";
    public static final String SENSOR_PLC_SEND_CMD = "Hpcal0103{0}T";
    public static final String SENSOR_PLC_SEND_END_CMD = "Hpcal0106{0}endT";
}
